import { useEffect, useState } from 'react';
import { firestore } from '../firebase/config';
import { collection, doc, onSnapshot } from 'firebase/firestore';
import type { StudentWithMetrics } from '../types/index';
import { mapFirestoreUser } from '@utils/studentAnalytics';

export function useStudents() {
  const [students, setStudents] = useState<StudentWithMetrics[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    try {
      const unsubscribe = onSnapshot(
        collection(firestore, 'users'),
        (snapshot) => {
          const studentList: StudentWithMetrics[] = snapshot.docs.map((d) =>
            mapFirestoreUser(d.id, d.data() as Record<string, unknown>)
          );
          setStudents(studentList);
          setLoading(false);
          setError(null);
        },
        (err) => {
          setError(err.message);
          setLoading(false);
        }
      );

      return () => unsubscribe();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to subscribe to users');
      setLoading(false);
    }
  }, []);

  return { students, loading, error };
}

export function useStudentById(studentId: string | null) {
  const [student, setStudent] = useState<StudentWithMetrics | null>(null);
  const [loading, setLoading] = useState(!!studentId);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!studentId) {
      setStudent(null);
      setLoading(false);
      return;
    }

    setLoading(true);
    const docRef = doc(firestore, 'users', studentId);
    const unsubscribe = onSnapshot(
      docRef,
      (snap) => {
        if (snap.exists()) {
          setStudent(mapFirestoreUser(snap.id, snap.data() as Record<string, unknown>));
        } else {
          setStudent(null);
        }
        setLoading(false);
        setError(null);
      },
      (err) => {
        setError(err.message);
        setLoading(false);
      }
    );
    return () => unsubscribe();
  }, [studentId]);

  return { student, loading, error };
}
