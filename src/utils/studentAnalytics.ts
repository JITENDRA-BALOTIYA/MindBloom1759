import { startOfDay, subDays } from 'date-fns';
import type { ActivityLogEntry, MoodLogEntry, RiskLevel, StudentWithMetrics, WeeklyReport } from '@types';
import { clamp, toMillis } from './firestoreNormalize';

const DAY_MS = 24 * 60 * 60 * 1000;
const NEGATIVE_MOOD_THRESHOLD = 4;
const HIGH_STRESS = 70;
const MEDIUM_STRESS = 40;
const ACTIVITY_DROP_RATIO = 0.35;

export function normalizeMoodLogs(raw: unknown): MoodLogEntry[] {
  if (raw == null) return [];
  if (Array.isArray(raw)) {
    return (raw as Record<string, unknown>[])
      .map((item) => ({
        timestamp: toMillis(item.timestamp ?? item.date ?? item.createdAt),
        mood: clamp(Number(item.mood ?? item.score ?? 5), 1, 10),
        note: typeof item.note === 'string' ? item.note : undefined,
      }))
      .filter((e) => !Number.isNaN(e.timestamp));
  }
  if (typeof raw === 'object') {
    return Object.entries(raw as Record<string, Record<string, unknown>>).map(([key, item]) => ({
      timestamp: toMillis(item?.timestamp ?? item?.date ?? key),
      mood: clamp(Number(item?.mood ?? item?.score ?? 5), 1, 10),
      note: typeof item?.note === 'string' ? item.note : undefined,
    }));
  }
  return [];
}

export function normalizeActivityLogs(raw: unknown): ActivityLogEntry[] {
  if (raw == null) return [];
  if (Array.isArray(raw)) {
    return (raw as Record<string, unknown>[])
      .map((item) => ({
        timestamp: toMillis(item.timestamp ?? item.startedAt ?? item.date ?? item.createdAt),
        type: typeof item.type === 'string' ? item.type : typeof item.kind === 'string' ? item.kind : undefined,
        durationMinutes:
          typeof item.durationMinutes === 'number'
            ? item.durationMinutes
            : typeof item.duration === 'number'
              ? item.duration / 60
              : undefined,
        sessionId: typeof item.sessionId === 'string' ? item.sessionId : undefined,
      }))
      .filter((e) => !Number.isNaN(e.timestamp));
  }
  if (typeof raw === 'object') {
    return Object.entries(raw as Record<string, Record<string, unknown>>).map(([key, item]) => ({
      timestamp: toMillis(item?.timestamp ?? item?.startedAt ?? key),
      type: typeof item?.type === 'string' ? item.type : undefined,
      durationMinutes:
        typeof item?.durationMinutes === 'number'
          ? item.durationMinutes
          : typeof item?.duration === 'number'
            ? (item.duration as number) / 60
            : undefined,
      sessionId: typeof item?.sessionId === 'string' ? item.sessionId : undefined,
    }));
  }
  return [];
}

function dayStart(t: number): number {
  return startOfDay(t).getTime();
}

/** Seven daily average moods ending today; 0 means no logs that calendar day */
export function moodTrendLast7Days(moodLogs: MoodLogEntry[], now = Date.now()): number[] {
  const trend: number[] = [];
  for (let i = 6; i >= 0; i--) {
    const d0 = dayStart(subDays(now, i).getTime());
    const d1 = d0 + DAY_MS;
    const moods = moodLogs.filter((m) => m.timestamp >= d0 && m.timestamp < d1).map((m) => m.mood);
    trend.push(moods.length ? moods.reduce((a, b) => a + b, 0) / moods.length : 0);
  }
  return trend;
}

export function consecutiveNegativeMoodDays(moodLogs: MoodLogEntry[], now = Date.now()): number {
  let streak = 0;
  for (let i = 0; i < 14; i++) {
    const d0 = dayStart(subDays(now, i).getTime());
    const d1 = d0 + DAY_MS;
    const moods = moodLogs.filter((m) => m.timestamp >= d0 && m.timestamp < d1).map((m) => m.mood);
    if (moods.length === 0) break;
    const avg = moods.reduce((a, b) => a + b, 0) / moods.length;
    if (avg <= NEGATIVE_MOOD_THRESHOLD) streak++;
    else break;
  }
  return streak;
}

export function activityCountInRange(logs: ActivityLogEntry[], startMs: number, endMs: number): number {
  return logs.filter((a) => a.timestamp >= startMs && a.timestamp < endMs).length;
}

export function stressFromMoodAvg(avgMood: number): number {
  if (avgMood <= 0) return 55;
  return Math.round(clamp(100 - ((avgMood - 1) / 9) * 100, 0, 100));
}

export function computeAverageStressPercent(
  moodLogs: MoodLogEntry[],
  stored: number | undefined,
  now = Date.now()
): number {
  if (typeof stored === 'number' && !Number.isNaN(stored)) {
    return clamp(stored > 10 ? stored : stored * 10, 0, 100);
  }
  const trend = moodTrendLast7Days(moodLogs, now);
  const withData = trend.filter((v) => v > 0);
  const avgMood = withData.length ? withData.reduce((a, b) => a + b, 0) / withData.length : 5;
  return stressFromMoodAvg(avgMood);
}

export function attendanceProxyPercent(
  activityLogs: ActivityLogEntry[],
  moodLogs: MoodLogEntry[],
  now = Date.now()
): number {
  const weekAgo = now - 7 * DAY_MS;
  const activeDays = new Set<number>();
  activityLogs.forEach((a) => {
    if (a.timestamp >= weekAgo) activeDays.add(dayStart(a.timestamp));
  });
  moodLogs.forEach((m) => {
    if (m.timestamp >= weekAgo) activeDays.add(dayStart(m.timestamp));
  });
  return Math.min(100, Math.round((activeDays.size / 7) * 100));
}

export function ruleBasedRisk(
  stressPercent: number,
  weeklyActivity: number,
  prevWeekActivity: number,
  negativeMoodStreak: number
): { level: RiskLevel; reasons: string[] } {
  const reasons: string[] = [];
  if (stressPercent > HIGH_STRESS) {
    reasons.push('Stress level is above 70%');
  }
  if (negativeMoodStreak >= 3) {
    reasons.push('Mood has been low for 3+ consecutive days');
  }
  const drop =
    prevWeekActivity >= 3 && weeklyActivity < prevWeekActivity * (1 - ACTIVITY_DROP_RATIO);
  if (drop) {
    reasons.push('App activity dropped significantly vs. last week');
  }

  if (stressPercent > HIGH_STRESS || negativeMoodStreak >= 3) {
    return { level: 'high', reasons };
  }
  if (drop || stressPercent > MEDIUM_STRESS) {
    return { level: 'medium', reasons: reasons.length ? reasons : ['Elevated stress or engagement change'] };
  }
  return { level: 'low', reasons: [] };
}

export function ruleBasedSuggestions(
  risk: RiskLevel,
  stressPercent: number,
  weeklyActivity: number,
  negativeMoodStreak: number
): string[] {
  const s: string[] = [];
  if (risk === 'high') {
    s.push('Schedule a check-in with student support or counselling.');
    s.push('Share coping resources and reduce academic load where possible.');
  } else if (risk === 'medium') {
    s.push('Send a gentle wellness nudge and mindfulness exercise suggestions.');
    s.push('Monitor for another week and review activity logs.');
  } else {
    s.push('Continue routine monitoring; reinforce positive habits.');
  }
  if (stressPercent > HIGH_STRESS) {
    s.push('Recommend short daily breathing or grounding sessions (3â€“5 minutes).');
  }
  if (weeklyActivity < 3) {
    s.push('Encourage re-engagement with one micro-goal in the app today.');
  }
  if (negativeMoodStreak >= 3) {
    s.push('Consider proactive outreach â€” sustained low mood increases follow-up priority.');
  }
  return [...new Set(s)].slice(0, 4);
}

export function stressTrendLabel(trend: number[]): 'worsening' | 'stable' | 'improving' {
  const vals = trend.filter((v) => v > 0);
  if (vals.length < 2) return 'stable';
  const half = Math.max(1, Math.floor(vals.length / 2));
  const earlyAvg = vals.slice(0, half).reduce((x, y) => x + y, 0) / half;
  const rest = vals.slice(half);
  const lateAvg = rest.length ? rest.reduce((x, y) => x + y, 0) / rest.length : earlyAvg;
  const diff = lateAvg - earlyAvg;
  if (diff <= -0.35) return 'worsening';
  if (diff >= 0.35) return 'improving';
  return 'stable';
}

export function activityLevelLabel(count: number): 'low' | 'moderate' | 'high' {
  if (count >= 14) return 'high';
  if (count >= 5) return 'moderate';
  return 'low';
}

export function getReportWeekRange(now = Date.now()): { weekStartDate: string; weekEndDate: string; startKey: string } {
  const start = subDays(startOfDay(now), 6);
  const end = startOfDay(now);
  const fmt = (t: number) =>
    new Date(t).toLocaleDateString('en-CA', { year: 'numeric', month: '2-digit', day: '2-digit' });
  const weekStartDate = fmt(start.getTime());
  const weekEndDate = fmt(end.getTime());
  return { weekStartDate, weekEndDate, startKey: weekStartDate };
}

export function mapFirestoreUser(id: string, data: Record<string, unknown>): StudentWithMetrics {
  const moodLogs = normalizeMoodLogs(data.moodLogs);
  const activityLogs = normalizeActivityLogs(data.activityLogs);
  const now = Date.now();
  const lastActive = toMillis(data.lastActive ?? data.lastActivity ?? data.updatedAt ?? data.createdAt);
  const storedStress =
    typeof data.stressLevel === 'number' ? (data.stressLevel as number) : undefined;
  const weeklyActivityCount = activityCountInRange(activityLogs, now - 7 * DAY_MS, now + 1);
  const previousWeekActivityCount = activityCountInRange(
    activityLogs,
    now - 14 * DAY_MS,
    now - 7 * DAY_MS
  );
  const moodTrend7d = moodTrendLast7Days(moodLogs, now);
  const stressLevel = computeAverageStressPercent(moodLogs, storedStress, now);
  const negativeStreak = consecutiveNegativeMoodDays(moodLogs, now);
  const { level: adminRiskLevel, reasons: alertReasons } = ruleBasedRisk(
    stressLevel,
    weeklyActivityCount,
    previousWeekActivityCount,
    negativeStreak
  );
  const aiSuggestions = ruleBasedSuggestions(
    adminRiskLevel,
    stressLevel,
    weeklyActivityCount,
    negativeStreak
  );
  const course = String(data.course ?? '');
  const semester = Number(data.year ?? data.semester ?? 1);
  const enrollment = toMillis(data.createdAt ?? data.enrollmentDate ?? lastActive);
  const attendancePct =
    typeof data.attendancePercentage === 'number'
      ? clamp(data.attendancePercentage as number, 0, 100)
      : attendanceProxyPercent(activityLogs, moodLogs, now);

  return {
    id,
    name: String(data.name ?? 'Unknown'),
    email: String(data.email ?? ''),
    course,
    semester: Number.isFinite(semester) ? semester : 1,
    enrollmentDate: enrollment,
    lastActivity: lastActive,
    profileImage: typeof data.profileImage === 'string' ? data.profileImage : undefined,
    moodLogs,
    activityLogs,
    moodTrend7d,
    previousWeekActivityCount,
    stressLevel,
    attendancePercentage: attendancePct,
    weeklyActiveCount: weeklyActivityCount,
    adminRiskLevel,
    alertReasons,
    aiSuggestions,
    negativeMoodStreakDays: negativeStreak,
  };
}

export function buildWeeklyReportForStudent(student: StudentWithMetrics, now = Date.now()): WeeklyReport {
  const { weekStartDate, weekEndDate, startKey } = getReportWeekRange(now);
  const trend = student.moodTrend7d ?? moodTrendLast7Days(student.moodLogs, now);
  const stLabel = stressTrendLabel(trend);
  const actLevel = activityLevelLabel(student.weeklyActiveCount);
  const meditationMinutes = student.activityLogs.reduce((s, a) => s + (a.durationMinutes ?? 0), 0);
  const meditationSessions = student.activityLogs.filter(
    (a) => (a.type === 'meditation' || a.type === 'session') && (a.durationMinutes ?? 0) > 0
  ).length;
  const aiMessages = student.activityLogs.filter((a) => a.type === 'ai' || a.type === 'chat').length;
  const wellness = clamp(
    Math.round(100 - student.stressLevel * 0.55 + Math.min(student.weeklyActiveCount * 1.5, 22)),
    0,
    100
  );
  const reportId = `${student.id}_${startKey}`;
  const avgStressOneToTen = Math.round((student.stressLevel / 10) * 10) / 10;

  return {
    reportId,
    studentId: student.id,
    studentName: student.name,
    weekStartDate,
    weekEndDate,
    generatedAt: now,
    totalClasses: 7,
    presentDays: Math.min(7, Math.ceil((student.attendancePercentage / 100) * 7)),
    absentDays: Math.max(0, 7 - Math.min(7, Math.ceil((student.attendancePercentage / 100) * 7))),
    lateDays: 0,
    attendancePercent: student.attendancePercentage,
    avgStressLevel: avgStressOneToTen,
    avgStressPercent: student.stressLevel,
    maxStressLevel: Math.min(10, avgStressOneToTen + 1.5),
    stressCheckInCount: student.moodLogs.filter((m) => m.timestamp >= now - 7 * DAY_MS).length,
    stressRiskFlag: student.stressLevel > HIGH_STRESS,
    totalMeditationMinutes: Math.round(meditationMinutes),
    meditationSessionCount: meditationSessions || Math.max(1, Math.floor(meditationMinutes / 10)),
    totalAiMessages: aiMessages || 0,
    aiChatSessionCount: Math.max(0, Math.floor(aiMessages / 5)),
    dominantTopic: 'wellness',
    wellnessScore: wellness,
    stressTrend: stLabel,
    activityLevel: actLevel,
    riskLevel: student.adminRiskLevel,
    aiSuggestions: student.aiSuggestions,
  };
}

export function mergeWeeklyReportPayload(
  computed: WeeklyReport,
  stored: Partial<WeeklyReport> | undefined
): WeeklyReport {
  if (!stored || Object.keys(stored).length === 0) return computed;
  return { ...computed, ...stored, reportId: computed.reportId, studentId: computed.studentId };
}

const CHART_LABELS_7 = ['âˆ’6d', 'âˆ’5d', 'âˆ’4d', 'âˆ’3d', 'âˆ’2d', 'âˆ’1d', 'Today'];

export function aggregateStressTrendChart(students: StudentWithMetrics[]): {
  labels: string[];
  values: number[];
} {
  const values = CHART_LABELS_7.map((_, dayIndex) => {
    const samples: number[] = [];
    students.forEach((s) => {
      const m = s.moodTrend7d?.[dayIndex] ?? 0;
      if (m > 0) samples.push(stressFromMoodAvg(m));
    });
    return samples.length
      ? Math.round(samples.reduce((a, b) => a + b, 0) / samples.length)
      : 0;
  });
  return { labels: CHART_LABELS_7, values };
}

export function aggregateActivityTrendChart(students: StudentWithMetrics[]): {
  labels: string[];
  values: number[];
} {
  const now = Date.now();
  const values = CHART_LABELS_7.map((_, dayIndex) => {
    const dayStartMs = dayStart(subDays(now, 6 - dayIndex).getTime());
    const dayEndMs = dayStartMs + DAY_MS;
    let sum = 0;
    students.forEach((s) => {
      sum += activityCountInRange(s.activityLogs, dayStartMs, dayEndMs);
    });
    return sum;
  });
  return { labels: CHART_LABELS_7, values };
}

export function aggregateMoodLineChart(students: StudentWithMetrics[]): number[] {
  return CHART_LABELS_7.map((_, dayIndex) => {
    const moods: number[] = [];
    students.forEach((s) => {
      const v = s.moodTrend7d?.[dayIndex] ?? 0;
      if (v > 0) moods.push(v);
    });
    return moods.length ? Math.round((moods.reduce((a, b) => a + b, 0) / moods.length) * 10) / 10 : 0;
  });
}

export function countHighRiskStudents(students: StudentWithMetrics[]): number {
  return students.filter((s) => s.adminRiskLevel === 'high').length;
}
