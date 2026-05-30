param(
    [string]$Url = "http://127.0.0.1:8080/v3/api-docs",
    [int]$TotalRequests = 2000,
    [int]$Concurrency = 50
)

$ErrorActionPreference = "Stop"

$pidLine = (netstat -ano | Select-String ":8080\s+.*LISTENING" | Select-Object -First 1).ToString()
$backendPid = [int]($pidLine -replace ".*\s+(\d+)\s*$", '$1')

# Warm-up para carregar servlet, classes e caches antes da medicao principal.
1..50 | ForEach-Object {
    try {
        Invoke-WebRequest -UseBasicParsing $Url -TimeoutSec 10 | Out-Null
    } catch {
    }
}

$procBefore = Get-Process -Id $backendPid
$cpuBefore = $procBefore.CPU
$start = Get-Date
$latencies = [System.Collections.Concurrent.ConcurrentBag[double]]::new()
$errors = [System.Collections.Concurrent.ConcurrentBag[string]]::new()

$pool = [runspacefactory]::CreateRunspacePool(1, $Concurrency)
$pool.Open()
$jobs = New-Object System.Collections.Generic.List[object]

for ($i = 0; $i -lt $TotalRequests; $i++) {
    $ps = [powershell]::Create()
    $ps.RunspacePool = $pool
    [void]$ps.AddScript({
        param($RequestUrl, $LatencyBag, $ErrorBag)

        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        try {
            $response = Invoke-WebRequest -UseBasicParsing $RequestUrl -TimeoutSec 20
            $sw.Stop()
            if ($response.StatusCode -eq 200) {
                $LatencyBag.Add($sw.Elapsed.TotalMilliseconds)
            } else {
                $ErrorBag.Add("HTTP $($response.StatusCode)")
            }
        } catch {
            $sw.Stop()
            $ErrorBag.Add($_.Exception.GetType().Name)
        }
    }).AddArgument($Url).AddArgument($latencies).AddArgument($errors)

    $jobs.Add([pscustomobject]@{ Pipe = $ps; Handle = $ps.BeginInvoke() })
}

$initialProcess = Get-Process -Id $backendPid
$peakWorkingSetMb = $initialProcess.WorkingSet64 / 1MB
$peakPrivateMb = $initialProcess.PrivateMemorySize64 / 1MB
$cpuSamples = New-Object System.Collections.Generic.List[double]
$lastCpu = (Get-Process -Id $backendPid).CPU
$lastTime = Get-Date

while (@($jobs | Where-Object { -not $_.Handle.IsCompleted }).Count -gt 0) {
    Start-Sleep -Milliseconds 500
    try {
        $process = Get-Process -Id $backendPid
        $now = Get-Date
        $cpuNow = $process.CPU
        $elapsed = ($now - $lastTime).TotalSeconds
        if ($elapsed -gt 0) {
            $cpuSamples.Add((($cpuNow - $lastCpu) / $elapsed) * 100)
        }
        $lastCpu = $cpuNow
        $lastTime = $now
        $peakWorkingSetMb = [Math]::Max($peakWorkingSetMb, $process.WorkingSet64 / 1MB)
        $peakPrivateMb = [Math]::Max($peakPrivateMb, $process.PrivateMemorySize64 / 1MB)
    } catch {
    }
}

foreach ($job in $jobs) {
    $job.Pipe.EndInvoke($job.Handle)
    $job.Pipe.Dispose()
}
$pool.Close()
$pool.Dispose()

$end = Get-Date
$duration = ($end - $start).TotalSeconds
$procAfter = Get-Process -Id $backendPid
$sorted = @($latencies.ToArray() | Sort-Object)

function Get-Percentile([double[]]$Values, [double]$Percentile) {
    if ($Values.Count -eq 0) {
        return 0
    }

    $index = [Math]::Ceiling(($Percentile / 100.0) * $Values.Count) - 1
    $index = [Math]::Max(0, [Math]::Min($index, $Values.Count - 1))
    return $Values[$index]
}

$avgLatency = if ($sorted.Count -gt 0) { ($sorted | Measure-Object -Average).Average } else { 0 }
$avgCpuPct = if ($cpuSamples.Count -gt 0) { ($cpuSamples | Measure-Object -Average).Average } else { 0 }
$maxCpuPct = if ($cpuSamples.Count -gt 0) { ($cpuSamples | Measure-Object -Maximum).Maximum } else { 0 }

[pscustomobject]@{
    BackendPid = $backendPid
    Url = $Url
    TotalRequests = $TotalRequests
    Concurrency = $Concurrency
    DurationSeconds = [Math]::Round($duration, 2)
    RequestsPerSecond = [Math]::Round($TotalRequests / $duration, 2)
    Success = $sorted.Count
    Errors = $errors.Count
    AvgLatencyMs = [Math]::Round($avgLatency, 2)
    P50LatencyMs = [Math]::Round((Get-Percentile $sorted 50), 2)
    P95LatencyMs = [Math]::Round((Get-Percentile $sorted 95), 2)
    P99LatencyMs = [Math]::Round((Get-Percentile $sorted 99), 2)
    CpuSecondsDelta = [Math]::Round($procAfter.CPU - $cpuBefore, 2)
    AvgCpuPercentOneCore = [Math]::Round($avgCpuPct, 2)
    MaxCpuPercentOneCore = [Math]::Round($maxCpuPct, 2)
    WorkingSetMbAfter = [Math]::Round($procAfter.WorkingSet64 / 1MB, 2)
    PeakWorkingSetMb = [Math]::Round($peakWorkingSetMb, 2)
    PrivateMemoryMbAfter = [Math]::Round($procAfter.PrivateMemorySize64 / 1MB, 2)
    PeakPrivateMemoryMb = [Math]::Round($peakPrivateMb, 2)
} | Format-List
