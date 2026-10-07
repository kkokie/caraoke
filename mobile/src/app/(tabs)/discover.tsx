import { Text, View } from 'react-native';
import { router } from 'expo-router';
import { Screen } from '@/components/Screen';
import { Button } from '@/components/Button';
import { useSession } from '@/features/session/useSession';
import { makeStyles, space, useTheme } from '@/theme';

// Home tab. Becomes a real feed later; for now the front door into songs.
export default function HomeScreen() {
  const styles = useStyles();
  const { type } = useTheme();
  const session = useSession();
  if (session.status !== 'ready') return null;   // guard in _layout redirects; this just narrows the type

  return (
    <Screen>
      <View style={styles.header}>
        <Text style={type.title}>What are you{'\n'}listening to?</Text>
        <Text style={type.subtitle}>
          Hey {session.profile.displayName}. Find a song, read what it means to people, and share what it means to you.
        </Text>
      </View>

      <Button label="Find a song" onPress={() => router.push('/search')} />
    </Screen>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  header: { gap: space.sm, marginTop: space.xl },
}));
