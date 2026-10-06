import { Pressable, StyleSheet, Text, View } from 'react-native';
import { colors, space, type } from '@/theme';

export type GridTab = 'stories' | 'felt';

/** "Stories | Felt" switcher above your own grid. Felt is private, so others never see this. */
export function GridTabs({ tab, onChange }: { tab: GridTab; onChange: (t: GridTab) => void }) {
  return (
    <View style={styles.row} accessibilityRole="tablist">
      <TabButton label="Stories" active={tab === 'stories'} onPress={() => onChange('stories')} />
      <TabButton label="♥ Felt" active={tab === 'felt'} onPress={() => onChange('felt')} />
    </View>
  );
}

function TabButton({ label, active, onPress }: { label: string; active: boolean; onPress: () => void }) {
  return (
    <Pressable accessibilityRole="tab" accessibilityState={{ selected: active }} onPress={onPress}
      style={[styles.tab, active && styles.active]}>
      <Text style={[styles.label, active && styles.activeLabel]}>{label}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', borderBottomWidth: 1, borderBottomColor: colors.border, marginTop: space.sm },
  tab: { flex: 1, alignItems: 'center', paddingVertical: space.sm, borderBottomWidth: 2, borderBottomColor: 'transparent' },
  active: { borderBottomColor: colors.accent },
  label: { ...type.hint, fontWeight: '600', color: colors.textMuted },
  activeLabel: { color: colors.text },
});
