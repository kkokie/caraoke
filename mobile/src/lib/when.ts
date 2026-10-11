/** "TUESDAY NIGHT", "SUNDAY MORNING": the liner-notes timestamp at the top of Discover. */
export function partOfWeek(now: Date = new Date()): string {
  const day = now.toLocaleDateString('en-US', { weekday: 'long' }).toUpperCase();
  const h = now.getHours();
  const part = h < 5 ? 'NIGHT' : h < 12 ? 'MORNING' : h < 17 ? 'AFTERNOON' : h < 21 ? 'EVENING' : 'NIGHT';
  return `${day} ${part}`;
}
