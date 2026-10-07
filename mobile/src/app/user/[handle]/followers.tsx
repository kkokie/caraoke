import { useLocalSearchParams } from 'expo-router';
import { api } from '@/api/client';
import { PeopleList } from '@/features/people/PeopleList';
import { usePeople } from '@/features/people/usePeople';

export default function FollowersScreen() {
  const { handle } = useLocalSearchParams<{ handle: string }>();
  const state = usePeople(() => api.getFollowers(handle), `followers:${handle}`);
  return <PeopleList state={state} empty="No followers yet." />;
}
