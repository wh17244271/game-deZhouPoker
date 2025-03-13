const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'http://localhost:8080',
      changeOrigin: true,
      secure: false,
      logLevel: 'debug',
    })
  );
  
  app.use(
    '/ws',
    createProxyMiddleware({
      target: 'ws://localhost:8080',
      ws: true,
      changeOrigin: true,
      secure: false,
      logLevel: 'debug',
    })
  );
}; 