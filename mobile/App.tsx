import { StatusBar } from 'expo-status-bar';
import { useEffect, useState } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { api, ApiError, PublicProfile } from './src/api/client';

// Phase 0 smoke screen: proves the app can reach the API and resolve the signed-in user.
// Replaced by Expo Router screens (feed, song page, onboarding) in Phase 1.

type State =
  | { kind: 'loading' }
  | { kind: 'error'; message: string }
  | { kind: 'needsProfile' }
  | { kind: 'ready'; profile: PublicProfile };

export default function App() {
  const [state, setState] = useState<State>({ kind: 'loading' });

  useEffect(() => {
    (async () => {
      try {
        await api.health();
        const profile = await api.me();
        setState({ kind: 'ready', profile });
      } catch (e) {
        if (e instanceof ApiError && e.status === 404) {
          setState({ kind: 'needsProfile' });   // signed in, no handle yet -> onboarding
        } else {
          setState({ kind: 'error', message: e instanceof Error ? e.message : String(e) });
        }
      }
    })();
  }, []);

  return (
    <View style={styles.container}>
      {state.kind === 'loading' && <ActivityIndicator />}
      {state.kind === 'error' && <Text style={styles.error}>API unreachable: {state.message}</Text>}
      {state.kind === 'needsProfile' && <Text>Connected. Next: pick your @handle.</Text>}
      {state.kind === 'ready' && (
        <Text>
          Signed in as @{state.profile.handle} ({state.profile.displayName})
        </Text>
      )}
      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff', alignItems: 'center', justifyContent: 'center', padding: 24 },
  error: { color: '#b00020', textAlign: 'center' },
});
