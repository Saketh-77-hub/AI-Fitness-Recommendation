export const authConfig = {
  clientId: import.meta.env.VITE_AUTH0_CLIENT_ID,
  authorizationEndpoint: `https://${import.meta.env.VITE_AUTH0_DOMAIN}/authorize`,
  tokenEndpoint: `https://${import.meta.env.VITE_AUTH0_DOMAIN}/oauth/token`,
  redirectUri: import.meta.env.VITE_REDIRECT_URI || 'http://localhost:5173/',
  scope: 'openid profile email offline_access',
  onRefreshTokenExpire: (event) => event.logIn(),
};
