import { Pressable, Text, View } from 'react-native';
import { router } from 'expo-router';
import { Story } from '@/api/client';
import { makeStyles, space } from '@/theme';
import { useResonance } from './useResonance';

/**
 * Bottom row of a story card:  [♡ I felt this too]          12 felt this ›
 * Your own story shows only the count, since you can't resonate with yourself.
 */
export function ResonateRow({ story }: { story: Story }) {
  const styles = useStyles();
  const { count, mine, toggle } = useResonance(story);
  const openPeople = () => router.push({ pathname: '/story/[id]/felt', params: { id: String(story.id) } });

  return (
    <View style={styles.row}>
      {story.mine ? <View /> : <FeltButton active={mine} onPress={toggle} />}
      {count > 0 ? (
        <Pressable accessibilityRole="link" onPress={openPeople} hitSlop={8}>
          <Text style={styles.count}>{countLabel(count, story.mine)} ›</Text>
        </Pressable>
      ) : null}
    </View>
  );
}

function FeltButton({ active, onPress }: { active: boolean; onPress: () => void }) {
  const styles = useStyles();
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ selected: active }}
      accessibilityLabel={active ? 'You felt this too. Tap to undo.' : 'I felt this too'}
      onPress={onPress}
      hitSlop={8}
      style={({ pressed }) => [styles.pill, active && styles.pillActive, pressed && styles.pressed]}>
      <Text style={[styles.pillText, active && styles.pillTextActive]}>
        {active ? '♥ You felt this' : '♡ I felt this too'}
      </Text>
    </Pressable>
  );
}

function countLabel(count: number, isMyStory: boolean): string {
  if (isMyStory) return count === 1 ? '1 person felt this' : `${count} people felt this`;
  return `${count} felt this`;
}

const useStyles = makeStyles(({ colors, type }) => ({
  row: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: space.xs },
  pill: {
    paddingHorizontal: space.md,
    paddingVertical: 6,
    borderRadius: 999,
    borderWidth: 1,
    borderColor: colors.border,
  },
  pillActive: { backgroundColor: colors.accent, borderColor: colors.accent },
  pressed: { opacity: 0.7 },
  pillText: { ...type.hint, color: colors.text, fontWeight: '600' },
  pillTextActive: { color: colors.accentText },
  count: { ...type.hint, color: colors.textMuted },
}));
