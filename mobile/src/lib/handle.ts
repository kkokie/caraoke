// Mirrors backend HandleRules so users get instant feedback before any network call.
// The server is still the source of truth (it re-validates and checks uniqueness).

const FORMAT = /^[a-z0-9_]{3,20}$/;

const RESERVED = new Set([
  'admin', 'administrator', 'support', 'help', 'moderator', 'mod',
  'staff', 'official', 'caraoke', 'root', 'system', 'api', 'me',
]);

export function normalizeHandle(raw: string): string {
  return raw.trim().toLowerCase().replace(/^@/, '');
}

/** @returns null if valid, otherwise a user-facing reason */
export function validateHandle(normalized: string): string | null {
  if (!FORMAT.test(normalized)) {
    return 'Handles are 3-20 characters: letters, numbers, and underscores.';
  }
  if (RESERVED.has(normalized)) {
    return 'That handle is reserved.';
  }
  return null;
}
