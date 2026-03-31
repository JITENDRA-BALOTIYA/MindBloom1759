import React from 'react';
import { Box, Card, CardContent, CardHeader, Skeleton, useTheme } from '@mui/material';
import { Line, Doughnut, Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import { motion } from 'framer-motion';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

interface ChartCardProps {
  title: string;
  data: any;
  type: 'line' | 'doughnut' | 'bar' | 'area';
  loading?: boolean;
  height?: number;
  options?: any;
  delay?: number;
}

const ChartCard: React.FC<ChartCardProps> = ({
  title,
  data,
  type,
  loading = false,
  height = 300,
  options = {},
  delay = 0,
}) => {
  const theme = useTheme();
  const isLight = theme.palette.mode === 'light';
  const tick = isLight ? theme.palette.text.secondary : '#94A3B8';
  const grid = isLight ? 'rgba(15, 23, 42, 0.06)' : 'rgba(148, 163, 184, 0.12)';
  const legend = isLight ? theme.palette.text.primary : '#F1F5F9';

  const defaultOptions = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 680 },
    plugins: {
      legend: {
        labels: {
          color: legend,
          font: { family: '"Inter", sans-serif', weight: 500, size: 12 },
          usePointStyle: true,
          padding: 16,
        },
      },
      tooltip: {
        backgroundColor: isLight ? 'rgba(15, 23, 42, 0.94)' : 'rgba(15, 23, 42, 0.95)',
        titleColor: '#F8FAFC',
        bodyColor: '#E2E8F0',
        borderColor: 'rgba(13, 148, 136, 0.45)',
        borderWidth: 1,
        padding: 14,
        cornerRadius: 12,
        titleFont: { weight: 700, size: 13 },
        bodyFont: { size: 12 },
      },
    },
    scales: {
      x: {
        grid: { color: grid, drawBorder: false },
        ticks: { color: tick, font: { size: 11 } },
      },
      y: {
        grid: { color: grid, drawBorder: false },
        ticks: { color: tick, font: { size: 11 } },
      },
    },
  };

  const chartOptions = { ...defaultOptions, ...options };

  const renderChart = () => {
    if (loading) {
      return <Skeleton variant="rounded" width="100%" height={height} className="mindbloom-shimmer" animation={false} />;
    }

    switch (type) {
      case 'line':
        return <Line data={data} options={chartOptions} height={height} />;
      case 'doughnut':
        return <Doughnut data={data} options={{ ...chartOptions, scales: {} }} height={height} />;
      case 'bar':
        return <Bar data={data} options={chartOptions} height={height} />;
      case 'area':
        return <Line data={data} options={{ ...chartOptions, fill: true }} height={height} />;
      default:
        return null;
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay, duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
      whileHover={{ y: -4, transition: { duration: 0.28 } }}
      style={{ height: '100%' }}
    >
      <Card
        sx={{
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          borderRadius: '22px',
          ...(isLight
            ? {
                background: 'rgba(255, 255, 255, 0.78)',
                backdropFilter: 'blur(20px) saturate(170%)',
                WebkitBackdropFilter: 'blur(20px) saturate(170%)',
                border: '1px solid rgba(255, 255, 255, 0.9)',
                boxShadow: '0 4px 28px rgba(15, 23, 42, 0.06), 0 1px 2px rgba(59, 130, 246, 0.04)',
              }
            : {}),
        }}
      >
        <CardHeader
          title={title}
          titleTypographyProps={{
            variant: 'subtitle1',
            sx: {
              fontWeight: 700,
              fontFamily: theme.typography.h6.fontFamily,
              letterSpacing: '-0.02em',
            },
          }}
          sx={{
            pb: 1.5,
            pt: 2.5,
            px: 2.5,
            borderBottom: '1px solid',
            borderColor: isLight ? 'rgba(15, 23, 42, 0.06)' : 'divider',
          }}
        />
        <CardContent
          sx={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            position: 'relative',
            px: 2,
            pb: 2,
          }}
        >
          <Box sx={{ width: '100%', height: height }}>{renderChart()}</Box>
        </CardContent>
      </Card>
    </motion.div>
  );
};

export default ChartCard;
