import { Text } from 'react-native';
import { makeStyles, Palette, useTheme } from '@/theme';
import { HandleStatus } from './useHandleAvailability';

/** The one-line status under the handle input. */
export function HandleStatusLine({ status }: { status: HandleStatus }) {
  const styles = useStyles();
  const { colors } = useTheme();
  const { text, color } = describe(status, colors);
  return <Text style={[styles.line, { color }]}>{text}</Text>;
}

function describe(status: HandleStatus, colors: Palette): { text: string; color: string } {
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

const useStyles = makeStyles(({ colors, type }) => ({
  line: type.hint,
}));
