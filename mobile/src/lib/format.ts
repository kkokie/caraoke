/** 223 -> "3:43" */
export function formatDuration(totalSec: number | null | undefined): string {
  if (totalSec == null || totalSec < 0) return '';
  const min = Math.floor(totalSec / 60);
  const sec = Math.floor(totalSec % 60);
  return `${min}:${sec.toString().padStart(2, '0')}`;
}

/** "2:14" -> 134, "45" -> 45, anything else -> null */
export function parseDuration(text: string): number | null {
  const t = text.trim();
  const match = /^(?:(\d{1,2}):)?(\d{1,2})$/.exec(t);
  if (!match) return null;
  const min = match[1] ? Number(match[1]) : 0;
  const sec = Number(match[2]);
  if (match[1] && sec > 59) return null;   // "2:75" isn't a time
  return min * 60 + sec;
}

/** ISO timestamp -> "just now", "5m", "3h", "2d", or "Mar 4" / "Mar 4, 2024" */
export function timeAgo(iso: string, now: Date = new Date()): string {
  const then = new Date(iso);
  const sec = Math.max(0, Math.floor((now.getTime() - then.getTime()) / 1000));
  if (sec < 60) return 'just now';
  if (sec < 3600) return `${Math.floor(sec / 60)}m`;
  if (sec < 86400) return `${Math.floor(sec / 3600)}h`;
  if (sec < 7 * 86400) return `${Math.floor(sec / 86400)}d`;
  const sameYear = then.getFullYear() === now.getFullYear();
  return then.toLocaleDateString(undefined, { month: 'short', day: 'numeric', ...(sameYear ? {} : { year: 'numeric' }) });
}
