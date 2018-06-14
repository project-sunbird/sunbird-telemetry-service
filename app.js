var createError = require('http-errors');
var express = require('express');
var cluster = require('express-cluster');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
var bodyParser = require('body-parser');
var os = require('os');

var indexRouter = require('./routes/index');

cluster(function(worker) {
  var app = express();
  app.use(bodyParser.json({limit: '50mb'}));
  app.use(logger('dev'));
  app.use(express.json());
  app.use(bodyParser.urlencoded({ extended: false }));
  app.use(cookieParser());
  app.use('/', indexRouter);
  module.exports = app;
  return app.listen(9000);
}, {count: (process.env.telemetry_service_threads || os.cpus().length)});