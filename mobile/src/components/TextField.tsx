import { ReactNode } from 'react';
import { StyleSheet, Text, TextInput, TextInputProps, View } from 'react-native';
import { colors, radius, space, type } from '@/theme';

type Props = TextInputProps & {
  label: string;
  prefix?: string;     // e.g. "@" for handles
  footer?: ReactNode;  // hint / validation line under the input
};

export function TextField({ label, prefix, footer, style, ...input }: Props) {
  return (
    <View style={styles.wrap}>
      <Text style={type.label}>{label.toUpperCase()}</Text>
      <View style={styles.box}>
        {prefix ? <Text style={styles.prefix}>{prefix}</Text> : null}
        <TextInput
          placeholderTextColor={colors.textMuted}
          selectionColor={colors.accent}
          style={[styles.input, style]}
          {...input}
        />
      </View>
      {footer}
    </View>
  );
}

const styles = StyleSheet.create({
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
});
