const express = require('express'),
  cluster = require('express-cluster'),
  cookieParser = require('cookie-parser'),
  logger = require('morgan'),
  bodyParser = require('body-parser'),
  envVariables = require('./envVariables'),
  port = envVariables.port,
  threads = envVariables.threads;

const createAppServer = () => {
  const app = express();
  app.use((req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*')
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,PATCH,DELETE,OPTIONS')
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization,' + 'cid, user-id, x-auth, Cache-Control, X-Requested-With, datatype, *')
    if (req.method === 'OPTIONS') res.sendStatus(200)
    else next()
  })
  app.use(bodyParser.json({ limit: '5mb' }));
  app.use(logger('dev'));
  app.use(express.json());
  app.use(bodyParser.urlencoded({ extended: false }));
  app.use(cookieParser());
  app.use('/', require('./routes'));
  module.exports = app;
  return app;
}

if (process.env.node_env !== 'test') {
  cluster((worker) => {
    const app = createAppServer();
    const server = app.listen(port, () => console.log(`telemetry services cluster is running on port ${port} with ${process.pid} pid`));
    server.keepAliveTimeout = 60000 * 5;
    return server;
  }, { count: threads });
} else {
  const app = createAppServer();
  const server = app.listen(port, () => console.log(`telemetry services is running in test env on port ${port} with ${process.pid} pid`));
  server.keepAliveTimeout = 60000 * 5;
  return server;
}
