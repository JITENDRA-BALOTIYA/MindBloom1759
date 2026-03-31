/** Normalize Firestore Timestamp, number (sec/ms), Date, or ISO string to epoch ms */

export function toMillis(v: unknown): number {
  if (v == null) return Date.now();
  if (typeof v === 'number' && !Number.isNaN(v)) {
    return v < 1e12 ? Math.round(v * 1000) : v;
  }
  if (v instanceof Date) return v.getTime();
  if (typeof v === 'object' && v !== null && 'seconds' in v) {
    const t = v as { seconds: number; nanoseconds?: number };
    return t.seconds * 1000 + Math.floor((t.nanoseconds ?? 0) / 1e6);
  }
  if (typeof v === 'string') {
    const n = Date.parse(v);
    return Number.isNaN(n) ? Date.now() : n;
  }
  return Date.now();
}

export function clamp(n: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, n));
}
