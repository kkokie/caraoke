// Two palettes with the same keys. The app follows the phone's light/dark setting.
// `accent` doubles as text color, so each mode picks an amber that reads on its background.

export type Palette = {
  bg: string;
  surface: string;
  border: string;
  text: string;
  textMuted: string;
  accent: string;       // buttons, links, active tab, moments
  accentText: string;   // text on an accent fill
  paper: string;        // story "paper" cards
  paperText: string;
  paperMuted: string;
  dot: string;          // inactive moment dots
  success: string;
  danger: string;
};

/** Dim listening room: warm near-black, amber like an old stereo dial. */
export const dark: Palette = {
  bg: '#14110F',
  surface: '#1F1A17',
  border: '#3A322C',
  text: '#F4EDE4',
  textMuted: '#A89A8C',
  accent: '#F2A65A',
  accentText: '#14110F',
  paper: '#EFE6D8',
  paperText: '#1C1714',
  paperMuted: '#6B5A4A',
  dot: '#6E6157',
  success: '#7FC99A',
  danger: '#E5736B',
};

/** Liner-notes paper: warm cream, deep amber ink. */
export const light: Palette = {
  bg: '#F7F1E8',
  surface: '#EFE6D8',
  border: '#DCCFBD',
  text: '#1C1714',
  textMuted: '#6B5A4A',
  accent: '#9A5216',
  accentText: '#FFF8F0',
  paper: '#FFFDF8',
  paperText: '#1C1714',
  paperMuted: '#6B5A4A',
  dot: '#BFAE99',
  success: '#2F7A4B',
  danger: '#B3261E',
};
