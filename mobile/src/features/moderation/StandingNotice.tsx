import { Text, View } from 'react-native';
import { Button } from '@/components/Button';
import { openLegal } from '@/lib/legal';
import { makeStyles, radius, space } from '@/theme';
import { standingText } from './standingText';
import { useStanding } from './useStanding';

/** A quiet card at the top of your page when your account has a warning, suspension or ban. */
export function StandingNotice() {
  const styles = useStyles();
  const text = standingText(useStanding());
  if (!text) return null;

  return (
    <View style={styles.card} accessibilityRole="alert">
      <Text style={styles.title}>{text.title}</Text>
      <Text style={styles.body}>{text.body}</Text>
      <Button label="Community Guidelines" variant="ghost" onPress={() => openLegal('guidelines')} />
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  card: {
    gap: space.sm,
    padding: space.md,
    marginBottom: space.md,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.danger,
    backgroundColor: colors.surface,
  },
  title: { ...type.body, fontWeight: '600' },
  body: { ...type.hint },
}));
