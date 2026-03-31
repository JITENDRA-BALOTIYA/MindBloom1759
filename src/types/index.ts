export type RiskLevel = 'low' | 'medium' | 'high';

export interface MoodLogEntry {
  timestamp: number;
  mood: number;
  note?: string;
}

export interface ActivityLogEntry {
  timestamp: number;
  type?: string;
  durationMinutes?: number;
  sessionId?: string;
}

export interface Student {
  id: string;
  name: string;
  email: string;
  course: string;
  semester: number;
  enrollmentDate: number;
  lastActivity: number;
  profileImage?: string;
}

export interface StudentWithMetrics extends Student {
  attendancePercentage: number;
  /** 0–100 calculated or from Firestore `stressLevel` */
  stressLevel: number;
  weeklyActiveCount: number;
  moodLogs: MoodLogEntry[];
  activityLogs: ActivityLogEntry[];
  moodTrend7d: number[];
  previousWeekActivityCount: number;
  adminRiskLevel: RiskLevel;
  alertReasons: string[];
  aiSuggestions: string[];
  negativeMoodStreakDays: number;
}

export interface AttendanceRecord {
  date: string;
  status: 'present' | 'absent';
  timestamp: number;
}

export interface AttendanceSummary {
  presentDays: number;
  absentDays: number;
  percentage: number;
}

export interface ActivityData {
  moodChecks: number[];
  meditationMinutes: number[];
  aiAssistantUsage: number[];
  engagementScore: number;
  dates: string[];
}

export interface StudentAlert {
  studentId: string;
  name: string;
  email: string;
  stressLevel: number;
  lastMoodCheck: number;
  course: string;
  semester: number;
  riskLevel: 'high' | 'medium' | 'low';
}

export interface StatCard {
  title: string;
  value: number | string;
  icon: React.ReactNode;
  color: string;
  trend?: number;
  unit?: string;
}

export interface User {
  uid: string;
  email: string;
  displayName: string;
  photoURL?: string;
  role: 'admin' | 'moderator';
}

export interface DashboardStats {
  totalStudents: number;
  highStressStudents: number;
  averageAttendance: number;
  weeklyActiveUsers: number;
}

export interface MoodEntry {
  timestamp: number;
  mood: number; // 1-10
  note?: string;
}

export interface MeditationSession {
  timestamp: number;
  duration: number; // in minutes
}

export interface AiInteraction {
  timestamp: number;
  topic: string;
  duration: number; // in seconds
}

export interface CourseData {
  courseId: string;
  courseName: string;
  semester: number;
  totalStudents: number;
}

export interface ChartDataPoint {
  label: string;
  value: number;
  timestamp?: number;
}

// ── Weekly Report Types ──────────────────────────────────────────────────────

export interface WeeklyReport {
  reportId: string;
  studentId: string;
  studentName: string;
  weekStartDate: string;
  weekEndDate: string;
  generatedAt: number;
  totalClasses: number;
  presentDays: number;
  absentDays: number;
  lateDays: number;
  attendancePercent: number;
  /** Average stress on 1–10 scale (for charts / legacy UI) */
  avgStressLevel: number;
  /** 0–100 average stress (explicit) */
  avgStressPercent?: number;
  maxStressLevel: number;
  stressCheckInCount: number;
  stressRiskFlag: boolean;
  totalMeditationMinutes: number;
  meditationSessionCount: number;
  totalAiMessages: number;
  aiChatSessionCount: number;
  dominantTopic: string;
  wellnessScore: number;
  adminNote?: string;
  stressTrend?: 'worsening' | 'stable' | 'improving';
  activityLevel?: 'low' | 'moderate' | 'high';
  riskLevel?: RiskLevel;
  aiSuggestions?: string[];
}

// Risk level configuration
export const RISK_CONFIG: Record<RiskLevel, { bg: string; color: string; label: string }> = {
  low: { bg: '#E8F5E9', color: '#2E7D32', label: 'Low Risk' },
  medium: { bg: '#FFF3E0', color: '#E65100', label: 'Medium Risk' },
  high: { bg: '#FFEBEE', color: '#C62828', label: 'High Risk' },
};

// Determine risk level based on report metrics (falls back if `riskLevel` not stored)
export function getRiskLevel(report: WeeklyReport): RiskLevel {
  if (report.riskLevel) return report.riskLevel;

  const stressPct =
    report.avgStressPercent != null
      ? report.avgStressPercent
      : report.avgStressLevel > 10
        ? report.avgStressLevel
        : report.avgStressLevel * 10;

  const attendanceScore = report.attendancePercent;
  const wellnessScore = report.wellnessScore;

  const riskScore = stressPct * 0.4 + (100 - attendanceScore) * 0.3 + (100 - wellnessScore) * 0.3;

  if (riskScore > 60) return 'high';
  if (riskScore > 35) return 'medium';
  return 'low';
}
