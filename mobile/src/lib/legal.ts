import * as WebBrowser from 'expo-web-browser';
import { Linking } from 'react-native';
import { apiUrl } from '@/api/client';

export type LegalPage = 'terms' | 'privacy' | 'guidelines';

export const SUPPORT_EMAIL = 'hello@caraoke.app';

/** Terms, Privacy Policy and Community Guidelines live as public web pages (the App Store links them too). */
export function openLegal(page: LegalPage) {
  return WebBrowser.openBrowserAsync(apiUrl(`/legal/${page}.html`));
}

export function emailSupport() {
  return Linking.openURL(`mailto:${SUPPORT_EMAIL}?subject=caraoke`);
}
