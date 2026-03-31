import React, { ReactNode, useMemo } from 'react';
import { ThemeProvider } from '@mui/material/styles';
import { Box, CssBaseline } from '@mui/material';
import { useTheme } from '@hooks/index';
import { darkTheme, lightTheme } from './theme';

interface ThemeWrapperProps {
  children: ReactNode;
}

export const ThemeWrapper: React.FC<ThemeWrapperProps> = ({ children }) => {
  const { isDarkMode } = useTheme();

  const theme = useMemo(() => {
    return isDarkMode ? darkTheme : lightTheme;
  }, [isDarkMode]);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box
        sx={{
          minHeight: '100vh',
          ...(isDarkMode
            ? { bgcolor: 'background.default' }
            : {
                background:
                  'linear-gradient(165deg, #EEF4FF 0%, #ECFDFB 28%, #F8FAFC 55%, #F1F5F9 100%)',
                backgroundAttachment: 'fixed',
              }),
        }}
      >
        {children}
      </Box>
    </ThemeProvider>
  );
};

export default ThemeWrapper;
