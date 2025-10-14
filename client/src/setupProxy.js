const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
    const target = 'http://backend:8080';
    app.use(
        ['/api', '/oauth2', '/login/oauth2'],
        createProxyMiddleware({
            target,
            changeOrigin: true,
            ws: false,
            secure: false,
            logLevel: 'warn',
        })
    );
};
