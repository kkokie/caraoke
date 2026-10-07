import { Image, Modal, Pressable, Text, useWindowDimensions, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { mediaUrl } from '@/api/client';
import { makeStyles, space } from '@/theme';

type Props = { url: string | null | undefined; visible: boolean; onClose: () => void; label?: string };

/** Full-screen photo with a close button top-left. Tapping anywhere also closes it. */
export function PhotoViewer({ url, visible, onClose, label }: Props) {
  const styles = useStyles();
  const { width } = useWindowDimensions();
  const uri = mediaUrl(url);
  if (!uri) return null;

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose} statusBarTranslucent>
      <Pressable style={styles.backdrop} onPress={onClose} accessibilityLabel="Close photo">
        <SafeAreaView style={styles.safe}>
          <Pressable onPress={onClose} hitSlop={12} style={styles.close} accessibilityRole="button" accessibilityLabel="Close">
            <Text style={styles.closeText}>✕</Text>
          </Pressable>
          <View style={styles.center}>
            <Image source={{ uri }} style={{ width, height: width }} resizeMode="contain" accessibilityLabel={label} />
          </View>
        </SafeAreaView>
      </Pressable>
    </Modal>
  );
}

const useStyles = makeStyles(({ colors, type }) => ({
  backdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.94)' },
  safe: { flex: 1 },
  close: { alignSelf: 'flex-start', padding: space.md },
  closeText: { color: '#FFFFFF', fontSize: 22 },   // always on the black backdrop
  center: { flex: 1, justifyContent: 'center' },
}));
