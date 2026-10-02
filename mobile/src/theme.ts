// Design tokens. One place to change the look of the whole app.

export const colors = {
  bg: '#14110F',          // warm near-black, like a dim listening room
  surface: '#1F1A17',
  border: '#3A322C',
  text: '#F4EDE4',
  textMuted: '#A89A8C',
  accent: '#F2A65A',      // amber, the glow of an old stereo dial
  accentText: '#14110F',
  success: '#7FC99A',
  danger: '#E5736B',
} as const;

export const space = { xs: 4, sm: 8, md: 16, lg: 24, xl: 40 } as const;

export const radius = { md: 12, lg: 20 } as const;

export const type = {
  title: { fontSize: 32, fontWeight: '700' as const, color: colors.text },
  subtitle: { fontSize: 16, color: colors.textMuted, lineHeight: 22 },
  label: { fontSize: 13, fontWeight: '600' as const, color: colors.textMuted, letterSpacing: 0.5 },
  body: { fontSize: 16, color: colors.text },
  hint: { fontSize: 13, lineHeight: 18 },
};
