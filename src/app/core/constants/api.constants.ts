/**
 * Configuration de l'API backend.
 * En dev : ng serve utilise le proxy (requêtes vers /api → localhost:8080).
 * En prod : adapter apiUrl selon votre déploiement.
 */
export const API_CONFIG = {
  /** Base URL des appels API. En dev avec proxy : '' ou '/api'. En prod : 'https://votre-backend.com/api' */
  apiUrl: '/api',
} as const;
