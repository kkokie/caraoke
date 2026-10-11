import { Text } from 'react-native';
import { makeStyles } from '@/theme';

/** A block's small caps title, in the page's type pairing. */
export function BlockTitle({ children }: { children: string }) {
  const styles = useStyles();
  return <Text style={styles.title}>{children}</Text>;
}

const useStyles = makeStyles(({ type }) => ({
  title: { ...type.label },
}));
