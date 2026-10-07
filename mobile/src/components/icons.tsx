import Svg, { Circle, Path, Rect } from 'react-native-svg';

type IconProps = { color: string; size?: number };

/** A record / compass: digging for stories. */
export function DiscoverIcon({ color, size = 24 }: IconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth={1.8} strokeLinecap="round">
      <Circle cx={12} cy={12} r={9} />
      <Circle cx={12} cy={12} r={2.5} />
    </Svg>
  );
}

/** "You": a person with slim headphones (option A in the mockup). */
export function YouIcon({ color, size = 24 }: IconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth={1.8} strokeLinecap="round">
      <Circle cx={12} cy={8.2} r={3.1} />
      <Path d="M7.9 9a4.1 4.1 0 0 1 8.2 0" />
      <Rect x={7} y={8.4} width={1.7} height={2.8} rx={0.85} fill={color} stroke="none" />
      <Rect x={15.3} y={8.4} width={1.7} height={2.8} rx={0.85} fill={color} stroke="none" />
      <Path d="M5.2 20.5c1.2-3.2 3.7-4.9 6.8-4.9s5.6 1.7 6.8 4.9" />
    </Svg>
  );
}

export function PlusIcon({ color, size = 26 }: IconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth={2.2} strokeLinecap="round">
      <Path d="M12 5v14M5 12h14" />
    </Svg>
  );
}
