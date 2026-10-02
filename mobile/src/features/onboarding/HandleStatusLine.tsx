import { StyleSheet, Text } from 'react-native';
import { colors, type } from '@/theme';
import { HandleStatus } from './useHandleAvailability';

/** The one-line status under the handle input. */
export function HandleStatusLine({ status }: { status: HandleStatus }) {
  const { text, color } = describe(status);
  return <Text style={[styles.line, { color }]}>{text}</Text>;
}

function describe(status: HandleStatus): { text: string; color: string } {
  switch (status.kind) {
    case 'empty':
      return { text: 'This is how people find you. You can’t change it later.', color: colors.textMuted };
    case 'invalid':
      return { text: status.reason, color: colors.danger };
    case 'checking':
      return { text: 'Checking…', color: colors.textMuted };
    case 'available':
      return { text: `@${status.handle} is yours if you want it.`, color: colors.success };
    case 'taken':
      return { text: status.reason, color: colors.danger };
    case 'error':
      return { text: 'Couldn’t check right now. Try again in a moment.', color: colors.danger };
  }
}

const styles = StyleSheet.create({
  line: type.hint,
});
