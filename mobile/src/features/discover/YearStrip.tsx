import { FlatList, Pressable, Text } from 'react-native';
import { router } from 'expo-router';
import { YearCount } from '@/api/client';
import { makeStyles, space } from '@/theme';

/** "Dig through the years": every year that has stories, newest first. Tap one to open it. */
export function YearStrip({ years }: { years: YearCount[] }) {
  const styles = useStyles();
  if (years.length === 0) return null;
  return (
    <FlatList
      horizontal
      data={years}
      keyExtractor={(y) => String(y.year)}
      showsHorizontalScrollIndicator={false}
      contentContainerStyle={styles.row}
      renderItem={({ item }) => (
        <Pressable
          accessibilityRole="link"
          accessibilityLabel={`${item.year}, ${item.stories} ${item.stories === 1 ? 'story' : 'stories'}`}
          onPress={() => router.push({ pathname: '/year/[year]', params: { year: String(item.year) } })}
          style={({ pressed }) => [styles.year, pressed && styles.pressed]}>
          <Text style={styles.label}>’{String(item.year).slice(2)}</Text>
          <Text style={styles.count}>{item.stories}</Text>
        </Pressable>
      )}
    />
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  row: { gap: space.md, paddingRight: space.lg },
  year: { alignItems: 'center', minWidth: 52, minHeight: 56, justifyContent: 'center' },
  pressed: { opacity: 0.6 },
  label: { fontFamily: type.story.fontFamily, fontSize: 34, lineHeight: 40, color: colors.accent },
  count: { ...type.mono, fontSize: 11 },
}));
