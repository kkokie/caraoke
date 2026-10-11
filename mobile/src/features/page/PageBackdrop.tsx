import { StyleSheet } from 'react-native';
import Svg, { Circle, Defs, Line, Path, Pattern, Rect } from 'react-native-svg';
import { PageBackgroundName } from '@/api/client';
import { useTheme } from '@/theme';

/** A quiet pattern behind a decorated page, drawn in the page theme's border color. */
export function PageBackdrop({ background }: { background: PageBackgroundName }) {
  const { colors } = useTheme();
  if (background === 'PLAIN') return null;
  const ink = colors.border;

  return (
    <Svg style={StyleSheet.absoluteFill} pointerEvents="none">
      <Defs>
        <Pattern id="page-bg" patternUnits="userSpaceOnUse" width={SIZE[background]} height={SIZE[background]}>
          {tile(background, ink)}
        </Pattern>
      </Defs>
      <Rect width="100%" height="100%" fill="url(#page-bg)" />
    </Svg>
  );
}

const SIZE: Record<Exclude<PageBackgroundName, 'PLAIN'>, number> = { DOTS: 18, LINES: 28, GRID: 24, STARS: 64 };

function tile(background: Exclude<PageBackgroundName, 'PLAIN'>, ink: string) {
  switch (background) {
    case 'DOTS':
      return <Circle cx={9} cy={9} r={1.4} fill={ink} />;
    case 'LINES':   // ruled notebook paper
      return <Line x1={0} y1={27} x2={28} y2={27} stroke={ink} strokeWidth={1} />;
    case 'GRID':
      return <Path d="M24 0V24M0 24H24" stroke={ink} strokeWidth={0.8} fill="none" />;
    case 'STARS':   // a few tiny four-point sparkles
      return (
        <>
          <Path d="M12 6l1.2 3.8L17 11l-3.8 1.2L12 16l-1.2-3.8L7 11l3.8-1.2z" fill={ink} />
          <Path d="M46 34l.8 2.4 2.4.8-2.4.8-.8 2.4-.8-2.4-2.4-.8 2.4-.8z" fill={ink} />
          <Circle cx={30} cy={52} r={1.2} fill={ink} />
          <Circle cx={56} cy={10} r={0.9} fill={ink} />
        </>
      );
  }
}
