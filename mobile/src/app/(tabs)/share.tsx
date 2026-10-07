import { Redirect } from 'expo-router';

/**
 * The center "+" tab never shows a screen of its own: its button opens the share flow
 * (see the tabs layout). This is only reached if someone deep-links here.
 */
export default function ShareTab() {
  return <Redirect href="/search" />;
}
