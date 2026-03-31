import React from 'react';
import { Card, CardContent, Box, Typography, Skeleton, useTheme } from '@mui/material';
import { motion } from 'framer-motion';
import { StatCard as StatCardType } from '../types/index';
import { AnimatedNumber } from './AnimatedNumber';

interface StatCardComponentProps extends StatCardType {
  loading?: boolean;
  delay?: number;
}

function hexToRgbChannels(hex: string): string {
  const h = hex.replace('#', '');
  if (h.length !== 6) return '13, 148, 136';
  const r = parseInt(h.slice(0, 2), 16);
  const g = parseInt(h.slice(2, 4), 16);
  const b = parseInt(h.slice(4, 6), 16);
  return `${r}, ${g}, ${b}`;
}

const StatCardComponent: React.FC<StatCardComponentProps> = (props) => {
  const { title, value, icon, color, trend, unit, loading = false, delay = 0 } = props;
  const theme = useTheme();
  const isLight = theme.palette.mode === 'light';
  const rgb = hexToRgbChannels(color);

  const glass = isLight
    ? {
        background: `linear-gradient(135deg, rgba(${rgb}, 0.12) 0%, rgba(255, 255, 255, 0.75) 50%, rgba(59, 130, 246, 0.06) 100%)`,
        border: `1px solid rgba(${rgb}, 0.22)`,
        boxShadow: `0 4px 24px rgba(${rgb}, 0.08), 0 1px 2px rgba(15, 23, 42, 0.04)`,
      }
    : {
        background: `linear-gradient(135deg, rgba(${rgb}, 0.18) 0%, rgba(15, 23, 42, 0.6) 100%)`,
        border: `1px solid rgba(${rgb}, 0.25)`,
        boxShadow: `0 8px 32px rgba(0,0,0,0.2)`,
      };

  return (
    <motion.div
      initial={{ opacity: 0, y: 22 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay, duration: 0.45, ease: [0.22, 1, 0.36, 1] }}
      whileHover={{ y: -6, transition: { duration: 0.25 } }}
      style={{ height: '100%' }}
    >
      <Card
        sx={{
          ...glass,
          height: '100%',
          position: 'relative',
          overflow: 'hidden',
          borderRadius: '22px',
          backdropFilter: isLight ? 'blur(16px) saturate(160%)' : 'blur(12px)',
          WebkitBackdropFilter: isLight ? 'blur(16px) saturate(160%)' : 'blur(12px)',
          transition: 'box-shadow 0.35s cubic-bezier(0.22, 1, 0.36, 1), transform 0.35s ease',
          '&:hover': {
            boxShadow: isLight
              ? `0 16px 48px rgba(${rgb}, 0.14), 0 8px 20px rgba(59, 130, 246, 0.08)`
              : `0 16px 48px rgba(${rgb}, 0.2)`,
          },
          '&::before': {
            content: '""',
            position: 'absolute',
            top: '-40%',
            right: '-30%',
            width: '70%',
            height: '70%',
            borderRadius: '50%',
            background: `radial-gradient(circle, rgba(${rgb}, 0.25) 0%, transparent 65%)`,
            pointerEvents: 'none',
          },
        }}
      >
        <CardContent sx={{ pt: 2.5, pb: 2, px: 2.5, position: 'relative', zIndex: 1 }}>
          <Box
            sx={{
              mb: 2,
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: 56,
              height: 56,
              borderRadius: '16px',
              background: `linear-gradient(145deg, rgba(${rgb}, 0.2) 0%, rgba(${rgb}, 0.08) 100%)`,
              border: `1px solid rgba(${rgb}, 0.28)`,
              fontSize: 28,
              boxShadow: `inset 0 1px 0 rgba(255,255,255,0.35)`,
            }}
          >
            {icon}
          </Box>

          <Typography
            color="text.secondary"
            variant="body2"
            sx={{ mb: 0.75, fontWeight: 600, fontSize: '0.8125rem', letterSpacing: '0.04em', textTransform: 'uppercase' }}
          >
            {title}
          </Typography>

          <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 0.75, flexWrap: 'wrap' }}>
            {loading ? (
              <Skeleton width={100} height={40} sx={{ borderRadius: 2 }} />
            ) : typeof value === 'number' ? (
              <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 0.5 }}>
                <Typography
                  component="span"
                  variant="h4"
                  sx={{
                    fontFamily: theme.typography.h4.fontFamily,
                    fontWeight: 800,
                    letterSpacing: '-0.03em',
                    background: isLight
                      ? `linear-gradient(135deg, ${color} 0%, #0F172A 120%)`
                      : 'none',
                    backgroundClip: isLight ? 'text' : undefined,
                    WebkitBackgroundClip: isLight ? 'text' : undefined,
                    WebkitTextFillColor: isLight ? 'transparent' : undefined,
                    color: isLight ? undefined : color,
                  }}
                >
                  <AnimatedNumber value={value} />
                </Typography>
                {unit ? (
                  <Typography component="span" variant="h6" color="text.secondary" sx={{ fontWeight: 600 }}>
                    {unit}
                  </Typography>
                ) : null}
              </Box>
            ) : (
              <Typography variant="h4" sx={{ fontWeight: 800, color }}>
                {value}
                {unit ? (
                  <Typography component="span" variant="h6" color="text.secondary" sx={{ fontWeight: 600, ml: 0.5 }}>
                    {unit}
                  </Typography>
                ) : null}
              </Typography>
            )}
          </Box>

          {trend !== undefined && !loading && (
            <Typography
              variant="caption"
              sx={{
                mt: 1.25,
                display: 'block',
                fontWeight: 700,
                color: trend > 0 ? 'success.main' : 'error.main',
              }}
            >
              {trend > 0 ? '↑' : '↓'} {Math.abs(trend)}% vs last week
            </Typography>
          )}
        </CardContent>
      </Card>
    </motion.div>
  );
};

export default StatCardComponent;
