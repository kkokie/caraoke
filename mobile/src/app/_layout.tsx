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
        <Stack.Screen name="(tabs)" />
        <Stack.Screen name="search" options={{ ...pushedScreen, title: 'Search' }} />
        <Stack.Screen name="song/[id]/index" options={{ ...pushedScreen, title: '' }} />
        <Stack.Screen name="song/[id]/compose" options={{ ...pushedScreen, title: 'Your story' }} />
        <Stack.Screen name="story/[id]/felt" options={{ ...pushedScreen, title: 'Felt this too' }} />
        <Stack.Screen name="story/[id]/index" options={{ ...pushedScreen, title: 'Story' }} />
        <Stack.Screen name="story/[id]/edit" options={{ ...pushedScreen, title: 'Edit story' }} />
        <Stack.Screen name="user/[handle]/index" options={{ ...pushedScreen, title: '' }} />
        <Stack.Screen name="user/[handle]/followers" options={{ ...pushedScreen, title: 'Followers' }} />
        <Stack.Screen name="user/[handle]/following" options={{ ...pushedScreen, title: 'Following' }} />
        <Stack.Screen name="profile/edit" options={{ ...pushedScreen, title: 'Edit profile' }} />
      </Stack.Protected>
    </Stack>
  );
}

// Every screen you navigate into gets a back arrow top-left (no swipe-down sheets),
// and still supports the edge swipe back.
const pushedScreen = {
  headerShown: true,
  headerStyle: { backgroundColor: colors.bg },
  headerTintColor: colors.accent,
  headerTitleStyle: { color: colors.text },
  headerShadowVisible: false,
  headerBackButtonDisplayMode: 'minimal' as const,
};
