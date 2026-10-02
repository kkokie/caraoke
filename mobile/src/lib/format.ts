/** 223 -> "3:43" */
export function formatDuration(totalSec: number | null | undefined): string {
  if (totalSec == null || totalSec < 0) return '';
  const min = Math.floor(totalSec / 60);
  const sec = Math.floor(totalSec % 60);
  return `${min}:${sec.toString().padStart(2, '0')}`;
}
