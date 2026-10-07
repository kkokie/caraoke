import { useColorScheme, StyleSheet } from 'react-native';
import { dark, light, Palette } from './palettes';
import { TypeScale, typeFor } from './type';

export { fonts, fontAssets } from './fonts';
export { papers, paperNames, paperOf } from './papers';
export type { PaperColors } from './papers';
export type { Palette } from './palettes';

export const space = { xs: 4, sm: 8, md: 16, lg: 24, xl: 40 } as const;
export const radius = { md: 12, lg: 20, pill: 999 } as const;

export type Theme = { colors: Palette; type: TypeScale; scheme: 'light' | 'dark' };

const themes: Record<'light' | 'dark', Theme> = {
  dark: { colors: dark, type: typeFor(dark), scheme: 'dark' },
  light: { colors: light, type: typeFor(light), scheme: 'light' },
};

/** The current theme; follows the phone's light/dark setting (dark if unknown). */
export function useTheme(): Theme {
  return themes[useColorScheme() === 'light' ? 'light' : 'dark'];
}

/**
 * Themed StyleSheets: `const useStyles = makeStyles(({ colors, type }) => ({ ... }))`,
 * then `const styles = useStyles()` inside the component. Built once per theme.
 */
export function makeStyles<T extends StyleSheet.NamedStyles<T>>(factory: (theme: Theme) => T) {
  const cache = new Map<Theme, T>();
  return function useStyles(): T {
    const theme = useTheme();
    let styles = cache.get(theme);
    if (!styles) {
      styles = StyleSheet.create(factory(theme));
      cache.set(theme, styles);
    }
    return styles;
  };
}
