import React from 'react';
import { Box, Paper, Typography, Button } from '@mui/material';
import { Error as ErrorIcon } from '@mui/icons-material';

interface ErrorBoundaryProps {
  children: React.ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error) {
    console.error('Error caught by boundary:', error);
  }

  render() {
    if (this.state.hasError) {
      return (
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '400px',
            p: 2,
          }}
        >
          <Paper
            sx={{
              p: 4,
              textAlign: 'center',
              backgroundImage: 'linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(30, 41, 59, 0.6) 100%)',
              border: '1px solid rgba(226, 232, 240, 0.1)',
              maxWidth: 500,
            }}
          >
            <ErrorIcon sx={{ fontSize: 60, color: '#EF4444', mb: 2 }} />
            <Typography variant="h5" sx={{ fontWeight: 700, mb: 2 }}>
              Something Went Wrong
            </Typography>
            <Typography variant="body2" sx={{ color: 'textSecondary', mb: 3 }}>
              {this.state.error?.message || 'An unexpected error occurred'}
            </Typography>
            <Button
              variant="contained"
              onClick={() => window.location.reload()}
              sx={{ background: 'linear-gradient(135deg, #00BFA5 0%, #4DD0C1 100%)' }}
            >
              Reload Page
            </Button>
          </Paper>
        </Box>
      );
    }

    return this.props.children;
  }
}

/**
 * Error display component for Firebase errors
 */
export const FirebaseErrorDisplay: React.FC<{ error: string | null }> = ({ error }) => {
  if (!error) return null;

  return (
    <Paper
      sx={{
        p: 2,
        mb: 2,
        backgroundColor: 'rgba(239, 68, 68, 0.1)',
        border: '1px solid rgba(239, 68, 68, 0.3)',
        borderRadius: 1,
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <ErrorIcon sx={{ color: '#EF4444', fontSize: 20 }} />
        <Typography
          variant="body2"
          sx={{
            color: '#FCA5A5',
            flex: 1,
          }}
        >
          {error}
        </Typography>
      </Box>
    </Paper>
  );
};

/**
 * Loading skeleton for data tables
 */
export const LoadingSkeleton: React.FC<{ rows?: number }> = ({ rows = 5 }) => {
  return (
    <Box sx={{ width: '100%' }}>
      {Array.from({ length: rows }).map((_, i) => (
        <Box
          key={i}
          sx={{
            display: 'flex',
            gap: 2,
            p: 2,
            borderBottom: '1px solid rgba(226, 232, 240, 0.1)',
          }}
        >
          <Box sx={{ width: '100%', height: 20, backgroundColor: 'rgba(226, 232, 240, 0.1)', borderRadius: 1 }} />
        </Box>
      ))}
    </Box>
  );
};

/**
 * Empty state component
 */
export const EmptyStateDisplay: React.FC<{ title: string; description: string }> = ({
  title,
  description,
}) => {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '300px',
        p: 4,
        textAlign: 'center',
      }}
    >
      <Typography variant="h6" sx={{ fontWeight: 600, mb: 1, color: '#CBD5E1' }}>
        {title}
      </Typography>
      <Typography variant="body2" sx={{ color: 'textSecondary' }}>
        {description}
      </Typography>
    </Box>
  );
};
