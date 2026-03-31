import React from 'react';
import {
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Box,
  useMediaQuery,
  Theme,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import {
  Dashboard as DashboardIcon,
  PeopleAlt as StudentsIcon,
  EventNote as AttendanceIcon,
  BarChart as AnalyticsIcon,
  WarningAmber as AlertsIcon,
  Assessment as ReportIcon,
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import Logo from './Logo';

const DRAWER_WIDTH = 280;

interface SidebarProps {
  onMobileDrawerClose?: () => void;
  mobileOpen?: boolean;
}

const Sidebar: React.FC<SidebarProps> = ({ onMobileDrawerClose, mobileOpen = false }) => {
  const theme = useTheme();
  const navigate = useNavigate();
  const location = useLocation();
  const isMobile = useMediaQuery((t: Theme) => t.breakpoints.down('md'));
  const isLight = theme.palette.mode === 'light';
  const bg = isLight ? 'rgba(255, 255, 255, 0.72)' : '#0B1220';
  const border = isLight ? '1px solid rgba(255, 255, 255, 0.85)' : '1px solid rgba(148, 163, 184, 0.12)';
  const inactive = isLight ? theme.palette.text.secondary : '#CBD5E1';
  const active = theme.palette.primary.main;

  const menuItems = [
    { label: 'Dashboard', icon: DashboardIcon, path: '/dashboard' },
    { label: 'Students', icon: StudentsIcon, path: '/students' },
    { label: 'Attendance', icon: AttendanceIcon, path: '/attendance' },
    { label: 'Analytics', icon: AnalyticsIcon, path: '/analytics' },
    { label: 'Weekly Report', icon: ReportIcon, path: '/weekly-report' },
    { label: 'Alerts', icon: AlertsIcon, path: '/alerts' },
  ];

  const handleNavigate = (path: string) => {
    navigate(path);
    if (isMobile && onMobileDrawerClose) {
      onMobileDrawerClose();
    }
  };

  const drawerContent = (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        backgroundColor: bg,
        backdropFilter: isLight ? 'blur(20px) saturate(170%)' : undefined,
        WebkitBackdropFilter: isLight ? 'blur(20px) saturate(170%)' : undefined,
        boxShadow: isLight ? 'inset -1px 0 0 rgba(255,255,255,0.5)' : undefined,
      }}
    >
      <Box sx={{ p: 3, borderBottom: border }}>
        <Logo size="medium" />
      </Box>

      <List sx={{ flex: 1, py: 2 }}>
        {menuItems.map((item, index) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.path;

          return (
            <motion.div
              key={item.path}
              initial={{ opacity: 0, x: -12 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: index * 0.04 }}
            >
              <ListItem
                button
                onClick={() => handleNavigate(item.path)}
                sx={{
                  mx: 1,
                  mb: 0.75,
                  borderRadius: '14px',
                  backgroundColor: isActive
                    ? isLight
                      ? 'linear-gradient(135deg, rgba(13, 148, 136, 0.18) 0%, rgba(59, 130, 246, 0.08) 100%)'
                      : 'rgba(45, 212, 191, 0.15)'
                    : 'transparent',
                  borderLeft: isActive ? `3px solid ${active}` : '3px solid transparent',
                  transition: 'all 0.28s cubic-bezier(0.22, 1, 0.36, 1)',
                  cursor: 'pointer',
                  pl: isActive ? 2 : 2.5,
                  boxShadow: isActive && isLight ? '0 4px 14px rgba(13, 148, 136, 0.12)' : 'none',
                  '&:hover': {
                    backgroundColor: isLight ? 'rgba(59, 130, 246, 0.06)' : 'rgba(45, 212, 191, 0.1)',
                    transform: 'translateX(4px)',
                  },
                }}
              >
                <ListItemIcon sx={{ minWidth: 40, color: isActive ? active : inactive }}>
                  <Icon fontSize="small" />
                </ListItemIcon>
                <ListItemText
                  primary={item.label}
                  primaryTypographyProps={{
                    variant: 'body2',
                    fontWeight: isActive ? 600 : 500,
                    color: isActive ? active : inactive,
                  }}
                />
              </ListItem>
            </motion.div>
          );
        })}
      </List>

      <Box
        sx={{
          p: 2,
          borderTop: border,
          textAlign: 'center',
          color: theme.palette.text.disabled,
          fontSize: '0.75rem',
        }}
      >
        <div>MindBloom Admin v1.0</div>
        <div>© 2026</div>
      </Box>
    </Box>
  );

  return (
    <>
      {isMobile && (
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={onMobileDrawerClose}
          sx={{
            '& .MuiDrawer-paper': {
              width: DRAWER_WIDTH,
              boxSizing: 'border-box',
              backgroundColor: bg,
              borderRight: border,
              boxShadow: isLight ? '4px 0 32px rgba(15, 23, 42, 0.08)' : undefined,
            },
          }}
        >
          {drawerContent}
        </Drawer>
      )}

      {!isMobile && (
        <Drawer
          variant="permanent"
          sx={{
            width: DRAWER_WIDTH,
            flexShrink: 0,
            '& .MuiDrawer-paper': {
              width: DRAWER_WIDTH,
              boxSizing: 'border-box',
              backgroundColor: bg,
              borderRight: border,
              boxShadow: isLight ? '4px 0 32px rgba(15, 23, 42, 0.06)' : undefined,
            },
          }}
        >
          {drawerContent}
        </Drawer>
      )}
    </>
  );
};

export default Sidebar;
export { DRAWER_WIDTH };
