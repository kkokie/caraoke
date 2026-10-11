import { ReportReason } from '@/api/client';

/** What people pick from when reporting. Plain words, most common first. */
export const REPORT_REASONS: { reason: ReportReason; label: string }[] = [
  { reason: 'SPAM', label: 'Spam or a scam' },
  { reason: 'HARASSMENT', label: 'Bullying or harassment' },
  { reason: 'HATE', label: 'Hate speech or symbols' },
  { reason: 'SEXUAL', label: 'Sexual content' },
  { reason: 'VIOLENCE', label: 'Violence or threats' },
  { reason: 'SELF_HARM', label: 'Suicide or self-harm' },
  { reason: 'IMPERSONATION', label: 'Pretending to be someone else' },
  { reason: 'OTHER', label: 'Something else' },
];
