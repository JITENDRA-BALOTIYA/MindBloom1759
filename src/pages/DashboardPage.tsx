import React, { useMemo } from 'react';
import { Box, Grid, Typography, Paper, Skeleton, Button } from '@mui/material';
import { Download as DownloadIcon } from '@mui/icons-material';
import { motion } from 'framer-motion';
import Papa from 'papaparse';
import toast from 'react-hot-toast';
import StatCardComponent from '@components/StatCard';
import ChartCard from '@components/ChartCard';
import { FirebaseErrorDisplay, EmptyStateDisplay } from '@components/ErrorBoundary';
import { useStudents } from '@hooks/useFirebaseStudent';
import { calculateDashboardStats, filterStudentsByStress } from '@utils/dataUtils';
import {
  aggregateActivityTrendChart,
  aggregateStressTrendChart,
  countHighRiskStudents,
} from '@utils/studentAnalytics';

const containerVariants = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: { staggerChildren: 0.06, delayChildren: 0.04 },
  },
};

const itemVariants = {
  hidden: { opacity: 0, y: 16 },
  show: { opacity: 1, y: 0 },
};

const DashboardPage: React.FC = () => {
  const { students, loading, error } = useStudents();

  const stats = useMemo(() => calculateDashboardStats(students), [students]);
  const highRiskCount = useMemo(() => countHighRiskStudents(students), [students]);
  const highStressStudents = useMemo(() => filterStudentsByStress(students, 'high'), [students]);

  const weeklyActivityData = useMemo(() => {
    const { labels, values } = aggregateActivityTrendChart(students);
    return {
      labels,
      datasets: [
        {
          label: 'Sessions (all students)',
          data: values,
          borderColor: '#0D9488',
          backgroundColor: 'rgba(13, 148, 136, 0.14)',
          tension: 0.4,
          fill: true,
          pointBackgroundColor: '#0D9488',
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointRadius: 4,
          pointHoverRadius: 6,
        },
      ],
    };
  }, [students]);

  const stressTrendData = useMemo(() => {
    const { labels, values } = aggregateStressTrendChart(students);
    return {
      labels,
      datasets: [
        {
          label: 'Avg stress index (0–100)',
          data: values,
          borderColor: '#6366F1',
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          tension: 0.4,
          fill: true,
          pointBackgroundColor: '#6366F1',
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointRadius: 4,
          pointHoverRadius: 6,
        },
      ],
    };
  }, [students]);

  const lowStress = students.filter((s) => s.stressLevel <= 40).length;
  const mediumStress = students.filter((s) => s.stressLevel > 40 && s.stressLevel <= 70).length;
  const highStress = students.filter((s) => s.stressLevel > 70).length;

  const stressDistributionData = {
    labels: ['Low (≤40)', 'Medium (41–70)', 'High (>70)'],
    datasets: [
      {
        data: [lowStress, mediumStress, highStress],
        backgroundColor: ['rgba(16, 185, 129, 0.85)', 'rgba(245, 158, 11, 0.88)', 'rgba(239, 68, 68, 0.88)'],
        borderColor: ['#059669', '#D97706', '#DC2626'],
        borderWidth: 2,
        hoverOffset: 8,
      },
    ],
  };

  const handleExport = () => {
    const rows = students.map((s) => ({
      id: s.id,
      name: s.name,
      email: s.email,
      course: s.course,
      semester: s.semester,
      stressPercent: s.stressLevel,
      risk: s.adminRiskLevel,
      weeklyActive: s.weeklyActiveCount,
      attendanceProxy: s.attendancePercentage,
      lastActive: s.lastActivity,
    }));
    const csv = Papa.unparse(rows);
    const link = document.createElement('a');
    link.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }));
    link.download = `mindbloom-students-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    toast.success('Exported student snapshot');
  };

  const StatSkeleton = () => (
    <Skeleton
      variant="rounded"
      height={158}
      sx={{ borderRadius: '22px' }}
      className="mindbloom-shimmer"
      animation={false}
    />
  );

  return (
    <Box sx={{ p: { xs: 2, sm: 3, md: 4 }, maxWidth: 1440, mx: 'auto' }}>
      {error && <FirebaseErrorDisplay error={error} />}

      <motion.div initial={{ opacity: 0, y: -14 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.45, ease: [0.22, 1, 0.36, 1] }}>
        <Paper
          elevation={0}
          sx={{
            p: { xs: 2.5, sm: 3 },
            mb: 3,
            borderRadius: '24px',
            background: 'linear-gradient(135deg, rgba(255,255,255,0.9) 0%, rgba(248, 250, 252, 0.75) 45%, rgba(236, 253, 250, 0.65) 100%)',
            backdropFilter: 'blur(20px)',
            border: '1px solid rgba(255, 255, 255, 0.9)',
            boxShadow: '0 8px 40px rgba(15, 23, 42, 0.06), 0 2px 8px rgba(59, 130, 246, 0.04)',
            position: 'relative',
            overflow: 'hidden',
            '&::after': {
              content: '""',
              position: 'absolute',
              top: '-60%',
              right: '-20%',
              width: '55%',
              height: '140%',
              background: 'radial-gradient(ellipse, rgba(59, 130, 246, 0.12) 0%, transparent 65%)',
              pointerEvents: 'none',
            },
          }}
        >
          <Box sx={{ position: 'relative', zIndex: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 2 }}>
            <Box>
              <Typography variant="h4" sx={{ fontWeight: 800, mb: 0.5, letterSpacing: '-0.03em' }}>
                MindBloom
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 420, lineHeight: 1.6 }}>
                Live wellness overview — soft glass UI, crisp metrics, Firestore-backed.
              </Typography>
            </Box>
            <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.98 }}>
              <Button variant="contained" size="large" startIcon={<DownloadIcon />} onClick={handleExport} disabled={loading || students.length === 0} sx={{ borderRadius: '14px', px: 2.5 }}>
                Export CSV
              </Button>
            </motion.div>
          </Box>
        </Paper>
      </motion.div>

      <Grid container spacing={2.5} sx={{ mb: 3 }} component={motion.div} variants={containerVariants} initial="hidden" animate="show">
        <Grid item xs={12} sm={6} md={3} component={motion.div} variants={itemVariants}>
          {loading ? <StatSkeleton /> : <StatCardComponent title="Total students" value={stats.totalStudents} icon="👥" color="#0D9488" delay={0} />}
        </Grid>
        <Grid item xs={12} sm={6} md={3} component={motion.div} variants={itemVariants}>
          {loading ? <StatSkeleton /> : <StatCardComponent title="Active (7 days)" value={stats.weeklyActiveUsers} icon="⭐" color="#6366F1" delay={0.05} />}
        </Grid>
        <Grid item xs={12} sm={6} md={3} component={motion.div} variants={itemVariants}>
          {loading ? <StatSkeleton /> : <StatCardComponent title="High risk" value={highRiskCount} icon="⚠️" color="#DC2626" delay={0.1} />}
        </Grid>
        <Grid item xs={12} sm={6} md={3} component={motion.div} variants={itemVariants}>
          {loading ? <StatSkeleton /> : <StatCardComponent title="Avg engagement" value={stats.averageAttendance} icon="📊" color="#D97706" unit="%" delay={0.15} />}
        </Grid>
      </Grid>

      <Grid container spacing={2.5}>
        <Grid item xs={12} md={6}>
          {loading ? (
            <Skeleton variant="rounded" height={340} sx={{ borderRadius: '22px' }} className="mindbloom-shimmer" animation={false} />
          ) : (
            <ChartCard title="Weekly stress (cohort)" data={stressTrendData} type="line" height={280} delay={0.15} />
          )}
        </Grid>
        <Grid item xs={12} md={6}>
          {loading ? (
            <Skeleton variant="rounded" height={340} sx={{ borderRadius: '22px' }} className="mindbloom-shimmer" animation={false} />
          ) : (
            <ChartCard title="Activity trend" data={weeklyActivityData} type="line" height={280} delay={0.2} />
          )}
        </Grid>
        <Grid item xs={12} md={6}>
          {loading ? (
            <Skeleton variant="rounded" height={300} sx={{ borderRadius: '22px' }} className="mindbloom-shimmer" animation={false} />
          ) : (
            <ChartCard title="Stress distribution" data={stressDistributionData} type="doughnut" height={260} delay={0.25} />
          )}
        </Grid>
      </Grid>

      <Box sx={{ mt: 3 }}>
        <motion.div initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2, duration: 0.45, ease: [0.22, 1, 0.36, 1] }}>
          <Paper
            elevation={0}
            sx={{
              p: 3,
              borderRadius: '24px',
              background: 'rgba(255, 255, 255, 0.78)',
              backdropFilter: 'blur(18px)',
              border: '1px solid rgba(255, 255, 255, 0.95)',
              boxShadow: '0 8px 36px rgba(15, 23, 42, 0.06)',
            }}
          >
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 2, letterSpacing: '-0.02em' }}>
              Risk alerts ({highStressStudents.length})
            </Typography>
            {highStressStudents.length === 0 ? (
              <EmptyStateDisplay title="No high stress flags" description="No students are above 70% stress on the current model." />
            ) : (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.25 }}>
                {highStressStudents.slice(0, 5).map((student, i) => (
                  <motion.div
                    key={student.id}
                    initial={{ opacity: 0, x: -12 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: 0.05 * i, duration: 0.35 }}
                    whileHover={{ scale: 1.01, x: 4 }}
                  >
                    <Box
                      sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        gap: 2,
                        p: 2,
                        borderRadius: '16px',
                        background: 'linear-gradient(135deg, rgba(239, 68, 68, 0.08) 0%, rgba(255,255,255,0.6) 100%)',
                        border: '1px solid rgba(239, 68, 68, 0.2)',
                        boxShadow: '0 2px 12px rgba(239, 68, 68, 0.06)',
                        transition: 'box-shadow 0.25s ease',
                        '&:hover': { boxShadow: '0 8px 24px rgba(239, 68, 68, 0.12)' },
                      }}
                    >
                      <Box>
                        <Typography variant="subtitle2" fontWeight={800}>
                          {student.name}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          Stress {Math.round(student.stressLevel)}% · {student.course || '—'} · {student.alertReasons[0] ?? 'Review profile'}
                        </Typography>
                      </Box>
                      <Typography variant="caption" fontWeight={800} color="error.main" sx={{ letterSpacing: '0.06em' }}>
                        {student.adminRiskLevel.toUpperCase()}
                      </Typography>
                    </Box>
                  </motion.div>
                ))}
              </Box>
            )}
          </Paper>
        </motion.div>
      </Box>
    </Box>
  );
};

export default DashboardPage;
