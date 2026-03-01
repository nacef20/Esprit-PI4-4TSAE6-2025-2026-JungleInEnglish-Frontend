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
    checkLoginIframe: false
  });
}

export function getKeycloak() {
  return keycloak;
}
