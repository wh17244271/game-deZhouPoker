const path = require('path');

module.exports = {
  // 其他配置...
  devServer: {
    allowedHosts: ['localhost', '.localhost'],
    host: 'localhost',
    port: 3000,
    open: true,
    hot: true,
    historyApiFallback: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      }
    }
  }
}; 