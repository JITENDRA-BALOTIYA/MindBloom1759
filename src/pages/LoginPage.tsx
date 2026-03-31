import React, { useState } from 'react';
import { Box, Container, Paper, TextField, Button, Typography, Link, CircularProgress, Alert } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { motion } from 'framer-motion';
import Logo from '@components/Logo';
import { firebaseAuthService } from '../firebase/authService';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';

const LoginPage: React.FC = () => {
  const muiTheme = useTheme();
  const isLight = muiTheme.palette.mode === 'light';
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleEmailLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await firebaseAuthService.loginWithEmail(email, password);
      toast.success('Login successful!');
      if (rememberMe) {
        localStorage.setItem('mindbloom-remember', email);
      }
      navigate('/dashboard');
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Login failed';
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = async () => {
    setLoading(true);
    setError('');

    try {
      await firebaseAuthService.loginWithGoogle();
      toast.success('Login successful!');
      navigate('/dashboard');
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Google login failed';
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: isLight
          ? 'linear-gradient(165deg, #EEF4FF 0%, #ECFDFB 35%, #F8FAFC 70%, #E8EEF9 100%)'
          : 'linear-gradient(135deg, #0B1220 0%, #131B2E 100%)',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      <Box
        sx={{
          position: 'absolute',
          top: '-40%',
          right: '-25%',
          width: '90%',
          height: '90%',
          background: 'radial-gradient(ellipse, rgba(59, 130, 246, 0.18) 0%, rgba(13, 148, 136, 0.1) 40%, transparent 68%)',
          pointerEvents: 'none',
        }}
      />
      <Box
        sx={{
          position: 'absolute',
          bottom: '-30%',
          left: '-20%',
          width: '70%',
          height: '70%',
          background: 'radial-gradient(circle, rgba(13, 148, 136, 0.12) 0%, transparent 65%)',
          pointerEvents: 'none',
        }}
      />

      <Container maxWidth="sm">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
        >
          <Paper
            elevation={0}
            sx={{
              p: { xs: 4, sm: 6 },
              borderRadius: '28px',
              backdropFilter: 'blur(24px) saturate(170%)',
              WebkitBackdropFilter: 'blur(24px) saturate(170%)',
              ...(isLight
                ? {
                    background: 'rgba(255, 255, 255, 0.78)',
                    border: '1px solid rgba(255, 255, 255, 0.95)',
                    boxShadow: '0 24px 64px rgba(15, 23, 42, 0.1), 0 8px 24px rgba(59, 130, 246, 0.08)',
                  }
                : {
                    background: 'rgba(19, 27, 46, 0.85)',
                    border: '1px solid rgba(148, 163, 184, 0.15)',
                    boxShadow: '0 24px 64px rgba(0, 0, 0, 0.35)',
                  }),
            }}
          >
            {/* Logo and Tagline */}
            <Box sx={{ textAlign: 'center', mb: 4 }}>
              <Logo size="large" />
              <Typography variant="subtitle1" sx={{ mt: 2, color: 'text.secondary', fontWeight: 500, fontSize: '1.05rem', letterSpacing: '0.02em' }}>
                Nurturing minds, thoughtfully.
              </Typography>
            </Box>

            {/* Title */}
            <Typography variant="h4" sx={{ textAlign: 'center', mb: 1, fontWeight: 800, letterSpacing: '-0.03em' }}>
              Admin Login
            </Typography>
            <Typography variant="body2" sx={{ textAlign: 'center', mb: 4, color: 'text.secondary' }}>
              Welcome back to the MindBloom console
            </Typography>

            {/* Error Alert */}
            {error && (
              <Alert severity="error" sx={{ mb: 3 }}>
                {error}
              </Alert>
            )}

            {/* Login Form */}
            <Box component="form" onSubmit={handleEmailLogin} sx={{ mt: 3 }}>
              <TextField
                fullWidth
                label="Email Address"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                margin="normal"
                required
                disabled={loading}
              />

              <TextField
                fullWidth
                label="Password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                margin="normal"
                required
                disabled={loading}
              />

              <Box
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  mt: 2,
                  mb: 3,
                }}
              >
                <Box>
                  <input
                    type="checkbox"
                    id="remember"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                    disabled={loading}
                    style={{ marginRight: 8, cursor: 'pointer' }}
                  />
                  <label htmlFor="remember" style={{ cursor: 'pointer', fontSize: '0.9rem', color: '#64748B' }}>
                    Remember me
                  </label>
                </Box>
                <Link href="#" underline="hover" sx={{ fontSize: '0.9rem', color: 'primary.main', fontWeight: 600 }}>
                  Forgot password?
                </Link>
              </Box>

              <Button
                fullWidth
                variant="contained"
                size="large"
                type="submit"
                disabled={loading || !email || !password}
                sx={{ mb: 2 }}
              >
                {loading ? <CircularProgress size={24} color="inherit" /> : 'Sign In'}
              </Button>

              <Button
                fullWidth
                variant="outlined"
                size="large"
                onClick={handleGoogleLogin}
                disabled={loading}
                sx={{ mb: 3 }}
              >
                {loading ? 'Loading...' : 'Continue with Google'}
              </Button>
            </Box>

            {/* Demo Credentials */}
            <Paper
              variant="outlined"
              sx={{
                p: 2,
                backgroundColor: 'rgba(13, 148, 136, 0.06)',
                borderColor: 'rgba(13, 148, 136, 0.25)',
                borderRadius: '16px',
                mt: 3,
              }}
            >
              <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 1, fontWeight: 600 }}>
                Demo credentials (testing)
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                Email: demo@mindbloom.com
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                Password: demo123456
              </Typography>
            </Paper>
          </Paper>
        </motion.div>
      </Container>
    </Box>
  );
};

export default LoginPage;
