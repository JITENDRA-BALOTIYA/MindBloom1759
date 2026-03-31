import React, { useState } from 'react';
import {
  AppBar,
  Toolbar,
  Box,
  TextField,
  IconButton,
  Menu,
  MenuItem,
  Avatar,
  Badge,
  useMediaQuery,
  Theme,
  InputAdornment,
} from '@mui/material';
import { useTheme as useMuiTheme } from '@mui/material/styles';
import {
  Logout as LogoutIcon,
  Menu as MenuIcon,
  Search as SearchIcon,
  Notifications as NotificationsIcon,
  Brightness4 as DarkModeIcon,
  Brightness7 as LightModeIcon,
} from '@mui/icons-material';
import Logo from './Logo';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { DRAWER_WIDTH } from './Sidebar';

interface NavbarProps {
  onMobileMenuOpen: () => void;
  isDarkMode: boolean;
  onThemeToggle: () => void;
}

const Navbar: React.FC<NavbarProps> = (props: NavbarProps) => {
  const { onMobileMenuOpen, isDarkMode, onThemeToggle } = props;
  const muiTheme = useMuiTheme();
  const navigate = useNavigate();
  const isMobile = useMediaQuery((theme: Theme) => theme.breakpoints.down('md'));
  const isLight = muiTheme.palette.mode === 'light';
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [notificationAnchor, setNotificationAnchor] = useState<null | HTMLElement>(null);

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleProfileMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    localStorage.removeItem('mindbloom-remember');
    navigate('/login');
    handleProfileMenuClose();
  };

  const handleNotificationClick = (event: React.MouseEvent<HTMLElement>) => {
    setNotificationAnchor(event.currentTarget);
  };

  const handleNotificationClose = () => {
    setNotificationAnchor(null);
  };

  return (
    <AppBar
      position="fixed"
      color={isLight ? 'inherit' : 'default'}
      elevation={0}
      sx={{
        zIndex: (t) => t.zIndex.drawer + 1,
        bgcolor: isLight ? 'rgba(255, 255, 255, 0.78)' : undefined,
        backgroundImage: isLight
          ? 'linear-gradient(180deg, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.72) 100%)'
          : 'linear-gradient(135deg, rgba(15, 23, 42, 0.95) 0%, rgba(30, 41, 59, 0.9) 100%)',
        backdropFilter: isLight ? 'blur(20px) saturate(180%)' : 'blur(12px)',
        WebkitBackdropFilter: isLight ? 'blur(20px) saturate(180%)' : undefined,
        boxShadow: isLight ? '0 4px 24px rgba(15, 23, 42, 0.04), inset 0 1px 0 rgba(255,255,255,0.9)' : '0 4px 30px rgba(0, 0, 0, 0.1)',
        borderBottom: isLight ? '1px solid rgba(15, 23, 42, 0.06)' : '1px solid rgba(226, 232, 240, 0.1)',
        color: isLight ? muiTheme.palette.text.primary : muiTheme.palette.common.white,
        width: isMobile ? '100%' : `calc(100% - ${DRAWER_WIDTH}px)`,
        marginLeft: isMobile ? 0 : DRAWER_WIDTH,
      }}
    >
      <Toolbar
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: 2,
        }}
      >
        {/* Left Section */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {isMobile && (
            <IconButton color="inherit" onClick={onMobileMenuOpen}>
              <MenuIcon />
            </IconButton>
          )}
          {isMobile && <Logo size="small" />}
        </Box>

        {/* Center - Search */}
        {!isMobile && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.2 }}
            style={{ flex: 1, maxWidth: 300 }}
          >
            <TextField
              size="small"
              placeholder="Search students..."
              type="search"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: isLight ? muiTheme.palette.text.secondary : '#CBD5E1', fontSize: 20 }} />
                  </InputAdornment>
                ),
              }}
              sx={{
                width: '100%',
                '& .MuiOutlinedInput-root': {
                  backgroundColor: isLight ? 'rgba(15, 23, 42, 0.04)' : 'rgba(15, 23, 42, 0.5)',
                  borderRadius: 2,
                  '& fieldset': {
                    borderColor: isLight ? 'rgba(15, 23, 42, 0.12)' : 'rgba(226, 232, 240, 0.2)',
                  },
                  '&:hover fieldset': {
                    borderColor: 'rgba(0, 191, 165, 0.35)',
                  },
                  '&.Mui-focused fieldset': {
                    borderColor: muiTheme.palette.primary.main,
                  },
                },
              }}
            />
          </motion.div>
        )}

        {/* Right Section */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          {/* Notifications */}
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: 'spring', stiffness: 100 }}
          >
            <IconButton
              color="inherit"
              onClick={handleNotificationClick}
              sx={{
                '&:hover': {
                  backgroundColor: 'rgba(0, 191, 165, 0.1)',
                },
              }}
            >
              <Badge color="error" variant="dot">
                <NotificationsIcon />
              </Badge>
            </IconButton>
          </motion.div>

          {/* Theme Toggle */}
          <IconButton
            color="inherit"
            onClick={onThemeToggle}
            sx={{
              '&:hover': {
                backgroundColor: 'rgba(0, 191, 165, 0.1)',
              },
            }}
          >
            {isDarkMode ? <LightModeIcon /> : <DarkModeIcon />}
          </IconButton>

          {/* User Avatar */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            <IconButton
              onClick={handleProfileMenuOpen}
              sx={{
                p: 0,
                '&:hover': {
                  backgroundColor: 'rgba(0, 191, 165, 0.1)',
                },
              }}
            >
              <Avatar
                sx={{
                  background: 'linear-gradient(135deg, #00BFA5 0%, #7C3AED 100%)',
                  width: 36,
                  height: 36,
                  fontSize: '1.2rem',
                  fontWeight: 700,
                }}
              >
                AD
              </Avatar>
            </IconButton>
          </motion.div>
        </Box>
      </Toolbar>

      {/* Profile Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleProfileMenuClose}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        PaperProps={{
          sx: {
            bgcolor: isLight ? 'background.paper' : undefined,
            backgroundImage: isLight ? 'none' : 'linear-gradient(135deg, rgba(30, 41, 59, 0.95) 0%, rgba(30, 41, 59, 0.85) 100%)',
            backdropFilter: 'blur(10px)',
            border: isLight ? '1px solid' : '1px solid rgba(226, 232, 240, 0.1)',
            borderColor: isLight ? 'divider' : undefined,
            boxShadow: isLight ? 2 : '0 8px 32px rgba(0, 0, 0, 0.2)',
          },
        }}
      >
        <MenuItem disabled>👤 Admin User</MenuItem>
        <MenuItem disabled>admin@mindbloom.com</MenuItem>
        <MenuItem
          onClick={handleLogout}
          sx={{
            color: '#EF4444',
            '&:hover': {
              backgroundColor: 'rgba(239, 68, 68, 0.1)',
            },
          }}
        >
          <LogoutIcon sx={{ mr: 1, fontSize: 18 }} />
          Logout
        </MenuItem>
      </Menu>

      {/* Notifications Menu */}
      <Menu
        anchorEl={notificationAnchor}
        open={Boolean(notificationAnchor)}
        onClose={handleNotificationClose}
        PaperProps={{
          sx: {
            bgcolor: isLight ? 'background.paper' : undefined,
            backgroundImage: isLight ? 'none' : 'linear-gradient(135deg, rgba(30, 41, 59, 0.95) 0%, rgba(30, 41, 59, 0.85) 100%)',
            backdropFilter: 'blur(10px)',
            border: isLight ? '1px solid' : '1px solid rgba(226, 232, 240, 0.1)',
            borderColor: isLight ? 'divider' : undefined,
            boxShadow: isLight ? 2 : '0 8px 32px rgba(0, 0, 0, 0.2)',
            minWidth: 300,
          },
        }}
      >
        <MenuItem>Review Alerts for medium/high risk students</MenuItem>
        <MenuItem>Weekly reports compute from live Firestore users</MenuItem>
        <MenuItem>Toggle theme with the sun/moon icon</MenuItem>
      </Menu>
    </AppBar>
  );
};

export default Navbar;
