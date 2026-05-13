import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Toaster, toast } from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import './Login.css';
import logoImg from '../../assets/logimg.png';
import registerImg from '../../assets/hero.png';

const Login = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();
  
  const [loginData, setLoginData] = useState({ email: '', password: '' });
  const [loginErrors, setLoginErrors] = useState({});

  const [registerData, setRegisterData] = useState({ 
    name: '', 
    email: '', 
    password: '', 
    confirmPassword: '' 
  });
  const [registerErrors, setRegisterErrors] = useState({});
  const [passwordStrength, setPasswordStrength] = useState('');

  const checkPasswordStrength = (pass) => {
    if (!pass) return '';
    if (pass.length < 6) return 'Fraca';
    const hasLetters = /[a-zA-Z]/.test(pass);
    const hasNumbers = /[0-9]/.test(pass);
    const hasSpecial = /[!@#$%^&*]/.test(pass);
    
    if (hasLetters && hasNumbers && hasSpecial && pass.length >= 8) return 'Forte';
    if (hasLetters && hasNumbers) return 'Média';
    return 'Fraca';
  };

  const handleRegisterChange = (e) => {
    const { id, value } = e.target;
    setRegisterData(prev => ({ ...prev, [id]: value }));
    if (id === 'password') setPasswordStrength(checkPasswordStrength(value));
  };

  const handleLoginSubmit = (e) => {
    e.preventDefault();
    let errors = {};
    if (!loginData.email) errors.email = 'E-mail obrigatório';
    if (!loginData.password) errors.password = 'Senha obrigatória';
    setLoginErrors(errors);
    if (Object.keys(errors).length > 0) {
      toast.error('Preencha os campos obrigatórios.');
      return;
    }
    setLoading(true);
    const loginToast = toast.loading('Autenticando...');
    setTimeout(() => {
      setLoading(false);
      toast.success('Bem-vindo!', { id: loginToast });
      navigate('/dashboard');
    }, 1500);
  };

  const handleRegisterSubmit = (e) => {
    e.preventDefault();
    let errors = {};
    if (!registerData.name) errors.name = 'Nome é obrigatório';
    if (!registerData.email) errors.email = 'E-mail é obrigatório';
    if (!registerData.password) errors.password = 'Senha é obrigatória';
    if (registerData.password !== registerData.confirmPassword) errors.confirmPassword = 'Senhas não coincidem';
    setRegisterErrors(errors);
    if (Object.keys(errors).length > 0) {
      toast.error('Verifique o formulário.');
      return;
    }
    setLoading(true);
    const regToast = toast.loading('Criando conta...');
    setTimeout(() => {
      setLoading(false);
      toast.success('Conta criada!', { id: regToast });
      setIsLogin(true);
    }, 1500);
  };

  const containerVariants = {
    hidden: { opacity: 0, y: 30 },
    visible: { 
      opacity: 1, 
      y: 0,
      transition: { duration: 0.8, ease: "easeOut" }
    }
  };

  const formVariants = {
    initial: { opacity: 0, x: 20 },
    animate: { opacity: 1, x: 0 },
    exit: { opacity: 0, x: -20 }
  };

  const floatingAnimation = {
    y: ["0%", "-5%", "0%"],
    transition: {
      duration: 3,
      repeat: Infinity,
      ease: "easeInOut"
    }
  };

  const pulseAnimation = {
    scale: [1, 1.05, 1],
    opacity: [0.1, 0.2, 0.1],
    transition: {
      duration: 4,
      repeat: Infinity,
      ease: "easeInOut"
    }
  };

  return (
    <div className="login-page-wrapper">
      <Toaster position="top-right" />
      
      <motion.div 
        className="main-wrapper"
        variants={containerVariants}
        initial="hidden"
        animate="visible"
      >
        {/* Lado Esquerdo - Branding */}
        <div className="brand-side">
          <motion.div className="particle particle-1" animate={pulseAnimation}></motion.div>
          <motion.div className="particle particle-2" animate={pulseAnimation} transition={{delay: 1}}></motion.div>
          <motion.div className="particle particle-3" animate={pulseAnimation} transition={{delay: 2}}></motion.div>

          <div className="brand-content">
            <motion.h1
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 }}
            >
              Secret Wish
            </motion.h1>
            <motion.p
              key={isLogin ? 'l-txt' : 'r-txt'}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.5 }}
            >
              {isLogin 
                ? "Descubra quem você vai presentear e espalhe alegria 🎁" 
                : "Crie sua conta e comece a espalhar alegria 🎁"}
            </motion.p>
            
            <motion.div 
              className="illustration-box"
              animate={floatingAnimation}
            >
              <AnimatePresence mode="wait">
                <motion.img 
                  key={isLogin ? 'img-l' : 'img-r'}
                  src={isLogin ? logoImg : registerImg} 
                  alt="Illustration" 
                  className="illustration-img"
                  initial={{ opacity: 0, scale: 0.8 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.8 }}
                  transition={{ duration: 0.5 }}
                />
              </AnimatePresence>
              {/* Linha decorativa animada */}
              <svg className="dotted-line" width="200" height="40" viewBox="0 0 200 40">
                <motion.path 
                  d="M0 20 Q 50 0, 100 20 T 200 20" 
                  fill="none" 
                  stroke="rgba(255,255,255,0.3)" 
                  strokeWidth="2" 
                  strokeDasharray="5,5"
                  animate={{ strokeDashoffset: [-20, 0] }}
                  transition={{ repeat: Infinity, duration: 2, ease: "linear" }}
                />
              </svg>
            </motion.div>

            <div className="badges-row">
              {['Seguro', 'Privado', 'Divertido'].map((text, i) => (
                <motion.span 
                  key={text}
                  className="badge-item"
                  initial={{ opacity: 0, x: -10 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.6 + (i * 0.1) }}
                >
                  {text}
                </motion.span>
              ))}
            </div>
          </div>
        </div>

        {/* Lado Direito - Formulário */}
        <div className="form-side">
          <AnimatePresence mode="wait">
            {isLogin ? (
              <motion.div 
                key="login"
                className="form-container"
                variants={formVariants}
                initial="initial"
                animate="animate"
                exit="exit"
              >
                <div className="form-header">
                  <h2>Bem-vindo de volta!</h2>
                  <p>Faça login para gerenciar seus grupos.</p>
                </div>

                <form onSubmit={handleLoginSubmit}>
                  <div className="input-group">
                    <label>E-mail</label>
                    <div className="relative-input">
                      <svg className="icon-left" width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                      </svg>
                      <input
                        type="email"
                        placeholder="seu@email.com"
                        className={loginErrors.email ? 'input-error' : ''}
                        value={loginData.email}
                        onChange={(e) => setLoginData({...loginData, email: e.target.value})}
                      />
                    </div>
                  </div>

                  <div className="input-group">
                    <label>Senha</label>
                    <div className="relative-input">
                      <svg className="icon-left" width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                      </svg>
                      <input
                        type={showPassword ? 'text' : 'password'}
                        placeholder="••••••••"
                        className={loginErrors.password ? 'input-error' : ''}
                        value={loginData.password}
                        onChange={(e) => setLoginData({...loginData, password: e.target.value})}
                      />
                      <button type="button" className="toggle-btn" onClick={() => setShowPassword(!showPassword)}>
                        <AnimatePresence mode="wait" initial={false}>
                          <motion.div
                            key={showPassword ? 'eye-off' : 'eye-on'}
                            initial={{ opacity: 0, rotate: -45 }}
                            animate={{ opacity: 1, rotate: 0 }}
                            exit={{ opacity: 0, rotate: 45 }}
                            transition={{ duration: 0.2 }}
                          >
                            {showPassword ? (
                              <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
                              </svg>
                            ) : (
                              <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                              </svg>
                            )}
                          </motion.div>
                        </AnimatePresence>
                      </button>
                    </div>
                  </div>

                  <div className="options-row">
                    <label className="check-label">
                      <input type="checkbox" />
                      <span>Lembrar de mim</span>
                    </label>
                    <a href="#" className="link-purple">Esqueceu a senha?</a>
                  </div>

                  <motion.button 
                    type="submit" 
                    className="btn-submit" 
                    disabled={loading}
                    whileHover={{ scale: 1.02, y: -2 }}
                    whileTap={{ scale: 0.98 }}
                  >
                    {loading ? <div className="loader"></div> : 'Entrar'}
                  </motion.button>

                  <div className="text-divider">Ou entre com</div>

                  <motion.button 
                    type="button" 
                    className="btn-google"
                    whileHover={{ backgroundColor: "#f8fafc", y: -1 }}
                    whileTap={{ scale: 0.99 }}
                  >
                    <svg width="20" height="20" viewBox="0 0 24 24">
                      <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
                      <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
                      <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
                      <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
                    </svg>
                    Entrar com Google
                  </motion.button>
                </form>

                <div className="form-footer">
                  Não tem uma conta? <button type="button" onClick={() => setIsLogin(false)} className="link-button">Inscrever-se</button>
                </div>
              </motion.div>
            ) : (
              <motion.div 
                key="register"
                className="form-container"
                variants={formVariants}
                initial="initial"
                animate="animate"
                exit="exit"
              >
                <div className="form-header">
                  <h2>Crie sua conta</h2>
                  <p>Junte-se ao Secret Wish e comece o sorteio!</p>
                </div>

                <form onSubmit={handleRegisterSubmit}>
                  <div className="input-group">
                    <label>Nome completo</label>
                    <div className="relative-input">
                      <svg className="icon-left" width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      <input
                        type="text"
                        placeholder="Seu nome"
                        className={registerErrors.name ? 'input-error' : ''}
                        value={registerData.name}
                        onChange={handleRegisterChange}
                      />
                    </div>
                  </div>

                  <div className="input-group">
                    <label>E-mail</label>
                    <div className="relative-input">
                      <svg className="icon-left" width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                      </svg>
                      <input
                        type="email"
                        placeholder="seu@email.com"
                        className={registerErrors.email ? 'input-error' : ''}
                        value={registerData.email}
                        onChange={handleRegisterChange}
                      />
                    </div>
                  </div>

                  <div className="input-group">
                    <label>Senha</label>
                    <div className="relative-input">
                      <svg className="icon-left" width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                      </svg>
                      <input
                        type={showPassword ? 'text' : 'password'}
                        placeholder="••••••••"
                        className={registerErrors.password ? 'input-error' : ''}
                        value={registerData.password}
                        onChange={handleRegisterChange}
                      />
                      <button type="button" className="toggle-btn" onClick={() => setShowPassword(!showPassword)}>
                        <AnimatePresence mode="wait" initial={false}>
                          <motion.div
                            key={showPassword ? 'eye-off' : 'eye-on'}
                            initial={{ opacity: 0, rotate: -45 }}
                            animate={{ opacity: 1, rotate: 0 }}
                            exit={{ opacity: 0, rotate: 45 }}
                            transition={{ duration: 0.2 }}
                          >
                            {showPassword ? (
                              <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
                              </svg>
                            ) : (
                              <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                              </svg>
                            )}
                          </motion.div>
                        </AnimatePresence>
                      </button>
                    </div>
                    {registerData.password && (
                      <div className="strength-meter">
                        <div className={`strength-bar ${passwordStrength.toLowerCase()}`}></div>
                        <span>Força: {passwordStrength}</span>
                      </div>
                    )}
                  </div>

                  <motion.button 
                    type="submit" 
                    className="btn-submit" 
                    disabled={loading}
                    whileHover={{ scale: 1.02, y: -2 }}
                    whileTap={{ scale: 0.98 }}
                  >
                    {loading ? <div className="loader"></div> : 'Criar conta'}
                  </motion.button>

                  <div className="text-divider">Ou cadastre-se com</div>

                  <motion.button 
                    type="button" 
                    className="btn-google"
                    whileHover={{ backgroundColor: "#f8fafc", y: -1 }}
                    whileTap={{ scale: 0.99 }}
                  >
                    <svg width="20" height="20" viewBox="0 0 24 24">
                      <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
                      <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
                      <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
                      <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
                    </svg>
                    Continuar com Google
                  </motion.button>
                </form>

                <div className="form-footer">
                  Já tem uma conta? <button type="button" onClick={() => setIsLogin(true)} className="link-button">Entrar</button>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </motion.div>
    </div>
  );
};

export default Login;
