import React, { useState, useEffect, ErrorInfo, ReactNode } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Box, useMediaQuery, Theme } from '@mui/material';
import { Toaster } from 'react-hot-toast';
import { motion, AnimatePresence } from 'framer-motion';

import { useTheme } from '@hooks/index';

// Components
import Navbar from '@components/Navbar';
import Sidebar, { DRAWER_WIDTH } from '@components/Sidebar';
import ProtectedRoute from '@components/ProtectedRoute';

// Pages
import LoginPage from '@pages/LoginPage';
import DashboardPage from '@pages/DashboardPage';
import StudentsPage from '@pages/StudentsPage';
import AttendancePage from '@pages/AttendancePage';
import AnalyticsPage from '@pages/AnalyticsPage';
import AlertsPage from '@pages/AlertsPage';
import WeeklyReportPage from '@pages/WeeklyReportPage';

type PageName = 'dashboard' | 'students' | 'attendance' | 'analytics' | 'alerts';

// Error Boundary Component
interface ErrorBoundaryProps {
  children: ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '100vh',
            background: 'linear-gradient(135deg, #0F172A 0%, #1E293B 100%)',
            flexDirection: 'column',
            p: 2,
          }}
        >
          <h1 style={{ color: '#E2E8F0' }}>Something went wrong</h1>
          <p style={{ color: '#CBD5E1' }}>Please try refreshing the page</p>
          <button
            onClick={() => window.location.reload()}
            style={{
              marginTop: '20px',
              padding: '10px 20px',
              background: '#00BFA5',
              color: '#fff',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '16px',
            }}
          >
            Refresh Page
          </button>
        </Box>
      );
    }

    return this.props.children;
  }
}

const AppContent: React.FC = () => {
  const { isDarkMode, toggleTheme } = useTheme();
  const [mobileOpen, setMobileOpen] = useState(false);
  const isMobile = useMediaQuery((theme: Theme) => theme.breakpoints.down('md'));
  const [currentPage, setCurrentPage] = useState<PageName>('dashboard');

  const handleMobileMenuOpen = () => {
    setMobileOpen(true);
  };

  const handleMobileMenuClose = () => {
    setMobileOpen(false);
  };

  const pageVariants = {
    initial: { opacity: 0, y: 20 },
    animate: { opacity: 1, y: 0 },
    exit: { opacity: 0, y: -20 },
  };

  const pageTransition = {
    duration: 0.42,
    ease: [0.22, 1, 0.36, 1],
  };

  return (
    <>
      <Toaster
        position="top-right"
        reverseOrder={false}
        gutter={8}
        toastOptions={{
          duration: 3000,
          style: {
            background: isDarkMode ? '#1E293B' : '#FFFFFF',
            color: isDarkMode ? '#E2E8F0' : '#0F172A',
            borderRadius: '10px',
            border: isDarkMode ? '1px solid rgba(226, 232, 240, 0.12)' : '1px solid rgba(15, 23, 42, 0.12)',
            boxShadow: isDarkMode ? '0 8px 24px rgba(0,0,0,0.2)' : '0 8px 24px rgba(15,23,42,0.08)',
          },
        }}
      />
      <Router>
        <Routes>
          {/* Login Route */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected Dashboard Routes */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Box sx={{ display: 'flex', minHeight: '100vh' }}>
                  <Sidebar onMobileDrawerClose={handleMobileMenuClose} mobileOpen={mobileOpen} />
                  <Box
                    sx={{
                      width: isMobile ? '100%' : `calc(100% - ${DRAWER_WIDTH}px)`,
                      display: 'flex',
                      flexDirection: 'column',
                    }}
                  >
                    <Navbar
                      onMobileMenuOpen={handleMobileMenuOpen}
                      isDarkMode={isDarkMode}
                      onThemeToggle={toggleTheme}
                    />
                    <Box
                      component="main"
                      sx={{
                        width: '100%',
                        mt: 8,
                        mb: 2,
                        overflowY: 'auto',
                      }}
                    >
                      <AnimatePresence mode="wait">
                        <motion.div
                          key="dashboard"
                          variants={pageVariants}
                          initial="initial"
                          animate="animate"
                          exit="exit"
                          transition={pageTransition}
                        >
                          <DashboardPage />
                        </motion.div>
                      </AnimatePresence>
                    </Box>
                  </Box>
                </Box>
              </ProtectedRoute>
            }
          />

          {/* Students Route */}
          <Route
            path="/students"
            element={
              <ProtectedRoute>
                <Box sx={{ display: 'flex', minHeight: '100vh' }}>
                  <Sidebar onMobileDrawerClose={handleMobileMenuClose} mobileOpen={mobileOpen} />
                  <Box
                    sx={{
                      width: isMobile ? '100%' : `calc(100% - ${DRAWER_WIDTH}px)`,
                      display: 'flex',
                      flexDirection: 'column',
                    }}
                  >
                    <Navbar
                      onMobileMenuOpen={handleMobileMenuOpen}
                      isDarkMode={isDarkMode}
                      onThemeToggle={toggleTheme}
                    />
                    <Box
                      component="main"
                      sx={{
                        width: '100%',
                        mt: 8,
                        mb: 2,
                        overflowY: 'auto',
                      }}
                    >
                      <AnimatePresence mode="wait">
                        <motion.div
                          key="students"
                          variants={pageVariants}
                          initial="initial"
                          animate="animate"
                          exit="exit"
                          transition={pageTransition}
                        >
                          <StudentsPage />
                        </motion.div>
                      </AnimatePresence>
                    </Box>
                  </Box>
                </Box>
              </ProtectedRoute>
            }
          />

          {/* Attendance Route */}
          <Route
            path="/attendance"
            element={
              <ProtectedRoute>
                <Box sx={{ display: 'flex', minHeight: '100vh' }}>
                  <Sidebar onMobileDrawerClose={handleMobileMenuClose} mobileOpen={mobileOpen} />
                  <Box
                    sx={{
                      width: isMobile ? '100%' : `calc(100% - ${DRAWER_WIDTH}px)`,
                      display: 'flex',
                      flexDirection: 'column',
                    }}
                  >
                    <Navbar
                      onMobileMenuOpen={handleMobileMenuOpen}
                      isDarkMode={isDarkMode}
                      onThemeToggle={toggleTheme}
                    />
                    <Box
                      component="main"
                      sx={{
                        width: '100%',
                        mt: 8,
                        mb: 2,
                        overflowY: 'auto',
                      }}
                    >
                      <AnimatePresence mode="wait">
                        <motion.div
                          key="attendance"
                          variants={pageVariants}
                          initial="initial"
                          animate="animate"
                          exit="exit"
                          transition={pageTransition}
                        >
                          <AttendancePage />
                        </motion.div>
                      </AnimatePresence>
                    </Box>
                  </Box>
                </Box>
              </ProtectedRoute>
            }
          />

          {/* Analytics Route */}
          <Route
            path="/analytics"
            element={
              <ProtectedRoute>
                <Box sx={{ display: 'flex', minHeight: '100vh' }}>
                  <Sidebar onMobileDrawerClose={handleMobileMenuClose} mobileOpen={mobileOpen} />
                  <Box
                    sx={{
                      width: isMobile ? '100%' : `calc(100% - ${DRAWER_WIDTH}px)`,
                      display: 'flex',
                      flexDirection: 'column',
                    }}
                  >
                    <Navbar
                      onMobileMenuOpen={handleMobileMenuOpen}
                      isDarkMode={isDarkMode}
                      onThemeToggle={toggleTheme}
                    />
                    <Box
                      component="main"
                      sx={{
                        width: '100%',
                        mt: 8,
                        mb: 2,
                        overflowY: 'auto',
                      }}
                    >
                      <AnimatePresence mode="wait">
                        <motion.div
                          key="analytics"
                          variants={pageVariants}
                          initial="initial"
                          animate="animate"
                          exit="exit"
                          transition={pageTransition}
                        >
                          <AnalyticsPage />
                        </motion.div>
                      </AnimatePresence>
                    </Box>
                  </Box>
                </Box>
              </ProtectedRoute>
            }
          />

          {/* Weekly Report Route */}
          <Route
            path="/weekly-report"
            element={
              <ProtectedRoute>
                <Box sx={{ display: 'flex', minHeight: '100vh' }}>
                  <Sidebar onMobileDrawerClose={handleMobileMenuClose} mobileOpen={mobileOpen} />
                  <Box
                    sx={{
                      width: isMobile ? '100%' : `calc(100% - ${DRAWER_WIDTH}px)`,
                      display: 'flex',
                      flexDirection: 'column',
                    }}
                  >
                    <Navbar
                      onMobileMenuOpen={handleMobileMenuOpen}
                      isDarkMode={isDarkMode}
                      onThemeToggle={toggleTheme}
                    />
                    <Box
                      component="main"
                      sx={{
                        width: '100%',
                        mt: 8,
                        mb: 2,
                        overflowY: 'auto',
                      }}
                    >
                      <AnimatePresence mode="wait">
                        <motion.div
                          key="weekly-report"
                          variants={pageVariants}
                          initial="initial"
                          animate="animate"
                          exit="exit"
                          transition={pageTransition}
                        >
                          <WeeklyReportPage />
                        </motion.div>
                      </AnimatePresence>
                    </Box>
                  </Box>
                </Box>
              </ProtectedRoute>
            }
          />

          {/* Alerts Route */}
          <Route
            path="/alerts"
            element={
              <ProtectedRoute>
                <Box sx={{ display: 'flex', minHeight: '100vh' }}>
                  <Sidebar onMobileDrawerClose={handleMobileMenuClose} mobileOpen={mobileOpen} />
                  <Box
                    sx={{
                      width: isMobile ? '100%' : `calc(100% - ${DRAWER_WIDTH}px)`,
                      display: 'flex',
                      flexDirection: 'column',
                    }}
                  >
                    <Navbar
                      onMobileMenuOpen={handleMobileMenuOpen}
                      isDarkMode={isDarkMode}
                      onThemeToggle={toggleTheme}
                    />
                    <Box
                      component="main"
                      sx={{
                        width: '100%',
                        mt: 8,
                        mb: 2,
                        overflowY: 'auto',
                      }}
                    >
                      <AnimatePresence mode="wait">
                        <motion.div
                          key="alerts"
                          variants={pageVariants}
                          initial="initial"
                          animate="animate"
                          exit="exit"
                          transition={pageTransition}
                        >
                          <AlertsPage />
                        </motion.div>
                      </AnimatePresence>
                    </Box>
                  </Box>
                </Box>
              </ProtectedRoute>
            }
          />

          {/* Default redirect */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Router>
    </>
  );
};

const App: React.FC = () => {
  return (
    <ErrorBoundary>
      <AppContent />
    </ErrorBoundary>
  );
};

export default App;
