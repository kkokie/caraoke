import { TextStyle } from 'react-native';
import { fonts } from './fonts';
import { Palette } from './palettes';

/** Text styles for one palette. Every style carries a color, since RN text doesn't inherit one. */
export function typeFor(c: Palette) {
  const scale = {
    title: { fontFamily: fonts.serif, fontSize: 38, lineHeight: 42, color: c.text },
    subtitle: { fontSize: 16, lineHeight: 22, color: c.textMuted },
    label: { fontFamily: fonts.mono, fontSize: 12, letterSpacing: 1.5, color: c.textMuted },
    body: { fontSize: 16, color: c.text },
    hint: { fontSize: 13, lineHeight: 18, color: c.textMuted },
    story: { fontFamily: fonts.serif, fontSize: 20, lineHeight: 27, color: c.text },
    lyric: { fontFamily: fonts.serifItalic, fontSize: 22, lineHeight: 27, color: c.text },
    mono: { fontFamily: fonts.mono, fontSize: 12, color: c.textMuted },
  } satisfies Record<string, TextStyle>;
  // Widened to TextStyle so a decorated page can swap a style's font (see pageThemes)
  return scale as { [K in keyof typeof scale]: TextStyle };
}

export type TypeScale = ReturnType<typeof typeFor>;
