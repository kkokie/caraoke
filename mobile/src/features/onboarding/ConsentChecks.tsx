import { ReactNode } from 'react';
import { Pressable, Text, View } from 'react-native';
import { openLegal } from '@/lib/legal';
import { makeStyles, radius, space } from '@/theme';

type Props = {
  over13: boolean;
  setOver13: (v: boolean) => void;
  agreed: boolean;
  setAgreed: (v: boolean) => void;
};

/** The two things everyone confirms before joining: they're 13+, and they agree to the rules. */
export function ConsentChecks({ over13, setOver13, agreed, setAgreed }: Props) {
  const styles = useStyles();
  return (
    <View style={styles.wrap}>
      <Check checked={over13} onToggle={() => setOver13(!over13)} label="I’m 13 or older" />
      <Check checked={agreed} onToggle={() => setAgreed(!agreed)}>
        <Text style={styles.text}>
          I agree to the{' '}
          <Text style={styles.link} onPress={() => openLegal('terms')} accessibilityRole="link">Terms</Text>
          {' '}and{' '}
          <Text style={styles.link} onPress={() => openLegal('guidelines')} accessibilityRole="link">Community Guidelines</Text>
          , and I’ve read the{' '}
          <Text style={styles.link} onPress={() => openLegal('privacy')} accessibilityRole="link">Privacy Policy</Text>.
        </Text>
      </Check>
    </View>
  );
}

function Check({ checked, onToggle, label, children }: {
  checked: boolean; onToggle: () => void; label?: string; children?: ReactNode;
}) {
  const styles = useStyles();
  return (
    <Pressable accessibilityRole="checkbox" accessibilityState={{ checked }} onPress={onToggle} style={styles.row}>
      <View style={[styles.box, checked && styles.boxOn]}>{checked ? <Text style={styles.tick}>✓</Text> : null}</View>
      <View style={styles.flex}>{label ? <Text style={styles.text}>{label}</Text> : children}</View>
    </Pressable>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.md },
  row: { flexDirection: 'row', gap: space.md, alignItems: 'flex-start', minHeight: 44 },
  box: {
    width: 24,
    height: 24,
    marginTop: 1,
    borderRadius: radius.md / 2,
    borderWidth: 2,
    borderColor: colors.border,
    alignItems: 'center',
    justifyContent: 'center',
  },
  boxOn: { backgroundColor: colors.accent, borderColor: colors.accent },
  tick: { color: colors.accentText, fontSize: 14, fontWeight: '700' },
  flex: { flex: 1 },
  text: { ...type.body, fontSize: 15, lineHeight: 22 },
  link: { color: colors.accent, textDecorationLine: 'underline' },
}));
