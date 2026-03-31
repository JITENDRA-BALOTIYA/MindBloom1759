import React, { useMemo, useState } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  TextField,
  MenuItem,
  Button,
  Card,
  CardContent,
  CircularProgress,
} from '@mui/material';
import { motion } from 'framer-motion';
import ChartCard from '@components/ChartCard';
import { Download as DownloadIcon } from '@mui/icons-material';
import toast from 'react-hot-toast';
import { useStudents } from '@hooks/useFirebaseStudent';
import { FirebaseErrorDisplay } from '@components/ErrorBoundary';
import { aggregateActivityTrendChart, aggregateMoodLineChart, moodTrendLast7Days } from '@utils/studentAnalytics';

const AnalyticsPage: React.FC = () => {
  const { students, loading, error } = useStudents();
  const [courseFilter, setCourseFilter] = useState('all');

  const cohort = useMemo(() => {
    if (courseFilter === 'all') return students;
    return students.filter((s) => s.course === courseFilter);
  }, [students, courseFilter]);

  const courses = useMemo(() => {
    const u = new Set(students.map((s) => s.course).filter(Boolean));
    return ['all', ...[...u].sort()];
  }, [students]);

  const moodChart = useMemo(() => {
    const pts = aggregateMoodLineChart(cohort);
    return {
      labels: ['−6d', '−5d', '−4d', '−3d', '−2d', '−1d', 'Today'],
      datasets: [
        {
          label: 'Avg mood (1–10)',
          data: pts,
          borderColor: '#7C3AED',
          backgroundColor: 'rgba(124, 58, 237, 0.1)',
          tension: 0.35,
          fill: true,
          pointRadius: 4,
        },
      ],
    };
  }, [cohort]);

  const activityChart = useMemo(() => {
    const { labels, values } = aggregateActivityTrendChart(cohort);
    return {
      labels,
      datasets: [
        {
          label: 'Sessions',
          data: values,
          backgroundColor: 'rgba(13, 148, 136, 0.35)',
          borderColor: '#0D9488',
          borderWidth: 1.5,
        },
      ],
    };
  }, [cohort]);

  const totals = useMemo(() => {
    const checkins = cohort.reduce((n, s) => n + moodTrendLast7Days(s.moodLogs).filter((v) => v > 0).length, 0);
    const meditationMin = cohort.reduce(
      (n, s) => n + s.activityLogs.reduce((m, a) => m + (a.durationMinutes ?? 0), 0),
      0
    );
    const ai = cohort.reduce((n, s) => n + s.activityLogs.filter((a) => a.type === 'ai' || a.type === 'chat').length, 0);
    const avgEng = cohort.length
      ? Math.round(cohort.reduce((n, s) => n + s.attendancePercentage, 0) / cohort.length)
      : 0;
    return { checkins, meditationMin: Math.round(meditationMin), ai, avgEng };
  }, [cohort]);

  const handleExport = () => {
    const payload = { cohortSize: cohort.length, totals, generatedAt: new Date().toISOString() };
    const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'mindbloom-analytics.json';
    a.click();
    toast.success('Exported JSON summary');
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 12 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ p: { xs: 2, sm: 3, md: 4 }, maxWidth: 1400, mx: 'auto' }}>
      {error && <FirebaseErrorDisplay error={error} />}

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 2, mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5 }}>
            Analytics
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Aggregated mood and activity from Firestore <code>users</code> documents.
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<DownloadIcon />} onClick={handleExport} disabled={!cohort.length}>
          Export JSON
        </Button>
      </Box>

      <Paper variant="outlined" sx={{ p: 2, mb: 3 }}>
        <TextField select size="small" label="Course" value={courseFilter} onChange={(e) => setCourseFilter(e.target.value)} sx={{ minWidth: 220 }}>
          {courses.map((c) => (
            <MenuItem key={c} value={c}>
              {c === 'all' ? 'All courses' : c}
            </MenuItem>
          ))}
        </TextField>
      </Paper>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <ChartCard title="Cohort mood (7-day avg)" data={moodChart} type="line" height={280} delay={0.05} />
        </Grid>
        <Grid item xs={12} md={6}>
          <ChartCard title="Cohort activity (sessions / day)" data={activityChart} type="bar" height={280} delay={0.1} />
        </Grid>
      </Grid>

      <Grid container spacing={2} sx={{ mt: 1 }}>
        {[
          { t: 'Mood data points (7d)', v: totals.checkins, c: '#7C3AED' },
          { t: 'Meditation min (logged)', v: totals.meditationMin, c: '#0D9488' },
          { t: 'AI interactions (tags)', v: totals.ai, c: '#D97706' },
          { t: 'Avg engagement %', v: `${totals.avgEng}%`, c: '#059669' },
        ].map((k, i) => (
          <Grid item xs={12} sm={6} md={3} key={k.t}>
            <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.15 + i * 0.05 }}>
              <Card variant="outlined">
                <CardContent>
                  <Typography color="text.secondary" variant="caption">
                    {k.t}
                  </Typography>
                  <Typography variant="h5" sx={{ fontWeight: 800, color: k.c }}>
                    {k.v}
                  </Typography>
                </CardContent>
              </Card>
            </motion.div>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default AnalyticsPage;
