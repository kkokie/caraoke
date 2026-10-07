import { useLocalSearchParams } from 'expo-router';
import { api } from '@/api/client';
import { PeopleList } from '@/features/people/PeopleList';
import { usePeople } from '@/features/people/usePeople';

export default function FollowingScreen() {
  const { handle } = useLocalSearchParams<{ handle: string }>();
  const state = usePeople(() => api.getFollowing(handle), `following:${handle}`);
  return <PeopleList state={state} empty="Not following anyone yet." />;
}
