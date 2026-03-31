import React, { useState, useMemo } from 'react';
import {
  Box,
  Paper,
  TextField,
  Button,
  MenuItem,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Typography,
  Grid,
  InputAdornment,
  CircularProgress,
  List,
  ListItem,
  ListItemText,
} from '@mui/material';
import { Search as SearchIcon, Download as DownloadIcon, Visibility as VisibilityIcon } from '@mui/icons-material';
import { DataGrid, GridColDef, GridActionsCellItem } from '@mui/x-data-grid';
import { motion } from 'framer-motion';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import Papa from 'papaparse';
import toast from 'react-hot-toast';
import { useStudents } from '@hooks/useFirebaseStudent';
import { FirebaseErrorDisplay, EmptyStateDisplay } from '@components/ErrorBoundary';
import { searchStudents, getStressBadge, getTimeAgo } from '@utils/dataUtils';
import type { StudentWithMetrics } from '@types';

const MOOD_DAYS = ['−6d', '−5d', '−4d', '−3d', '−2d', '−1d', 'Today'];

const StudentsPage: React.FC = () => {
  const { students, loading, error } = useStudents();
  const [searchQuery, setSearchQuery] = useState('');
  const [courseFilter, setCourseFilter] = useState('all');
  const [stressFilter, setStressFilter] = useState<'all' | 'high' | 'medium' | 'low'>('all');
  const [selectedStudent, setSelectedStudent] = useState<StudentWithMetrics | null>(null);
  const [detailsOpen, setDetailsOpen] = useState(false);

  const courses = useMemo(() => {
    const set = new Set(students.map((s) => s.course).filter(Boolean));
    return ['all', ...[...set].sort()];
  }, [students]);

  const filteredStudents = useMemo(() => {
    let list = searchStudents(students, searchQuery);
    if (courseFilter !== 'all') {
      list = list.filter((s) => s.course === courseFilter);
    }
    if (stressFilter === 'high') list = list.filter((s) => s.stressLevel > 70);
    else if (stressFilter === 'medium') list = list.filter((s) => s.stressLevel > 40 && s.stressLevel <= 70);
    else if (stressFilter === 'low') list = list.filter((s) => s.stressLevel <= 40);
    return list;
  }, [students, searchQuery, courseFilter, stressFilter]);

  const columns: GridColDef[] = [
    { field: 'name', headerName: 'Name', flex: 1, minWidth: 130, sortable: true },
    { field: 'email', headerName: 'Email', flex: 1.2, minWidth: 180 },
    { field: 'course', headerName: 'Course', flex: 0.9, minWidth: 120 },
    { field: 'semester', headerName: 'Semester', width: 100 },
    {
      field: 'attendancePercentage',
      headerName: 'Engagement %',
      width: 130,
      renderCell: (params) => (
        <Chip
          label={`${params.value}%`}
          size="small"
          variant="outlined"
          color={params.value >= 60 ? 'success' : 'warning'}
        />
      ),
    },
    {
      field: 'stressLevel',
      headerName: 'Stress',
      width: 110,
      renderCell: (params) => {
        const badge = getStressBadge(params.value);
        return (
          <Chip label={`${Math.round(params.value)}%`} size="small" sx={{ bgcolor: badge.bg, color: badge.color, fontWeight: 600 }} />
        );
      },
    },
    {
      field: 'adminRiskLevel',
      headerName: 'Risk',
      width: 100,
      renderCell: (params) => (
        <Chip
          label={params.value}
          size="small"
          color={params.value === 'high' ? 'error' : params.value === 'medium' ? 'warning' : 'success'}
          sx={{ textTransform: 'capitalize', fontWeight: 600 }}
        />
      ),
    },
    {
      field: 'lastActivity',
      headerName: 'Last active',
      minWidth: 140,
      flex: 0.8,
      renderCell: (params) => getTimeAgo(params.row.lastActivity),
    },
    {
      field: 'actions',
      type: 'actions',
      width: 80,
      getActions: (params) => [
        <GridActionsCellItem
          icon={<VisibilityIcon color="primary" />}
          label="View"
          onClick={() => {
            const s = students.find((x) => x.id === params.id);
            if (s) {
              setSelectedStudent(s);
              setDetailsOpen(true);
            }
          }}
        />,
      ],
    },
  ];

  const handleExportCSV = () => {
    const csv = Papa.unparse(filteredStudents);
    const link = document.createElement('a');
    link.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }));
    link.download = 'mindbloom-students.csv';
    link.click();
    toast.success('Exported CSV');
  };

  const moodChartData =
    selectedStudent?.moodTrend7d.map((mood, i) => ({
      label: MOOD_DAYS[i] ?? i,
      mood: mood > 0 ? Math.round(mood * 10) / 10 : null,
    })) ?? [];

  return (
    <Box sx={{ p: { xs: 2, sm: 3, md: 4 }, maxWidth: 1600, mx: 'auto' }}>
      {error && <FirebaseErrorDisplay error={error} />}

      <motion.div initial={{ opacity: 0, y: -12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }}>
        <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5 }}>
          Students
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Search, filter by course and stress, export CSV. Data updates live from Firestore{' '}
          <code>users</code>.
        </Typography>
      </motion.div>

      <Paper
        variant="outlined"
        sx={{
          p: 2.5,
          mb: 2,
          borderRadius: '20px',
          background: 'rgba(255,255,255,0.72)',
          backdropFilter: 'blur(16px)',
          border: '1px solid rgba(255,255,255,0.95)',
          boxShadow: '0 4px 24px rgba(15, 23, 42, 0.05)',
        }}
      >
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', md: '2fr 1fr 1fr auto' },
            gap: 2,
            alignItems: 'end',
          }}
        >
          <TextField
            fullWidth
            size="small"
            label="Search"
            placeholder="Name, email, course…"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" color="action" />
                </InputAdornment>
              ),
            }}
          />
          <TextField select size="small" label="Course" value={courseFilter} onChange={(e) => setCourseFilter(e.target.value)} fullWidth>
            {courses.map((c) => (
              <MenuItem key={c} value={c}>
                {c === 'all' ? 'All courses' : c}
              </MenuItem>
            ))}
          </TextField>
          <TextField select size="small" label="Stress" value={stressFilter} onChange={(e) => setStressFilter(e.target.value as any)} fullWidth>
            <MenuItem value="all">All</MenuItem>
            <MenuItem value="low">Low (≤40%)</MenuItem>
            <MenuItem value="medium">Medium (41–70%)</MenuItem>
            <MenuItem value="high">High (&gt;70%)</MenuItem>
          </TextField>
          <Button variant="contained" startIcon={<DownloadIcon />} onClick={handleExportCSV} disabled={loading || filteredStudents.length === 0}>
            CSV
          </Button>
        </Box>
      </Paper>

      {loading ? (
        <Paper
          variant="outlined"
          sx={{
            height: 520,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderRadius: '22px',
            background: 'rgba(255,255,255,0.65)',
            backdropFilter: 'blur(12px)',
          }}
        >
          <CircularProgress />
        </Paper>
      ) : filteredStudents.length === 0 ? (
        <Paper variant="outlined" sx={{ p: 3 }}>
          <EmptyStateDisplay
            title="No rows"
            description={searchQuery || courseFilter !== 'all' || stressFilter !== 'all' ? 'Adjust filters' : 'No users in collection yet'}
          />
        </Paper>
      ) : (
        <Paper
          variant="outlined"
          sx={{
            height: 560,
            width: '100%',
            borderRadius: '22px',
            overflow: 'hidden',
            border: '1px solid rgba(255,255,255,0.95)',
            background: 'rgba(255,255,255,0.65)',
            backdropFilter: 'blur(14px)',
            boxShadow: '0 8px 32px rgba(15, 23, 42, 0.06)',
          }}
        >
          <DataGrid
            rows={filteredStudents}
            columns={columns}
            pageSizeOptions={[10, 25, 50]}
            initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            disableRowSelectionOnClick
            sx={{
              border: 'none',
              fontSize: '0.875rem',
              '& .MuiDataGrid-columnHeaders': {
                background: 'linear-gradient(180deg, rgba(248,250,252,0.98) 0%, rgba(241,245,249,0.85) 100%)',
                borderBottom: '2px solid rgba(15, 23, 42, 0.06)',
                fontWeight: 700,
              },
              '& .MuiDataGrid-row': {
                transition: 'transform 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease',
                '&:hover': {
                  bgcolor: 'rgba(59, 130, 246, 0.06)',
                  boxShadow: 'inset 3px 0 0 rgba(13, 148, 136, 0.65)',
                },
              },
              '& .MuiDataGrid-cell': {
                borderColor: 'rgba(15, 23, 42, 0.05)',
                py: 1.25,
              },
              '& .MuiDataGrid-footerContainer': {
                borderTop: '1px solid rgba(15, 23, 42, 0.08)',
                background: 'rgba(255,255,255,0.5)',
              },
            }}
          />
        </Paper>
      )}

      <Dialog open={detailsOpen} onClose={() => setDetailsOpen(false)} maxWidth="sm" fullWidth scroll="body" TransitionProps={{ timeout: 380 }}>
        {selectedStudent && (
          <motion.div initial={{ opacity: 0, scale: 0.96, y: 8 }} animate={{ opacity: 1, scale: 1, y: 0 }} transition={{ duration: 0.32, ease: [0.22, 1, 0.36, 1] }}>
            <DialogTitle sx={{ fontWeight: 800, letterSpacing: '-0.02em' }}>{selectedStudent.name}</DialogTitle>
            <DialogContent dividers>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="caption" color="text.secondary">
                    Email
                  </Typography>
                  <Typography variant="body2">{selectedStudent.email}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="caption" color="text.secondary">
                    Course
                  </Typography>
                  <Typography variant="body2">{selectedStudent.course || '—'}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="caption" color="text.secondary">
                    Stress
                  </Typography>
                  <Chip
                    size="small"
                    label={`${Math.round(selectedStudent.stressLevel)}% · ${selectedStudent.adminRiskLevel}`}
                    color={selectedStudent.adminRiskLevel === 'high' ? 'error' : selectedStudent.adminRiskLevel === 'medium' ? 'warning' : 'success'}
                    sx={{ mt: 0.5, textTransform: 'capitalize' }}
                  />
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="caption" color="text.secondary">
                    Weekly sessions
                  </Typography>
                  <Typography variant="body2">{selectedStudent.weeklyActiveCount}</Typography>
                </Grid>
                <Grid item xs={12}>
                  <Typography variant="subtitle2" fontWeight={700} sx={{ mb: 1 }}>
                    Mood (7 days)
                  </Typography>
                  <Box sx={{ width: '100%', height: 220 }}>
                    <ResponsiveContainer>
                      <LineChart data={moodChartData} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.08)" />
                        <XAxis dataKey="label" tick={{ fontSize: 11 }} />
                        <YAxis domain={[1, 10]} tick={{ fontSize: 11 }} width={28} />
                        <Tooltip />
                        <Line type="monotone" dataKey="mood" stroke="#0D9488" strokeWidth={2} dot connectNulls={false} />
                      </LineChart>
                    </ResponsiveContainer>
                  </Box>
                </Grid>
                {selectedStudent.alertReasons.length > 0 && (
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" fontWeight={700}>
                      Flags
                    </Typography>
                    <List dense disablePadding>
                      {selectedStudent.alertReasons.map((r) => (
                        <ListItem key={r} disablePadding sx={{ py: 0.25 }}>
                          <ListItemText primaryTypographyProps={{ variant: 'caption' }} primary={r} />
                        </ListItem>
                      ))}
                    </List>
                  </Grid>
                )}
                <Grid item xs={12}>
                  <Typography variant="subtitle2" fontWeight={700} sx={{ mb: 0.5 }}>
                    Suggestions
                  </Typography>
                  {selectedStudent.aiSuggestions.map((s) => (
                    <Typography key={s} variant="caption" display="block" sx={{ mb: 0.5 }}>
                      · {s}
                    </Typography>
                  ))}
                </Grid>
              </Grid>
            </DialogContent>
            <DialogActions sx={{ px: 3, pb: 2 }}>
              <Button variant="outlined" onClick={() => setDetailsOpen(false)} sx={{ borderRadius: '12px' }}>
                Close
              </Button>
            </DialogActions>
          </motion.div>
        )}
      </Dialog>
    </Box>
  );
};

export default StudentsPage;
