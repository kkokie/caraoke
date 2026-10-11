import { TextStyle } from 'react-native';
import { fonts } from './fonts';
import { dark, light, Palette } from './palettes';
import { typeFor } from './type';
import type { Theme } from './index';

// Page decoration (Cyworld minihompy spirit): curated color worlds, each drawn for light and dark.
// Only the surfaces and the accent change; text keeps the app's readable inks.

export type PageThemeName = 'PAPER' | 'MIDNIGHT' | 'MEADOW' | 'SUNSET' | 'OCEAN' | 'ROSE';
export type PageTypeName = 'CLASSIC' | 'TYPEWRITER' | 'MODERN';

type Tint = Pick<Palette, 'bg' | 'surface' | 'border' | 'accent' | 'accentText' | 'dot'>;

const tints: Record<Exclude<PageThemeName, 'PAPER'>, { light: Tint; dark: Tint }> = {
  MIDNIGHT: {
    light: { bg: '#E9ECF5', surface: '#DDE2EF', border: '#C3CADB', accent: '#3B4A8C', accentText: '#F5F7FF', dot: '#A9B2CC' },
    dark: { bg: '#0E1020', surface: '#181B30', border: '#2B2F4A', accent: '#9AA8FF', accentText: '#0E1020', dot: '#4A4F70' },
  },
  MEADOW: {
    light: { bg: '#EEF3E6', surface: '#E2EAD6', border: '#C9D5B8', accent: '#3F6B2E', accentText: '#F6FBF0', dot: '#A9BA92' },
    dark: { bg: '#10160E', surface: '#1A2317', border: '#2F3D29', accent: '#9CCB7E', accentText: '#10160E', dot: '#4C5E43' },
  },
  SUNSET: {
    light: { bg: '#FBEFE6', surface: '#F5E1D2', border: '#E8C9B3', accent: '#B4471F', accentText: '#FFF6F0', dot: '#D9AE92' },
    dark: { bg: '#1A100C', surface: '#261813', border: '#432A20', accent: '#FF9A6B', accentText: '#1A100C', dot: '#6E4A3B' },
  },
  OCEAN: {
    light: { bg: '#E8F2F3', surface: '#D9E9EB', border: '#BDD5D8', accent: '#1F6670', accentText: '#F0FBFC', dot: '#9CBFC3' },
    dark: { bg: '#0B1517', surface: '#132125', border: '#24393E', accent: '#7FD0D8', accentText: '#0B1517', dot: '#3F5C61' },
  },
  ROSE: {
    light: { bg: '#F8ECEE', surface: '#F0DDE1', border: '#E2C2C9', accent: '#9C3552', accentText: '#FFF5F7', dot: '#D3A6B1' },
    dark: { bg: '#1A0F12', surface: '#26171B', border: '#42272F', accent: '#F29BB2', accentText: '#1A0F12', dot: '#6A4550' },
  },
};

/** For the picker: each theme's swatch in the current mode. */
export const PAGE_THEMES: { name: PageThemeName; label: string }[] = [
  { name: 'PAPER', label: 'Paper' },
  { name: 'MIDNIGHT', label: 'Midnight' },
  { name: 'MEADOW', label: 'Meadow' },
  { name: 'SUNSET', label: 'Sunset' },
  { name: 'OCEAN', label: 'Ocean' },
  { name: 'ROSE', label: 'Rose' },
];

export const PAGE_TYPES: { name: PageTypeName; label: string }[] = [
  { name: 'CLASSIC', label: 'Classic' },
  { name: 'TYPEWRITER', label: 'Typewriter' },
  { name: 'MODERN', label: 'Modern' },
];

export function pagePalette(name: PageThemeName, scheme: 'light' | 'dark'): Palette {
  const base = scheme === 'light' ? light : dark;
  return name === 'PAPER' ? base : { ...base, ...tints[name][scheme] };
}

const cache = new Map<string, Theme>();

/** One Theme object per (theme, type, mode), so makeStyles' per-theme cache keeps working. */
export function pageTheme(name: PageThemeName, typeName: PageTypeName, scheme: 'light' | 'dark'): Theme {
  const key = `${name}:${typeName}:${scheme}`;
  const cached = cache.get(key);
  if (cached) return cached;
  const colors = pagePalette(name, scheme);
  const type = typeFor(colors);
  const theme: Theme = { colors, scheme, type: { ...type, title: titleFor(typeName, type.title) } };
  cache.set(key, theme);
  return theme;
}

/** Your name and block titles. Custom fonts have no bold; only the system font gets a weight. */
function titleFor(typeName: PageTypeName, classic: TextStyle): TextStyle {
  if (typeName === 'TYPEWRITER') return { ...classic, fontFamily: fonts.mono, fontSize: 30, lineHeight: 36 };
  if (typeName === 'MODERN') return { ...classic, fontFamily: undefined, fontSize: 32, lineHeight: 38, fontWeight: '700' as const };
  return classic;
}
