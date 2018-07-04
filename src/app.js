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

  app.use(function (req, res, next) {
  	res.header('Access-Control-Allow-Origin', '*')
  	res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,PATCH,DELETE,OPTIONS')
  	res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization,' +
                                              'cid, user-id, x-auth, Cache-Control, X-Requested-With, datatype, *')

  	if (req.method === 'OPTIONS') {
    	res.sendStatus(200)
  	} else {
    	next()
  	};
  })

  app.use(bodyParser.json({limit: '1mb'}));
  app.use(logger('dev'));
  app.use(express.json());
  app.use(bodyParser.urlencoded({ extended: false }));
  app.use(cookieParser());
  app.use('/', indexRouter);
  module.exports = app;
  return app.listen(process.env.telemetry_service_port || 9001);
}, {count: (process.env.telemetry_service_threads || os.cpus().length)});