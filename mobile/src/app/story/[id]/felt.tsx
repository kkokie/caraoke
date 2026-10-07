import { useLocalSearchParams } from 'expo-router';
import { api } from '@/api/client';
import { PeopleList } from '@/features/people/PeopleList';
import { usePeople } from '@/features/people/usePeople';

/** The people who felt a story too: the "connect through the same stories" moment. */
export default function FeltThisScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const state = usePeople(() => api.getResonators(Number(id)), `felt:${id}`);
  return <PeopleList state={state} header="They’ve been there too." empty="No one yet." />;
}
