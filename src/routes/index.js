var express = require('express');
var router = express.Router();
var telemetryService = require('../service/telemetry-service');

router.post('/v1/telemetry', function(req, res, next) {
  req.body.did = req.header('x-device-id');
  req.body.channel = req.header('x-channel-id');
  req.body.pid = req.header('x-app-id');
  telemetryService.dispatch(req, res);
})

router.get('/health', function(req, res, next) {
  telemetryService.health(res);
})

module.exports = router;