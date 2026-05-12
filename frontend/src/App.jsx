import React from 'react';
import MainLayout from './components/MainLayout/MainLayout';
import Dashboard from './components/Dashboard/Dashboard';
import './App.css';

function App() {
  return (
    <MainLayout>
      <Dashboard />
    </MainLayout>
  );
}

export default App;
