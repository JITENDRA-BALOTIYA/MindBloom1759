import React from 'react';
import { Box, Typography } from '@mui/material';

interface LogoProps {
  size?: 'small' | 'medium' | 'large';
}

const Logo: React.FC<LogoProps> = (props: any) => {
  const { size = 'medium' } = props;
  const sizeMap: any = {
    small: { icon: 24, text: 16 },
    medium: { icon: 32, text: 20 },
    large: { icon: 48, text: 28 },
  };

  const dims = sizeMap[size];

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: size === 'large' ? 2 : 1,
        justifyContent: size === 'large' ? 'center' : 'flex-start',
      }}
    >
      <Box
        sx={{
          width: dims.icon,
          height: dims.icon,
          background: 'linear-gradient(135deg, #00BFA5 0%, #7C3AED 100%)',
          borderRadius: '10px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: dims.icon * 0.6,
          fontWeight: 700,
          color: '#fff',
        }}
      >
        🧠
      </Box>
      <Typography
        variant="h6"
        sx={{
          fontFamily: '"Plus Jakarta Sans", "Inter", sans-serif',
          fontWeight: 800,
          letterSpacing: '-0.03em',
          background: 'linear-gradient(135deg, #0D9488 0%, #3B82F6 50%, #6366F1 100%)',
          backgroundClip: 'text',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          fontSize: dims.text,
          margin: 0,
        }}
      >
        MindBloom
      </Typography>
    </Box>
  );
};

export default Logo;
