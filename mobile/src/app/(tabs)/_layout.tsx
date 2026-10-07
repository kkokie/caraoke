import { Pressable, View } from 'react-native';
import { router } from 'expo-router';
import { Tabs } from 'expo-router/js-tabs';
import { DiscoverIcon, PlusIcon, YouIcon } from '@/components/icons';
import { makeStyles, useTheme } from '@/theme';

/** Bottom tabs: Discover · ＋ (share a story) · You. */
export default function TabsLayout() {
  const { colors } = useTheme();
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        sceneStyle: { backgroundColor: colors.bg },
        tabBarStyle: { backgroundColor: colors.bg, borderTopColor: colors.border },
        tabBarActiveTintColor: colors.accent,
        tabBarInactiveTintColor: colors.textMuted,
      }}>
      <Tabs.Screen
        name="discover"
        options={{ title: 'Discover', tabBarIcon: ({ color }) => <DiscoverIcon color={String(color)} /> }}
      />
      <Tabs.Screen
        name="share"
        options={{ title: 'Share a story', tabBarButton: () => <ShareButton /> }}
      />
      <Tabs.Screen
        name="me"
        options={{ title: 'You', tabBarIcon: ({ color }) => <YouIcon color={String(color)} /> }}
      />
    </Tabs>
  );
}

/** The raised amber "+" in the middle. Starts sharing from anywhere. */
function ShareButton() {
  const styles = useStyles();
  const { colors } = useTheme();
  return (
    <View style={styles.slot}>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel="Share a story"
        onPress={() => router.push({ pathname: '/search', params: { intent: 'share' } })}
        style={({ pressed }) => [styles.share, pressed && styles.pressed]}>
        <PlusIcon color={colors.accentText} />
      </Pressable>
    </View>
  );
}

const useStyles = makeStyles(({ colors }) => ({
  slot: { flex: 1, alignItems: 'center' },
  share: {
    width: 56,
    height: 56,
    borderRadius: 28,
    marginTop: -14,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.accent,
  },
  pressed: { opacity: 0.85 },
}));
