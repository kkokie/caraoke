import * as ImagePicker from 'expo-image-picker';
import { ImageManipulator, SaveFormat } from 'expo-image-manipulator';
import { LocalImage } from '@/api/client';

const AVATAR_PX = 512;

/**
 * Lets the user pick and square-crop a photo, then shrinks it to a 512px JPEG so uploads stay small
 * (and HEIC from iPhones becomes something every client can show). Returns null if they cancel.
 */
export async function pickAvatarImage(): Promise<LocalImage | null> {
  const picked = await ImagePicker.launchImageLibraryAsync({
    mediaTypes: ['images'],
    allowsEditing: true,
    aspect: [1, 1],
    quality: 1,
  });
  if (picked.canceled || picked.assets.length === 0) return null;

  const rendered = await ImageManipulator.manipulate(picked.assets[0].uri)
    .resize({ width: AVATAR_PX })
    .renderAsync();
  const saved = await rendered.saveAsync({ compress: 0.85, format: SaveFormat.JPEG });

  return { uri: saved.uri, name: 'avatar.jpg', type: 'image/jpeg' };
}
