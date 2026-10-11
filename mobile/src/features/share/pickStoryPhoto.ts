import * as ImagePicker from 'expo-image-picker';
import { ImageManipulator, SaveFormat } from 'expo-image-manipulator';
import { LocalImage } from '@/api/client';

const MAX_PX = 1600;   // plenty for a phone screen, small enough to upload quickly

/**
 * Pick a photo for a story (any shape, no forced crop), shrink it to ≤1600px wide as a JPEG
 * (iPhone HEIC becomes something every client can show), and keep its size for layout.
 */
export async function pickStoryPhoto(): Promise<LocalImage | null> {
  const picked = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ['images'], quality: 1 });
  if (picked.canceled || picked.assets.length === 0) return null;
  const asset = picked.assets[0];

  const steps = ImageManipulator.manipulate(asset.uri);
  if (asset.width > MAX_PX) steps.resize({ width: MAX_PX });
  const rendered = await steps.renderAsync();
  const saved = await rendered.saveAsync({ compress: 0.82, format: SaveFormat.JPEG });

  return { uri: saved.uri, name: 'story.jpg', type: 'image/jpeg', width: saved.width, height: saved.height };
}
