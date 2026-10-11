import { AccountStanding } from '@/api/client';

/** What we tell someone whose account isn't in good standing. Null = nothing to say. */
export function standingText(s: AccountStanding | null): { title: string; body: string } | null {
  if (!s) return null;
  switch (s.standing) {
    case 'WARNED':
      return {
        title: 'A warning on your account',
        body: 'A story of yours broke the Community Guidelines and was removed. Another strike pauses sharing for a week.',
      };
    case 'SUSPENDED':
      return {
        title: `Sharing is paused until ${day(s.suspendedUntil)}`,
        body: 'Your account broke the Community Guidelines more than once. You can still read and listen.',
      };
    case 'BANNED':
      return {
        title: 'This account was banned',
        body: 'It broke the Community Guidelines. You can still delete it from Edit profile.',
      };
    default:
      return null;
  }
}

function day(iso: string | null): string {
  return iso ? new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : 'later';
}
