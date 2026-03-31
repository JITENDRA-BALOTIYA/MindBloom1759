import { StudentWithMetrics } from '../types/index';

/**
 * Calculate dashboard statistics from students data
 */
export function calculateDashboardStats(students: StudentWithMetrics[]) {
  const totalStudents = students.length;
  const highStressStudents = students.filter((s) => s.stressLevel > 70).length;
  const averageAttendance = students.length > 0 
    ? Math.round(students.reduce((sum, s) => sum + s.attendancePercentage, 0) / students.length)
    : 0;
  const weeklyActiveUsers = students.filter(s => {
    const lastActivity = new Date(s.lastActivity);
    const oneWeekAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
    return lastActivity > oneWeekAgo;
  }).length;

  return {
    totalStudents,
    highStressStudents,
    averageAttendance,
    weeklyActiveUsers,
  };
}

/**
 * Filter students by stress level
 */
export function filterStudentsByStress(students: StudentWithMetrics[], level: 'high' | 'medium' | 'low') {
  return students.filter((student) => {
    if (level === 'high') return student.stressLevel > 70;
    if (level === 'medium') return student.stressLevel > 40 && student.stressLevel <= 70;
    return student.stressLevel <= 40;
  });
}

/**
 * Sort students by stress level descending
 */
export function sortStudentsByStress(students: StudentWithMetrics[]): StudentWithMetrics[] {
  return [...students].sort((a, b) => b.stressLevel - a.stressLevel);
}

/**
 * Get students with alerts (high risk)
 */
export function getHighRiskStudents(students: StudentWithMetrics[], threshold = 70): StudentWithMetrics[] {
  return students
    .filter(s => s.stressLevel >= threshold)
    .sort((a, b) => b.stressLevel - a.stressLevel);
}

/**
 * Calculate attendance statistics
 */
export function calculateAttendanceStats(attendanceRecords: Array<{ status: string; date: string }>) {
  const presentDays = attendanceRecords.filter(r => r.status === 'present').length;
  const absentDays = attendanceRecords.filter(r => r.status === 'absent').length;
  const total = presentDays + absentDays;
  const percentage = total > 0 ? Math.round((presentDays / total) * 100) : 0;

  return {
    presentDays,
    absentDays,
    totalDays: total,
    percentage,
  };
}

/**
 * Group students by semester
 */
export function groupStudentsBySemester(students: StudentWithMetrics[]): Record<number, StudentWithMetrics[]> {
  const grouped: Record<number, StudentWithMetrics[]> = {};
  
  students.forEach(student => {
    const sem = student.semester;
    if (!grouped[sem]) {
      grouped[sem] = [];
    }
    grouped[sem].push(student);
  });

  return grouped;
}

/**
 * Group students by course
 */
export function groupStudentsByCourse(students: StudentWithMetrics[]): Record<string, StudentWithMetrics[]> {
  const grouped: Record<string, StudentWithMetrics[]> = {};
  
  students.forEach(student => {
    const course = student.course || 'Unknown';
    if (!grouped[course]) {
      grouped[course] = [];
    }
    grouped[course].push(student);
  });

  return grouped;
}

/**
 * Search students by name or email
 */
export function searchStudents(students: StudentWithMetrics[], query: string): StudentWithMetrics[] {
  const lowerQuery = query.toLowerCase();
  return students.filter(student =>
    student.name.toLowerCase().includes(lowerQuery) ||
    student.email.toLowerCase().includes(lowerQuery) ||
    student.course.toLowerCase().includes(lowerQuery)
  );
}

/**
 * Format timestamp to readable date
 */
export function formatDate(timestamp: number | string): string {
  try {
    const date = new Date(typeof timestamp === 'string' ? parseInt(timestamp) : timestamp);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  } catch {
    return 'Invalid date';
  }
}

/**
 * Format timestamp to time ago (e.g., "2 hours ago")
 */
export function getTimeAgo(timestamp: number): string {
  const now = Date.now();
  const diff = now - timestamp;
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (seconds < 60) return 'just now';
  if (minutes < 60) return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
  if (hours < 24) return `${hours} hour${hours > 1 ? 's' : ''} ago`;
  if (days < 7) return `${days} day${days > 1 ? 's' : ''} ago`;
  
  return formatDate(timestamp);
}

/**
 * Get stress level label and color
 */
export function getStressBadge(level: number) {
  if (level <= 40) {
    return { label: 'Low', color: '#10B981', bg: 'rgba(16, 185, 129, 0.12)' };
  }
  if (level <= 70) {
    return { label: 'Medium', color: '#F59E0B', bg: 'rgba(245, 158, 11, 0.12)' };
  }
  return { label: 'High', color: '#EF4444', bg: 'rgba(239, 68, 68, 0.12)' };
}

/**
 * Generate chart data for weekly activity
 */
export function generateWeeklyActivityChart(activity: any[]) {
  const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  const data = new Array(7).fill(0);

  activity.forEach(entry => {
    const date = new Date(entry.timestamp);
    const dayIndex = (date.getDay() + 6) % 7; // Convert to Monday=0
    data[dayIndex]++;
  });

  return {
    labels: days,
    datasets: [
      {
        label: 'Active Users',
        data,
        borderColor: '#00BFA5',
        backgroundColor: 'rgba(0, 191, 165, 0.1)',
        tension: 0.4,
        fill: true,
        pointBackgroundColor: '#00BFA5',
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        pointRadius: 4,
      },
    ],
  };
}
