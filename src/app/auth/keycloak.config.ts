import Keycloak from 'keycloak-js';

let keycloak: Keycloak;

export function initKeycloak(): Promise<boolean> {
  keycloak = new Keycloak({
    url: 'http://localhost:8085',
    realm: 'jungleinenglish-realm',
    clientId: 'jungleinenglish-frontend'
  });

  return keycloak.init({
    onLoad: 'check-sso',
    silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
    checkLoginIframe: false,
    enableLogging: true
  });
}

export function getKeycloak() {
  return keycloak;
}
