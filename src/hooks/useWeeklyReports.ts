import { useEffect, useMemo, useState, useCallback } from 'react';
import { firestore } from '../firebase/config';
import { collection, doc, onSnapshot, setDoc, serverTimestamp } from 'firebase/firestore';
import { getRiskLevel, type WeeklyReport } from '../types/index';
import { useStudents } from './useFirebaseStudent';
import { buildWeeklyReportForStudent, mergeWeeklyReportPayload } from '@utils/studentAnalytics';

export const useWeeklyReports = () => {
  const { students, loading: studentsLoading, error: studentsError } = useStudents();
  const [firestoreMap, setFirestoreMap] = useState<Record<string, WeeklyReport>>({});
  const [frLoading, setFrLoading] = useState(true);
  const [frError, setFrError] = useState<string | null>(null);

  useEffect(() => {
    const unsub = onSnapshot(
      collection(firestore, 'weeklyReports'),
      (snap) => {
        const map: Record<string, WeeklyReport> = {};
        snap.forEach((d) => {
          const data = d.data() as WeeklyReport;
          map[d.id] = { ...data, reportId: d.id };
        });
        setFirestoreMap(map);
        setFrLoading(false);
        setFrError(null);
      },
      (err) => {
        setFrError(err.message);
        setFrLoading(false);
      }
    );
    return () => unsub();
  }, []);

  const reports = useMemo(() => {
    const now = Date.now();
    return students.map((s) => {
      const computed = buildWeeklyReportForStudent(s, now);
      return mergeWeeklyReportPayload(computed, firestoreMap[computed.reportId]);
    });
  }, [students, firestoreMap]);

  return {
    reports,
    loading: studentsLoading || frLoading,
    error: studentsError || frError,
  };
};

export const useStudentReports = (studentId: string) => {
  const { reports, loading, error } = useWeeklyReports();
  const list = useMemo(() => reports.filter((r) => r.studentId === studentId), [reports, studentId]);
  return { reports: list, loading, error };
};

export const useReportActions = () => {
  const [saving, setSaving] = useState(false);

  const updateAdminNote = useCallback(async (reportId: string, studentId: string, note: string) => {
    setSaving(true);
    try {
      await setDoc(
        doc(firestore, 'weeklyReports', reportId),
        {
          reportId,
          studentId,
          adminNote: note,
          updatedAt: serverTimestamp(),
        },
        { merge: true }
      );
    } catch (err) {
      console.error('Failed to update admin note:', err);
      throw err;
    } finally {
      setSaving(false);
    }
  }, []);

  return { updateAdminNote, saving };
};

export interface ReportSummary {
  totalStudents: number;
  highRiskCount: number;
  mediumRiskCount: number;
  avgWellness: number;
  lowAttendCount: number;
}

export const computeSummary = (reports: WeeklyReport[]): ReportSummary => {
  const latestByStudent = new Map<string, WeeklyReport>();
  reports.forEach((r) => {
    const prev = latestByStudent.get(r.studentId);
    if (!prev || (r.generatedAt ?? 0) > (prev.generatedAt ?? 0)) {
      latestByStudent.set(r.studentId, r);
    }
  });
  const list = [...latestByStudent.values()];
  return {
    totalStudents: list.length,
    highRiskCount: list.filter((r) => getRiskLevel(r) === 'high').length,
    mediumRiskCount: list.filter((r) => getRiskLevel(r) === 'medium').length,
    avgWellness: list.length
      ? Math.round(list.reduce((s, r) => s + r.wellnessScore, 0) / list.length)
      : 0,
    lowAttendCount: list.filter((r) => r.attendancePercent < 75).length,
  };
};
