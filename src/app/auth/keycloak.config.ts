import Keycloak from 'keycloak-js';
import { environment } from '../../environments/environment';

let keycloak: Keycloak;

export function initKeycloak(): Promise<boolean> {
  console.log('[Keycloak Debug] Initializing with:', environment.keycloak);
  
  keycloak = new Keycloak({
    url: environment.keycloak.url,
    realm: environment.keycloak.realm,
    clientId: environment.keycloak.clientId
  });

  return keycloak.init({
    onLoad: 'login-required',
    silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
    checkLoginIframe: false,
    enableLogging: true,
    pkceMethod: 'S256' // Added for better compatibility with modern Keycloak
  }).then(authenticated => {
    console.log(`[Keycloak Debug] Init Success. Authenticated: ${authenticated}`);
    return authenticated;
  }).catch(err => {
    console.error('[Keycloak Debug] Init Error:', err);
    throw err;
  });
}

export function getKeycloak() {
  return keycloak;
}
