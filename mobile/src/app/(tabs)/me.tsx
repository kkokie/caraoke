import { ProfileScreen } from '@/features/profile/ProfileScreen';
import { useSession } from '@/features/session/useSession';

/** Profile tab: your own dashboard. */
export default function MyProfileTab() {
  const session = useSession();
  if (session.status !== 'ready') return null;
  return <ProfileScreen handle={session.profile.handle} edges={['top', 'left', 'right']} />;
}
