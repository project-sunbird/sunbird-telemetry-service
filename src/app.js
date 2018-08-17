const express = require('express'),
cluster = require('express-cluster'),
cookieParser = require('cookie-parser'),
logger = require('morgan'),
bodyParser = require('body-parser'),
os = require('os'),
indexRouter = require('./routes'),
port = process.env.telemetry_service_port || 9001,
threads = process.env.telemetry_service_threads || os.cpus().length;

cluster((worker) => {
  const app = express();
  app.use((req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*')
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,PATCH,DELETE,OPTIONS')
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization,' +
      'cid, user-id, x-auth, Cache-Control, X-Requested-With, datatype, *')
    if (req.method === 'OPTIONS') res.sendStatus(200)
    else next()
  })
  app.use(bodyParser.json({
    limit: '1mb'
  }));
  app.use(logger('dev'));
  app.use(express.json());
  app.use(bodyParser.urlencoded({ 
    extended: false
  }));
  app.use(cookieParser());
  app.use('/', indexRouter);
  module.exports = app;
  return app.listen( port, () => console.log(`telemetry services is running on port ${port} with ${threads} threads`));
}, { count: threads });