export const authConfig = {
  clientId: 'tOyPWkSMRf5yAQIedD2VQyr8OWoPwRdi',
  authorizationEndpoint: 'https://dev-ooryx7evb8kbyq1z.us.auth0.com/authorize',
  tokenEndpoint: 'https://dev-ooryx7evb8kbyq1z.us.auth0.com/oauth/token',
  redirectUri: 'http://localhost:5173/',
  scope: 'openid profile email offline_access',
  onRefreshTokenExpire: (event) => event.logIn(),
};
