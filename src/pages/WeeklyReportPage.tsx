// src/pages/WeeklyReportPage.tsx

import React, { useState, useMemo } from 'react';
import {
  Box, Typography, Grid, Card, CardContent,
  Chip, TextField, InputAdornment, CircularProgress, Dialog,
  DialogTitle, DialogContent, DialogActions, Button, Divider,
  LinearProgress, Avatar, IconButton,
  ToggleButton, ToggleButtonGroup,
} from '@mui/material';
import {
  Search as SearchIcon,
  Warning as WarningIcon,
  CheckCircle as CheckIcon,
  Info as InfoIcon,
  Edit as EditIcon,
  Close as CloseIcon,
  School as SchoolIcon,
  EventNote as AttendIcon,
  ContentCopy as CopyIcon,
} from '@mui/icons-material';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';

import {
  useWeeklyReports,
  useReportActions,
  computeSummary,
} from '@hooks/index';
import { WeeklyReport, getRiskLevel, RISK_CONFIG } from '@/types';

type FilterType = 'all' | 'high' | 'medium' | 'low' | 'low_attend';

const WeeklyReportPage: React.FC = () => {
  const { reports, loading, error } = useWeeklyReports();
  const { updateAdminNote, saving }  = useReportActions();

  const [search, setSearch]       = useState('');
  const [filter, setFilter]       = useState<FilterType>('all');
  const [selected, setSelected]   = useState<WeeklyReport | null>(null);
  const [noteInput, setNoteInput] = useState('');
  const [noteSaved, setNoteSaved] = useState(false);

  const summary = useMemo(() => computeSummary(reports), [reports]);

  const filtered = useMemo(() => {
    let result = reports;
    if (search.trim()) {
      result = result.filter(
        (r) =>
          r.studentName.toLowerCase().includes(search.toLowerCase()) ||
          r.studentId.toLowerCase().includes(search.toLowerCase())
      );
    }
    switch (filter) {
      case 'high':       result = result.filter((r) => getRiskLevel(r) === 'high');   break;
      case 'medium':     result = result.filter((r) => getRiskLevel(r) === 'medium'); break;
      case 'low':        result = result.filter((r) => getRiskLevel(r) === 'low');    break;
      case 'low_attend': result = result.filter((r) => r.attendancePercent < 75);     break;
    }
    return result;
  }, [reports, search, filter]);

  const openDetail = (r: WeeklyReport) => {
    setSelected(r);
    setNoteInput(r.adminNote ?? '');
    setNoteSaved(false);
  };

  const handleSaveNote = async () => {
    if (!selected) return;
    await updateAdminNote(selected.reportId, selected.studentId, noteInput);
    setNoteSaved(true);
    setTimeout(() => setNoteSaved(false), 2000);
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="60vh">
        <CircularProgress sx={{ color: '#00BFA5' }} />
      </Box>
    );
  }

  if (error) {
    return (
      <Box p={4}>
        <Typography color="error">Error loading reports: {error}</Typography>
        <Typography variant="body2" mt={1} color="text.secondary">
          Confirm Firestore is enabled and the <code>users</code> / <code>weeklyReports</code> collections are reachable with your security rules.
        </Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ p: { xs: 2, md: 3 } }}>
      <Typography variant="h5" fontWeight={700} mb={0.5}>Weekly Reports</Typography>
      <Typography variant="body2" color="text.secondary" mb={3}>
        Auto-generated in the client from each user&apos;s <code>moodLogs</code>, <code>activityLogs</code>, and <code>stressLevel</code>. Admin
        notes sync to Firestore <code>weeklyReports</code>.
      </Typography>

      <Grid container spacing={2} mb={3}>
        {[
          { label: 'Total Students', value: summary.totalStudents,     icon: <SchoolIcon />,  color: '#00BFA5' },
          { label: 'High Risk',      value: summary.highRiskCount,     icon: <WarningIcon />, color: '#E53935' },
          { label: 'Avg Wellness',   value: `${summary.avgWellness}%`, icon: <CheckIcon />,   color: '#43A047' },
          { label: 'Low Attendance', value: summary.lowAttendCount,    icon: <AttendIcon />,  color: '#FB8C00' },
        ].map((s) => (
          <Grid item xs={6} sm={3} key={s.label}>
            <Card sx={{ borderRadius: 3, height: '100%' }}>
              <CardContent sx={{ p: 2 }}>
                <Box display="flex" alignItems="center" gap={1} mb={1}>
                  <Box sx={{ color: s.color, display: 'flex', '& svg': { fontSize: 18 } }}>{s.icon}</Box>
                  <Typography variant="caption" color="text.secondary" fontWeight={600}>
                    {s.label.toUpperCase()}
                  </Typography>
                </Box>
                <Typography variant="h4" fontWeight={800} sx={{ color: s.color }}>{s.value}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Box display="flex" flexDirection={{ xs: 'column', sm: 'row' }} gap={2} mb={2}>
        <TextField
          size="small"
          placeholder="Search by student name or ID..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          InputProps={{
            startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment>,
          }}
          sx={{ maxWidth: 360, width: '100%' }}
        />
        <ToggleButtonGroup value={filter} exclusive onChange={(_, v) => v && setFilter(v)} size="small" sx={{ flexWrap: 'wrap', gap: 0.5 }}>
          {[
            { value: 'all', label: 'All' },
            { value: 'high', label: 'High Risk' },
            { value: 'medium', label: 'Medium' },
            { value: 'low', label: 'Low Risk' },
            { value: 'low_attend', label: 'Low Attend' },
          ].map((f) => (
            <ToggleButton key={f.value} value={f.value} sx={{ textTransform: 'none', borderRadius: '20px !important', px: 2, fontSize: '0.75rem' }}>
              {f.label}
            </ToggleButton>
          ))}
        </ToggleButtonGroup>
      </Box>

      <Typography variant="body2" color="text.secondary" mb={2}>
        {filtered.length} report{filtered.length !== 1 ? 's' : ''}
      </Typography>

      {filtered.length === 0 ? (
        <Box textAlign="center" py={8}>
          <InfoIcon sx={{ fontSize: 48, color: 'text.disabled' }} />
          <Typography color="text.secondary" mt={1}>
            {reports.length === 0
              ? 'No students in users collection — seed documents with moodLogs / activityLogs for demos.'
              : 'No reports match your filter'}
          </Typography>
        </Box>
      ) : (
        <Grid container spacing={2}>
          <AnimatePresence>
            {filtered.map((report) => (
              <Grid item xs={12} sm={6} lg={4} key={report.reportId}>
                <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0 }} transition={{ duration: 0.25 }}>
                  <ReportCard report={report} onOpen={openDetail} />
                </motion.div>
              </Grid>
            ))}
          </AnimatePresence>
        </Grid>
      )}

      <ReportDetailDialog
        report={selected}
        noteInput={noteInput}
        onNoteChange={setNoteInput}
        onSave={handleSaveNote}
        onClose={() => setSelected(null)}
        saving={saving}
        saved={noteSaved}
      />
    </Box>
  );
};

export default WeeklyReportPage;

interface ReportCardProps { report: WeeklyReport; onOpen: (r: WeeklyReport) => void; }

const ReportCard: React.FC<ReportCardProps> = ({ report, onOpen }) => {
  const risk = getRiskLevel(report);
  const riskConf = RISK_CONFIG[risk];
  return (
    <Card
      sx={{ borderRadius: 3, cursor: 'pointer', border: risk === 'high' ? `1.5px solid ${riskConf.color}` : '1px solid transparent', transition: '0.2s', '&:hover': { boxShadow: 4, transform: 'translateY(-2px)' } }}
      onClick={() => onOpen(report)}
    >
      <CardContent sx={{ pb: 1 }}>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={1.5}>
          <Box display="flex" alignItems="center" gap={1}>
            <Avatar sx={{ width: 36, height: 36, bgcolor: '#00BFA520', color: '#00BFA5', fontSize: 14, fontWeight: 700 }}>
              {report.studentName.charAt(0).toUpperCase()}
            </Avatar>
            <Box>
              <Typography variant="subtitle2" fontWeight={700} lineHeight={1.2}>{report.studentName}</Typography>
              <Typography variant="caption" color="text.secondary">{report.weekStartDate} – {report.weekEndDate}</Typography>
            </Box>
          </Box>
          <Chip label={riskConf.label} size="small" sx={{ bgcolor: riskConf.bg, color: riskConf.color, fontWeight: 700, fontSize: '0.65rem' }} />
        </Box>
        <Divider sx={{ mb: 1.5 }} />
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mb: 1 }}>
          <Chip size="small" label={`Stress trend: ${report.stressTrend ?? '—'}`} variant="outlined" />
          <Chip size="small" label={`Activity: ${report.activityLevel ?? '—'}`} variant="outlined" />
        </Box>
        <Grid container spacing={1}>
          {[
            { label: 'Attendance', value: `${Math.round(report.attendancePercent)}%`, color: report.attendancePercent < 75 ? '#E53935' : '#43A047' },
            {
              label: 'Avg stress',
              value: `${(report.avgStressPercent ?? report.avgStressLevel * 10).toFixed(0)}%`,
              color: (report.avgStressPercent ?? report.avgStressLevel * 10) > 70 ? '#E53935' : (report.avgStressPercent ?? report.avgStressLevel * 10) > 40 ? '#FB8C00' : '#43A047',
            },
            { label: 'Meditation', value: `${report.totalMeditationMinutes}m`, color: '#5E5CE6' },
            { label: 'Wellness', value: `${report.wellnessScore}%`, color: '#00BFA5' },
          ].map((s) => (
            <Grid item xs={3} key={s.label}>
              <Box textAlign="center">
                <Typography variant="subtitle2" fontWeight={800} sx={{ color: s.color }}>{s.value}</Typography>
                <Typography variant="caption" color="text.secondary" fontSize="0.6rem">{s.label}</Typography>
              </Box>
            </Grid>
          ))}
        </Grid>
        <Box mt={1.5}>
          <LinearProgress variant="determinate" value={report.wellnessScore}
            sx={{ height: 5, borderRadius: 4, bgcolor: 'action.hover', '& .MuiLinearProgress-bar': { bgcolor: report.wellnessScore >= 70 ? '#43A047' : report.wellnessScore >= 40 ? '#FB8C00' : '#E53935' } }}
          />
        </Box>
        {report.aiSuggestions && report.aiSuggestions.length > 0 && (
          <Box mt={1}>
            {report.aiSuggestions.slice(0, 2).map((t) => (
              <Typography key={t} variant="caption" color="text.secondary" display="block" sx={{ lineHeight: 1.35 }}>
                · {t}
              </Typography>
            ))}
          </Box>
        )}
        {report.adminNote && (
          <Box mt={1.5} p={1} bgcolor="action.hover" borderRadius={1.5} display="flex" gap={0.5} alignItems="center">
            <EditIcon sx={{ fontSize: 12, color: 'text.secondary' }} />
            <Typography variant="caption" color="text.secondary" noWrap>
              {report.adminNote}
            </Typography>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

interface DetailDialogProps {
  report: WeeklyReport | null; noteInput: string;
  onNoteChange: (v: string) => void; onSave: () => void;
  onClose: () => void; saving: boolean; saved: boolean;
}

const ReportDetailDialog: React.FC<DetailDialogProps> = ({ report, noteInput, onNoteChange, onSave, onClose, saving, saved }) => {
  if (!report) return null;
  const riskConf = RISK_CONFIG[getRiskLevel(report)];
  return (
    <Dialog open={!!report} onClose={onClose} maxWidth="sm" fullWidth PaperProps={{ sx: { borderRadius: 4 } }}>
      <DialogTitle sx={{ pb: 1 }}>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="h6" fontWeight={700}>{report.studentName}</Typography>
            <Typography variant="caption" color="text.secondary">{report.weekStartDate} – {report.weekEndDate}</Typography>
          </Box>
          <IconButton onClick={onClose} size="small"><CloseIcon /></IconButton>
        </Box>
      </DialogTitle>
      <DialogContent dividers sx={{ maxHeight: 480 }}>
        <Box textAlign="center" mb={2}>
          <Box sx={{ width: 80, height: 80, borderRadius: '50%', bgcolor: '#00BFA520', mx: 'auto', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
            <Typography variant="h4" fontWeight={900} color="#00BFA5">{report.wellnessScore}</Typography>
            <Typography variant="caption" color="#00BFA5">score</Typography>
          </Box>
          <Chip label={riskConf.label} size="small" sx={{ mt: 1, bgcolor: riskConf.bg, color: riskConf.color, fontWeight: 700 }} />
        </Box>
        {[
          { title: 'Attendance', rows: [['Classes attended', `${report.presentDays}/${report.totalClasses}`], ['Absent days', String(report.absentDays)], ['Late days', String(report.lateDays)], ['Attendance %', `${Math.round(report.attendancePercent)}%`]] },
          {
            title: 'Stress',
            rows: [
              ['Avg stress %', `${Math.round(report.avgStressPercent ?? report.avgStressLevel * 10)}%`],
              ['Trend', report.stressTrend ?? '—'],
              ['Check-ins', String(report.stressCheckInCount)],
              ['Risk flag', report.stressRiskFlag ? 'Yes' : 'No'],
            ],
          },
          { title: 'Meditation', rows: [['Total minutes', `${report.totalMeditationMinutes} min`], ['Sessions', String(report.meditationSessionCount)]] },
          { title: 'AI Chat', rows: [['Total messages', String(report.totalAiMessages)], ['Sessions', String(report.aiChatSessionCount)], ['Main topic', report.dominantTopic || '—']] },
        ].map((section) => (
          <Box key={section.title} sx={{ bgcolor: 'action.hover', borderRadius: 2, p: 1.5, mb: 1.5 }}>
            <Typography variant="overline" color="text.secondary" fontWeight={700} fontSize="0.65rem">{section.title}</Typography>
            {section.rows.map(([label, value]) => (
              <Box key={label} display="flex" justifyContent="space-between" mt={0.5}>
                <Typography variant="body2" color="text.secondary">{label}</Typography>
                <Typography variant="body2" fontWeight={600}>{value}</Typography>
              </Box>
            ))}
          </Box>
        ))}
        {report.aiSuggestions && report.aiSuggestions.length > 0 && (
          <Box sx={{ bgcolor: 'action.hover', borderRadius: 2, p: 1.5, mb: 1.5 }}>
            <Typography variant="overline" color="text.secondary" fontWeight={700} fontSize="0.65rem">
              Rule-based suggestions
            </Typography>
            {report.aiSuggestions.map((t) => (
              <Typography key={t} variant="body2" display="block" sx={{ mt: 0.5 }}>
                · {t}
              </Typography>
            ))}
          </Box>
        )}
        <Box mt={1}>
          <Typography variant="subtitle2" fontWeight={700} mb={0.5}>Admin Note</Typography>
          <TextField fullWidth multiline minRows={3} placeholder="Add a note about this student..." value={noteInput} onChange={(e) => onNoteChange(e.target.value)} size="small" sx={{ '& .MuiOutlinedInput-root': { borderRadius: 2 } }} />
        </Box>
      </DialogContent>
      <DialogActions sx={{ px: 3, py: 2, flexWrap: 'wrap', gap: 1 }}>
        <Button
          startIcon={<CopyIcon />}
          onClick={() => {
            void navigator.clipboard.writeText(JSON.stringify(report, null, 2));
            toast.success('Report JSON copied');
          }}
          color="inherit"
        >
          Copy JSON
        </Button>
        <Box sx={{ flex: 1 }} />
        <Button onClick={onClose} color="inherit">
          Close
        </Button>
        <Button variant="contained" onClick={onSave} disabled={saving}
          sx={{ bgcolor: saved ? '#43A047' : '#00BFA5', '&:hover': { bgcolor: saved ? '#388E3C' : '#00897B' }, borderRadius: 2, textTransform: 'none', fontWeight: 700 }}>
          {saving ? 'Saving...' : saved ? 'Saved ✓' : 'Save Note'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};