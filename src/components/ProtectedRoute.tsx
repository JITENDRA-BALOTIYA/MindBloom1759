import React, { ReactNode, useState, useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { Box, CircularProgress, Typography, Button } from '@mui/material';
import { auth } from '../firebase/config';
import { User } from 'firebase/auth';

interface ProtectedRouteProps {
  children: ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const location = useLocation();

  useEffect(() => {
    let mounted = true;
    const timeout = setTimeout(() => {
      if (mounted && loading) {
        setError('Authentication service timed out. Please check your Firebase configuration.');
        setLoading(false);
      }
    }, 10000); // 10 second timeout

    try {
      const unsubscribe = auth.onAuthStateChanged(
        (currentUser) => {
          if (mounted) {
            setUser(currentUser);
            setLoading(false);
            setError(null);
            clearTimeout(timeout);
          }
        },
        (authError) => {
          if (mounted) {
            console.error('Auth error:', authError);
            setError('Failed to initialize authentication. Please check your Internet connection and Firebase configuration.');
            setLoading(false);
            clearTimeout(timeout);
          }
        }
      );

      return () => {
        mounted = false;
        unsubscribe?.();
        clearTimeout(timeout);
      };
    } catch (err) {
      console.error('Auth setup error:', err);
      if (mounted) {
        setError('Authentication setup failed.');
        setLoading(false);
      }
    }
  }, []);

  if (loading) {
    return (
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          background: 'linear-gradient(135deg, #0F172A 0%, #1E293B 100%)',
        }}
      >
        <CircularProgress sx={{ color: '#00BFA5' }} size={50} />
      </Box>
    );
  }

  if (error) {
    return (
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          background: 'linear-gradient(135deg, #0F172A 0%, #1E293B 100%)',
          p: 2,
        }}
      >
        <Typography variant="h5" sx={{ color: '#EF4444', mb: 2, textAlign: 'center' }}>
          Authentication Error
        </Typography>
        <Typography variant="body2" sx={{ color: '#CBD5E1', mb: 3, textAlign: 'center', maxWidth: 400 }}>
          {error}
        </Typography>
        <Button 
          variant="contained" 
          onClick={() => window.location.reload()}
          sx={{ background: 'linear-gradient(135deg, #00BFA5 0%, #4DD0C1 100%)' }}
        >
          Retry
        </Button>
      </Box>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
