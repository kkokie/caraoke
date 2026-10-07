// Per-weight imports: the packages' main entry would bundle every weight (~16 files).
import { InstrumentSerif_400Regular } from '@expo-google-fonts/instrument-serif/400Regular';
import { InstrumentSerif_400Regular_Italic } from '@expo-google-fonts/instrument-serif/400Regular_Italic';
import { IBMPlexMono_400Regular } from '@expo-google-fonts/ibm-plex-mono/400Regular';
import { IBMPlexMono_500Medium } from '@expo-google-fonts/ibm-plex-mono/500Medium';

/**
 * Serif for stories and titles (the words people wrote), mono for times, years and labels
 * (the liner-notes metadata). Everything else stays the system font so the UI feels native.
 */
export const fonts = {
  serif: 'InstrumentSerif_400Regular',
  serifItalic: 'InstrumentSerif_400Regular_Italic',
  mono: 'IBMPlexMono_400Regular',
  monoMedium: 'IBMPlexMono_500Medium',
} as const;

/** Passed to expo-font's useFonts in the root layout. */
export const fontAssets = {
  [fonts.serif]: InstrumentSerif_400Regular,
  [fonts.serifItalic]: InstrumentSerif_400Regular_Italic,
  [fonts.mono]: IBMPlexMono_400Regular,
  [fonts.monoMedium]: IBMPlexMono_500Medium,
};
