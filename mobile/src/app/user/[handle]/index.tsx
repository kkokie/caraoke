import { useLocalSearchParams } from 'expo-router';
import { ProfileScreen } from '@/features/profile/ProfileScreen';

/** Anyone's profile, opened by tapping an author. */
export default function UserProfileScreen() {
  const { handle } = useLocalSearchParams<{ handle: string }>();
  return <ProfileScreen handle={handle} />;
}
