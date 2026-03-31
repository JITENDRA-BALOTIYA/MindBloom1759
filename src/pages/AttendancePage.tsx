import React, { useMemo, useState } from 'react';
import {
  Box,
  Tabs,
  Tab,
  Paper,
  Grid,
  Card,
  CardContent,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  CircularProgress,
} from '@mui/material';
import { motion } from 'framer-motion';
import ChartCard from '@components/ChartCard';
import StatCardComponent from '@components/StatCard';
import { useStudents } from '@hooks/useFirebaseStudent';
import { FirebaseErrorDisplay } from '@components/ErrorBoundary';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div role="tabpanel" hidden={value !== index} id={`attendance-tabpanel-${index}`} aria-labelledby={`attendance-tab-${index}`} {...other}>
      {value === index && <Box sx={{ py: 2 }}>{children}</Box>}
    </div>
  );
}

const AttendancePage: React.FC = () => {
  const [value, setValue] = useState(0);
  const { students, loading, error } = useStudents();

  const bySemester = useMemo(() => {
    const sem = value + 1;
    return students.filter((s) => s.semester === sem);
  }, [students, value]);

  const summary = useMemo(() => {
    if (!bySemester.length) {
      return { activeAvg: 0, pct: 0 };
    }
    const pct = Math.round(bySemester.reduce((n, s) => n + s.attendancePercentage, 0) / bySemester.length);
    const activeAvg = Math.round(bySemester.reduce((n, s) => n + s.weeklyActiveCount, 0) / bySemester.length);
    return { activeAvg, pct };
  }, [bySemester]);

  const barData = useMemo(() => {
    const buckets = [1, 2, 3, 4].map((sem) => {
      const list = students.filter((s) => s.semester === sem);
      const avg =
        list.length === 0 ? 0 : Math.round(list.reduce((n, s) => n + s.attendancePercentage, 0) / list.length);
      return avg;
    });
    return {
      labels: ['Sem 1', 'Sem 2', 'Sem 3', 'Sem 4'],
      datasets: [
        {
          label: 'Avg engagement %',
          data: buckets,
          backgroundColor: 'rgba(13, 148, 136, 0.45)',
          borderColor: '#0D9488',
          borderWidth: 2,
        },
      ],
    };
  }, [students]);

  const rows = useMemo(
    () =>
      [...bySemester]
        .sort((a, b) => b.attendancePercentage - a.attendancePercentage)
        .slice(0, 12)
        .map((s) => ({
          name: s.name,
          course: s.course,
          engagement: s.attendancePercentage,
          sessions: s.weeklyActiveCount,
          stress: Math.round(s.stressLevel),
        })),
    [bySemester]
  );

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 12 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ p: { xs: 2, sm: 3, md: 4 }, maxWidth: 1200, mx: 'auto' }}>
      {error && <FirebaseErrorDisplay error={error} />}

      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}>
        <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5 }}>
          Engagement & attendance proxy
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Engagement % is derived from active days via mood and activity logs (last 7 days), plus optional{' '}
          <code>attendancePercentage</code> on each user.
        </Typography>
      </motion.div>

      <Paper variant="outlined" sx={{ mb: 2 }}>
        <Tabs value={value} onChange={(_, v) => setValue(v)} sx={{ px: 1 }}>
          <Tab label="Semester 1" />
          <Tab label="Semester 2" />
          <Tab label="Semester 3" />
          <Tab label="Semester 4" />
        </Tabs>
      </Paper>

      <TabPanel value={value} index={value}>
        <Grid container spacing={2} sx={{ mb: 2 }}>
          <Grid item xs={12} sm={4}>
            <StatCardComponent title="Students in cohort" value={bySemester.length} icon="👥" color="#0D9488" delay={0} />
          </Grid>
          <Grid item xs={12} sm={4}>
            <StatCardComponent title="Avg engagement %" value={summary.pct} icon="📊" color="#7C3AED" unit="%" delay={0.05} />
          </Grid>
          <Grid item xs={12} sm={4}>
            <StatCardComponent title="Avg weekly sessions" value={summary.activeAvg} icon="⚡" color="#D97706" delay={0.1} />
          </Grid>
        </Grid>

        <Grid container spacing={2} sx={{ mb: 2 }}>
          <Grid item xs={12} md={7}>
            <ChartCard title="Engagement by semester" data={barData} type="bar" height={280} delay={0.1} />
          </Grid>
          <Grid item xs={12} md={5}>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                  How to read this
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  The admin app does not require a separate attendance collection. Presence is inferred from Firestore fields{' '}
                  <code>moodLogs</code> and <code>activityLogs</code>. Add explicit attendance when your institution provides it.
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
          Top students (this semester)
        </Typography>
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Course</TableCell>
                <TableCell>Engagement</TableCell>
                <TableCell>Sessions / wk</TableCell>
                <TableCell>Stress</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5}>
                    <Typography color="text.secondary">No students for this semester tab.</Typography>
                  </TableCell>
                </TableRow>
              ) : (
                rows.map((r) => (
                  <TableRow key={r.name} hover>
                    <TableCell>{r.name}</TableCell>
                    <TableCell>{r.course}</TableCell>
                    <TableCell>
                      <Chip size="small" label={`${r.engagement}%`} color={r.engagement >= 60 ? 'success' : 'warning'} variant="outlined" />
                    </TableCell>
                    <TableCell>{r.sessions}</TableCell>
                    <TableCell>{r.stress}%</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </TabPanel>
    </Box>
  );
};

export default AttendancePage;
