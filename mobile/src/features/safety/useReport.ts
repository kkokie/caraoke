import { useState } from 'react';
import { Alert } from 'react-native';
import { router } from 'expo-router';
import { api, ReportBody, ReportReason } from '@/api/client';
import { confirmBlock } from './blockActions';

export type ReportTarget = { kind: 'STORY'; storyId: number; handle: string } | { kind: 'USER'; handle: string };

const MAX_DETAILS = 500;

/** The report form: pick a reason, optionally say more, send. Afterwards, offer to block them too. */
export function useReport(target: ReportTarget) {
  const [reason, setReason] = useState<ReportReason | null>(null);
  const [details, setDetails] = useState('');
  const [sending, setSending] = useState(false);

  async function send() {
    if (!reason || sending) return;
    setSending(true);
    const body: ReportBody = target.kind === 'STORY'
      ? { kind: 'STORY', storyId: target.storyId, reason, details: details.trim() || undefined }
      : { kind: 'USER', handle: target.handle, reason, details: details.trim() || undefined };
    try {
      await api.report(body);
      thankAndOfferBlock(target.handle);
    } catch (e) {
      Alert.alert('Couldn’t send your report', e instanceof Error ? e.message : String(e));
      setSending(false);
    }
  }

  return {
    reason, setReason,
    details, setDetails: (v: string) => setDetails(v.slice(0, MAX_DETAILS)),
    sending, send, canSend: !!reason && !sending,
  };
}

function thankAndOfferBlock(handle: string) {
  Alert.alert('Thanks for telling us', 'We’ll take a look. You can also block them so you never see each other.', [
    { text: 'Done', onPress: () => router.back() },
    { text: `Block @${handle}`, style: 'destructive', onPress: () => confirmBlock(handle, () => router.back()) },
  ]);
}
