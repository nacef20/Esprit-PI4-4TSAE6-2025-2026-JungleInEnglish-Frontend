import { environment } from '@env/environment';

export const API_CONFIG = {
  apiUrl: `${environment.apiBaseUrl}/api`,
} as const;
