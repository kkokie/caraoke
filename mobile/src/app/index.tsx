import { Redirect } from 'expo-router';
import { useSession } from '@/features/session/useSession';

/** Entry URL "/": send the user wherever their session says they belong. */
export default function Index() {
  const session = useSession();
  return <Redirect href={session.status === 'ready' ? '/discover' : '/onboarding'} />;
}
