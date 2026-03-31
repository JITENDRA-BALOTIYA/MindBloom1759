import { createTheme } from '@mui/material/styles';

const fontHeading = '"Plus Jakarta Sans", "Inter", system-ui, sans-serif';
const fontBody = '"Inter", system-ui, sans-serif';

/** Glass surface (light) */
export const glassLight = {
  background: 'rgba(255, 255, 255, 0.72)',
  backdropFilter: 'blur(20px) saturate(180%)',
  WebkitBackdropFilter: 'blur(20px) saturate(180%)',
  border: '1px solid rgba(255, 255, 255, 0.85)',
  boxShadow: '0 4px 24px rgba(15, 23, 42, 0.06), 0 1px 2px rgba(15, 23, 42, 0.04)',
};

const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: '#2DD4BF', light: '#5EEAD4', dark: '#14B8A6', contrastText: '#042F2E' },
    secondary: { main: '#818CF8', light: '#A5B4FC', dark: '#6366F1', contrastText: '#fff' },
    success: { main: '#34D399', light: '#6EE7B7', dark: '#10B981' },
    warning: { main: '#FBBF24', light: '#FCD34D', dark: '#D97706' },
    error: { main: '#F87171', light: '#FCA5A5', dark: '#EF4444' },
    info: { main: '#60A5FA', light: '#93C5FD', dark: '#3B82F6' },
    background: { default: '#0B1220', paper: '#131B2E' },
    text: { primary: '#F1F5F9', secondary: '#94A3B8' },
    divider: 'rgba(148, 163, 184, 0.12)',
  },
  typography: {
    fontFamily: fontBody,
    h1: { fontFamily: fontHeading, fontWeight: 800, letterSpacing: '-0.03em' },
    h2: { fontFamily: fontHeading, fontWeight: 800, letterSpacing: '-0.02em' },
    h3: { fontFamily: fontHeading, fontWeight: 700, letterSpacing: '-0.02em' },
    h4: { fontFamily: fontHeading, fontWeight: 700, letterSpacing: '-0.02em' },
    h5: { fontFamily: fontHeading, fontWeight: 600 },
    h6: { fontFamily: fontHeading, fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600, letterSpacing: '0.01em' },
  },
  shape: { borderRadius: 16 },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 14,
          padding: '10px 22px',
          transition: 'transform 0.2s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.2s ease',
          '&:hover': { transform: 'translateY(-1px)' },
        },
        containedPrimary: {
          background: 'linear-gradient(135deg, #2DD4BF 0%, #0D9488 100%)',
          boxShadow: '0 4px 14px rgba(13, 148, 136, 0.35)',
          '&:hover': {
            background: 'linear-gradient(135deg, #5EEAD4 0%, #0F766E 100%)',
            boxShadow: '0 8px 22px rgba(13, 148, 136, 0.4)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          background: 'linear-gradient(145deg, rgba(30, 41, 59, 0.9) 0%, rgba(15, 23, 42, 0.85) 100%)',
          backdropFilter: 'blur(12px)',
          border: '1px solid rgba(148, 163, 184, 0.12)',
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.25)',
          transition: 'box-shadow 0.3s ease, border-color 0.3s ease, transform 0.3s ease',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 14,
          transition: 'box-shadow 0.2s ease, border-color 0.2s ease',
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
            borderWidth: '1.5px',
            boxShadow: '0 0 0 4px rgba(45, 212, 191, 0.15)',
          },
        },
      },
    },
    MuiSkeleton: {
      styleOverrides: {
        root: { borderRadius: 12 },
      },
    },
  },
});

const lightTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#0D9488',
      light: '#14B8A6',
      dark: '#0F766E',
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#6366F1',
      light: '#818CF8',
      dark: '#4F46E5',
      contrastText: '#FFFFFF',
    },
    info: {
      main: '#3B82F6',
      light: '#60A5FA',
      dark: '#2563EB',
      contrastText: '#FFFFFF',
    },
    success: { main: '#059669', light: '#10B981', dark: '#047857' },
    warning: { main: '#D97706', light: '#F59E0B', dark: '#B45309' },
    error: { main: '#DC2626', light: '#EF4444', dark: '#B91C1C' },
    background: {
      default: '#F1F5F9',
      paper: '#FFFFFF',
    },
    text: {
      primary: '#0F172A',
      secondary: '#64748B',
    },
    divider: 'rgba(15, 23, 42, 0.08)',
  },
  typography: {
    fontFamily: fontBody,
    h1: { fontFamily: fontHeading, fontWeight: 800, letterSpacing: '-0.035em', fontSize: '2.25rem' },
    h2: { fontFamily: fontHeading, fontWeight: 800, letterSpacing: '-0.03em', fontSize: '1.875rem' },
    h3: { fontFamily: fontHeading, fontWeight: 700, letterSpacing: '-0.025em', fontSize: '1.5rem' },
    h4: { fontFamily: fontHeading, fontWeight: 700, letterSpacing: '-0.02em', fontSize: '1.35rem' },
    h5: { fontFamily: fontHeading, fontWeight: 600, letterSpacing: '-0.015em' },
    h6: { fontFamily: fontHeading, fontWeight: 600 },
    body1: { lineHeight: 1.6 },
    body2: { lineHeight: 1.55 },
    button: { textTransform: 'none', fontWeight: 600, letterSpacing: '0.02em' },
  },
  shape: { borderRadius: 18 },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          fontFeatureSettings: '"cv02", "cv03"',
        },
      },
    },
    MuiButton: {
      defaultProps: { disableElevation: false },
      styleOverrides: {
        root: {
          borderRadius: 14,
          padding: '10px 22px',
          fontWeight: 600,
          transition:
            'transform 0.22s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.22s ease, background 0.22s ease',
          '&:hover': { transform: 'translateY(-2px)' },
          '&:active': { transform: 'translateY(0)' },
        },
        containedPrimary: {
          background: 'linear-gradient(135deg, #2DD4BF 0%, #0D9488 45%, #0F766E 100%)',
          boxShadow: '0 4px 16px rgba(13, 148, 136, 0.28)',
          '&:hover': {
            background: 'linear-gradient(135deg, #5EEAD4 0%, #14B8A6 50%, #0D9488 100%)',
            boxShadow: '0 8px 24px rgba(13, 148, 136, 0.35)',
          },
        },
        outlined: {
          borderWidth: '1.5px',
          '&:hover': { borderWidth: '1.5px' },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          ...glassLight,
          borderRadius: 20,
          transition: 'transform 0.35s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.35s ease',
          '&:hover': {
            boxShadow: '0 12px 40px rgba(15, 23, 42, 0.08), 0 4px 12px rgba(59, 130, 246, 0.06)',
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: ({ ownerState }) => ({
          ...(ownerState.variant === 'outlined' && ownerState.elevation === 0
            ? {
                background: 'rgba(255, 255, 255, 0.65)',
                backdropFilter: 'blur(16px)',
                border: '1px solid rgba(255, 255, 255, 0.9)',
                boxShadow: '0 4px 24px rgba(15, 23, 42, 0.05)',
              }
            : {}),
        }),
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          boxShadow: 'none',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {},
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: 24,
          background: 'rgba(255, 255, 255, 0.92)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(255, 255, 255, 0.95)',
          boxShadow: '0 24px 64px rgba(15, 23, 42, 0.12), 0 8px 24px rgba(59, 130, 246, 0.06)',
        },
      },
    },
    MuiTextField: {
      defaultProps: { variant: 'outlined' },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 14,
          backgroundColor: 'rgba(255, 255, 255, 0.85)',
          transition: 'box-shadow 0.2s ease, border-color 0.2s ease, background-color 0.2s ease',
          '&:hover .MuiOutlinedInput-notchedOutline': {
            borderColor: 'rgba(13, 148, 136, 0.35)',
          },
          '&.Mui-focused': {
            backgroundColor: '#FFFFFF',
            boxShadow: '0 0 0 4px rgba(59, 130, 246, 0.12), 0 4px 16px rgba(13, 148, 136, 0.08)',
          },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
            borderWidth: '1.5px',
            borderColor: '#0D9488',
          },
        },
      },
    },
    MuiInputLabel: {
      styleOverrides: {
        root: {
          fontWeight: 500,
          '&.Mui-focused': { fontWeight: 600 },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { borderRadius: 10, fontWeight: 600 },
      },
    },
    MuiSkeleton: {
      styleOverrides: {
        root: {
          borderRadius: 14,
          backgroundColor: 'rgba(15, 23, 42, 0.06)',
        },
        rounded: { borderRadius: 18 },
      },
    },
  },
});

export { darkTheme, lightTheme };
