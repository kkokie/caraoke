import { PaperName } from '@/api/client';

export type PaperColors = { bg: string; text: string; muted: string; border: string };

/**
 * The papers a story can be printed on. Same in light and dark mode: a story looks the way its
 * author chose. `plus` papers come with caraoke+ (shown, but locked, until subscriptions exist).
 */
export const papers: Record<PaperName, PaperColors & { label: string; plus: boolean }> = {
  cream: { label: 'Cream', plus: false, bg: '#EFE6D8', text: '#1C1714', muted: '#6B5A4A', border: '#DCCFBD' },
  dusk: { label: 'Dusk', plus: false, bg: '#3A2C3F', text: '#F3E9F2', muted: '#C9B7C6', border: '#4D3D52' },
  sage: { label: 'Sage', plus: false, bg: '#DCE3D2', text: '#1D2419', muted: '#56614E', border: '#C4CEB7' },
  ink: { label: 'Ink', plus: false, bg: '#1E2733', text: '#E9EEF4', muted: '#A8B4C2', border: '#334155' },
  rose: { label: 'Rose', plus: true, bg: '#E9C9C4', text: '#2A1715', muted: '#6E4A45', border: '#D9B1AB' },
  tape: { label: 'Tape', plus: true, bg: '#C8B27A', text: '#1F1A0E', muted: '#5C4F2E', border: '#B39B60' },
};

export const paperNames = Object.keys(papers) as PaperName[];

export function paperOf(name: PaperName | null | undefined): PaperColors {
  return papers[name ?? 'cream'] ?? papers.cream;
}
