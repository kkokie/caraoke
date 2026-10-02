import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { SessionProvider } from '@/features/session/SessionProvider';
import { useSession } from '@/features/session/useSession';
import { SplashState } from '@/components/SplashState';
import { colors } from '@/theme';

export default function RootLayout() {
  return (
    <SessionProvider>
      <StatusBar style="light" />
      <RootNavigator />
    </SessionProvider>
  );
}

/**
 * Routing is driven by session state, not by screens pushing each other:
 * no profile -> only /onboarding is reachable; profile -> only /home.
 * Expo Router redirects automatically when a guard flips.
 */
function RootNavigator() {
  const session = useSession();

  if (session.status === 'loading') return <SplashState />;
  if (session.status === 'error') return <SplashState error={session.message} onRetry={session.reload} />;

  return (
    <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: colors.bg } }}>
      <Stack.Screen name="index" />
      <Stack.Protected guard={session.status === 'needsProfile'}>
        <Stack.Screen name="onboarding" />
      </Stack.Protected>
      <Stack.Protected guard={session.status === 'ready'}>
        <Stack.Screen name="home" />
        <Stack.Screen name="search" options={{ ...pushedScreen, title: 'Search' }} />
        <Stack.Screen name="song/[id]" options={{ ...pushedScreen, title: '' }} />
      </Stack.Protected>
    </Stack>
  );
}

// Screens you navigate into get a minimal dark header with a back arrow
const pushedScreen = {
  headerShown: true,
  headerStyle: { backgroundColor: colors.bg },
  headerTintColor: colors.accent,
  headerTitleStyle: { color: colors.text },
  headerShadowVisible: false,
  headerBackButtonDisplayMode: 'minimal' as const,
};
