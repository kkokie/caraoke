import { ColorValue, StyleSheet, Text } from 'react-native';
import { Tabs } from 'expo-router/js-tabs';
import { colors } from '@/theme';

/** Bottom tabs: Home (discover songs) and Profile (your dashboard). */
export default function TabsLayout() {
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
        name="home"
        options={{ title: 'Home', tabBarIcon: ({ color }) => <Glyph char="♪" color={color} /> }}
      />
      <Tabs.Screen
        name="me"
        options={{ title: 'Profile', tabBarIcon: ({ color }) => <Glyph char="◉" color={color} /> }}
      />
    </Tabs>
  );
}

/** Text glyphs as icons, so we don't need an icon package yet. */
function Glyph({ char, color }: { char: string; color: ColorValue }) {
  return <Text style={[styles.glyph, { color }]}>{char}</Text>;
}

const styles = StyleSheet.create({
  glyph: { fontSize: 20 },
});
