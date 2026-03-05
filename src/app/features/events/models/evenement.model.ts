import { environment } from '../../../../environments/environment';

export interface Evenement {
  eventId?: number;
  title: string;
  description?: string;
  /** Image as URL or base64 data URL. Backend may send as imageUrl or image_url. */
  imageUrl?: string;
  /** Backend snake_case variant; use getEventImageUrl(event) to get one value. */
  image_url?: string;
  startTime: string;
  endTime: string;
  location?: string;
  capacity: number;
  organizerId: number;
  reminderSent?: boolean;
}

/** Use in templates to get image URL whether backend sends imageUrl or image_url. */
export function getEventImageUrl(event: Evenement): string | null {
  const url = event?.imageUrl ?? event?.image_url ?? '';
  const trimmed = typeof url === 'string' ? url.trim() : '';
  if (!trimmed || trimmed.length === 0) return null;

  // If it's already a full URL (http://, https://, or data:), return as-is
  if (trimmed.startsWith('http://') || trimmed.startsWith('https://') || trimmed.startsWith('data:')) {
    return trimmed;
  }

  // If it's a relative path (starts with /), prepend backend URL
  if (trimmed.startsWith('/')) {
    return `${environment.eventApiUrl}${trimmed}`;
  }

  // Otherwise return as-is (might be a relative path without leading slash)
  return trimmed;
}
