export const environment = {
    production: true,
    mockMode: false,
    apiBaseUrl: 'http://localhost:8089/api', // Example API Gateway URL
    keycloak: {
        url: 'http://localhost:8080',
        realm: 'jungle-realm',
        clientId: 'jungle-frontend'
    }
};
