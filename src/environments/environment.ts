export const environment = {
    production: false,
    mockMode: true,
    apiBaseUrl: '/api', // Uses proxy.conf.json in dev mode
    keycloak: {
        url: '',
        realm: '',
        clientId: ''
    }
};
