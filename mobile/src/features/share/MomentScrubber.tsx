import { useRef, useState } from 'react';
import { GestureResponderEvent, LayoutChangeEvent, View } from 'react-native';
import * as Haptics from 'expo-haptics';
import { makeStyles } from '@/theme';

type Props = { value: number; durationSec: number; onChange: (sec: number) => void };

const THUMB = 28;

/**
 * Drag (or tap) along the song to pick the second that gets you. A plain responder view,
 * so no slider package is needed. A light tick of haptics every 10 seconds you pass.
 */
export function MomentScrubber({ value, durationSec, onChange }: Props) {
  const styles = useStyles();
  const [width, setWidth] = useState(0);
  const lastTick = useRef(Math.floor(value / 10));

  const pct = durationSec > 0 ? Math.min(1, Math.max(0, value / durationSec)) : 0;

  function seek(e: GestureResponderEvent) {
    if (width <= 0) return;
    const x = Math.min(width, Math.max(0, e.nativeEvent.locationX));
    const sec = Math.round((x / width) * durationSec);
    const tick = Math.floor(sec / 10);
    if (tick !== lastTick.current) {
      lastTick.current = tick;
      Haptics.selectionAsync();
    }
    onChange(sec);
  }

  return (
    <View
      style={styles.hit}
      onLayout={(e: LayoutChangeEvent) => setWidth(e.nativeEvent.layout.width)}
      onStartShouldSetResponder={() => true}
      onMoveShouldSetResponder={() => true}
      onResponderTerminationRequest={() => false}
      onResponderGrant={seek}
      onResponderMove={seek}
      accessible
      accessibilityRole="adjustable"
      accessibilityLabel="Moment in the song"
      accessibilityValue={{ min: 0, max: durationSec, now: value }}
      accessibilityActions={[{ name: 'increment' }, { name: 'decrement' }]}
      onAccessibilityAction={(e) =>
        onChange(Math.min(durationSec, Math.max(0, value + (e.nativeEvent.actionName === 'increment' ? 5 : -5))))}>
      <View pointerEvents="none" style={styles.track} />
      <View pointerEvents="none" style={[styles.fill, { width: pct * width }]} />
      <View pointerEvents="none" style={[styles.thumb, { left: pct * width - THUMB / 2 }]} />
    </View>
  );
}

const useStyles = makeStyles(({ colors }) => ({
  hit: { height: 44, justifyContent: 'center' },
  track: { position: 'absolute', left: 0, right: 0, height: 2, backgroundColor: colors.border },
  fill: { position: 'absolute', left: 0, height: 2, backgroundColor: colors.accent },
  thumb: {
    position: 'absolute',
    top: (44 - THUMB) / 2,
    width: THUMB,
    height: THUMB,
    borderRadius: THUMB / 2,
    backgroundColor: colors.accent,
  },
}));
