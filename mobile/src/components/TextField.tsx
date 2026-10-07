import { ReactNode } from 'react';
import { Text, TextInput, TextInputProps, View } from 'react-native';
import { makeStyles, radius, space, useTheme } from '@/theme';

type Props = TextInputProps & {
  label: string;
  prefix?: string;     // e.g. "@" for handles
  footer?: ReactNode;  // hint / validation line under the input
};

export function TextField({ label, prefix, footer, style, ...input }: Props) {
  const styles = useStyles();
  const { colors, type, scheme } = useTheme();
  return (
    <View style={styles.wrap}>
      <Text style={type.label}>{label.toUpperCase()}</Text>
      <View style={styles.box}>
        {prefix ? <Text style={styles.prefix}>{prefix}</Text> : null}
        <TextInput
          placeholderTextColor={colors.textMuted}
          selectionColor={colors.accent}
          keyboardAppearance={scheme}
          style={[styles.input, style]}
          {...input}
        />
      </View>
      {footer}
    </View>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  wrap: { gap: space.sm },
  box: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: radius.md,
    paddingHorizontal: space.md,
  },
  prefix: { ...type.body, color: colors.textMuted, marginRight: 2 },
  input: { flex: 1, ...type.body, paddingVertical: 14 },
}));
