import React, { useMemo, useState } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  CardActions,
  Typography,
  Button,
  LinearProgress,
  Chip,
  TextField,
  MenuItem,
  Paper,
  CircularProgress,
} from '@mui/material';
import { Phone as PhoneIcon, Mail as MailIcon } from '@mui/icons-material';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { useStudents } from '@hooks/useFirebaseStudent';
import { FirebaseErrorDisplay } from '@components/ErrorBoundary';
import { getTimeAgo } from '@utils/dataUtils';
import type { StudentWithMetrics, RiskLevel } from '@types';

function alertFromStudent(s: StudentWithMetrics) {
  const reasons = s.alertReasons.length ? s.alertReasons.join(' · ') : 'Monitoring';
  return {
    id: s.id,
    name: s.name,
    email: s.email,
    stressLevel: Math.round(s.stressLevel),
    course: s.course,
    semester: s.semester,
    lastMoodCheck: getTimeAgo(
      s.moodLogs.length ? Math.max(...s.moodLogs.map((m) => m.timestamp)) : s.lastActivity
    ),
    riskLevel: s.adminRiskLevel,
    reasons,
  };
}

const AlertsPage: React.FC = () => {
  const { students, loading, error } = useStudents();
  const [filterLevel, setFilterLevel] = useState<'all' | RiskLevel>('all');
  const [sortBy, setSortBy] = useState<'stress' | 'risk'>('stress');

  const alerts = useMemo(() => {
    const list = students
      .filter((s) => s.adminRiskLevel !== 'low')
      .map(alertFromStudent)
      .filter((a) => filterLevel === 'all' || a.riskLevel === filterLevel);
    if (sortBy === 'stress') {
      return [...list].sort((a, b) => b.stressLevel - a.stressLevel);
    }
    const order: RiskLevel[] = ['high', 'medium', 'low'];
    return [...list].sort((a, b) => order.indexOf(a.riskLevel) - order.indexOf(b.riskLevel));
  }, [students, filterLevel, sortBy]);

  const counts = useMemo(() => {
    const hs = students.filter((s) => s.adminRiskLevel === 'high').length;
    const ms = students.filter((s) => s.adminRiskLevel === 'medium').length;
    const ls = students.filter((s) => s.adminRiskLevel === 'low').length;
    return { hs, ms, ls, total: hs + ms };
  }, [students]);

  const handleContact = (name: string, method: 'email' | 'phone') => {
    toast.success(`${method === 'email' ? 'Email' : 'Call'} workflow placeholder — ${name}`);
  };

  const badge = (level: RiskLevel) => {
    if (level === 'high') return { bg: 'rgba(239, 68, 68, 0.1)', color: '#DC2626' };
    if (level === 'medium') return { bg: 'rgba(245, 158, 11, 0.12)', color: '#D97706' };
    return { bg: 'rgba(16, 185, 129, 0.1)', color: '#059669' };
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

      <motion.div initial={{ opacity: 0, y: -12 }} animate={{ opacity: 1, y: 0 }}>
        <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5 }}>
          Alerts
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Rule-based flags: stress above 70%, activity drop, or 3+ days low mood (live from Firestore).
        </Typography>
      </motion.div>

      <Grid container spacing={2} sx={{ mb: 3 }}>
        {[
          { label: 'High risk', value: counts.hs, ...badge('high') },
          { label: 'Medium', value: counts.ms, ...badge('medium') },
          { label: 'Low (hidden)', value: counts.ls, ...badge('low') },
          { label: 'Open alerts', value: counts.total, color: '#0D9488', bg: 'rgba(13, 148, 136, 0.1)' },
        ].map((s) => (
          <Grid item xs={6} md={3} key={s.label}>
            <Paper variant="outlined" sx={{ p: 2, textAlign: 'center' }}>
              <Typography variant="h4" sx={{ color: s.color, fontWeight: 800 }}>
                {s.value}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {s.label}
              </Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>

      <Paper variant="outlined" sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField
              select
              fullWidth
              size="small"
              label="Severity"
              value={filterLevel}
              onChange={(e) => setFilterLevel(e.target.value as typeof filterLevel)}
            >
              <MenuItem value="all">High + medium</MenuItem>
              <MenuItem value="high">High only</MenuItem>
              <MenuItem value="medium">Medium only</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField select fullWidth size="small" label="Sort" value={sortBy} onChange={(e) => setSortBy(e.target.value as any)}>
              <MenuItem value="stress">Stress level</MenuItem>
              <MenuItem value="risk">Risk rank</MenuItem>
            </TextField>
          </Grid>
        </Grid>
      </Paper>

      <Grid container spacing={2}>
        {alerts.map((alert, idx) => {
          const b = badge(alert.riskLevel);
          return (
            <Grid item xs={12} md={6} lg={4} key={alert.id}>
              <motion.div initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: idx * 0.03 }}>
                <Card variant="outlined" sx={{ borderColor: b.color, bgcolor: b.bg, height: '100%' }}>
                  <CardContent>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="h6" sx={{ fontWeight: 700 }}>
                        {alert.name}
                      </Typography>
                      <Chip label={alert.riskLevel} size="small" color={alert.riskLevel === 'high' ? 'error' : 'warning'} sx={{ textTransform: 'capitalize' }} />
                    </Box>
                    <Typography variant="caption" color="text.secondary" display="block" sx={{ mb: 1 }}>
                      {alert.course} · Sem {alert.semester}
                    </Typography>
                    <Typography variant="caption" display="block" sx={{ mb: 1.5 }}>
                      {alert.reasons}
                    </Typography>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                      <Typography variant="body2" fontWeight={600}>
                        Stress
                      </Typography>
                      <Typography variant="body2" fontWeight={700} sx={{ color: b.color }}>
                        {alert.stressLevel}%
                      </Typography>
                    </Box>
                    <LinearProgress variant="determinate" value={Math.min(100, alert.stressLevel)} sx={{ height: 8, borderRadius: 4, bgcolor: 'rgba(0,0,0,0.08)', '& .MuiLinearProgress-bar': { bgcolor: b.color } }} />
                    <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                      Last mood log: {alert.lastMoodCheck}
                    </Typography>
                  </CardContent>
                  <CardActions>
                    <Button size="small" startIcon={<MailIcon sx={{ fontSize: 18 }} />} onClick={() => handleContact(alert.name, 'email')}>
                      Email
                    </Button>
                    <Button size="small" startIcon={<PhoneIcon sx={{ fontSize: 18 }} />} onClick={() => handleContact(alert.name, 'phone')}>
                      Call
                    </Button>
                  </CardActions>
                </Card>
              </motion.div>
            </Grid>
          );
        })}
      </Grid>

      {alerts.length === 0 && (
        <Paper variant="outlined" sx={{ p: 6, textAlign: 'center', mt: 2 }}>
          <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
            No medium/high alerts
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Students at low risk are excluded from this list.
          </Typography>
        </Paper>
      )}
    </Box>
  );
};

export default AlertsPage;
