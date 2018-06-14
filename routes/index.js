var express = require('express');
var router = express.Router();
var telemetryService = require('../service/telemetry-service');

router.post('/v1/telemetry', function(req, res, next) {
  const headers = {
    did: req.header('x-device-id'),
    channel: req.header('x-channel-id'),
    appId: req.header('x-app-id'),
    encodingType: req.header('content-encoding'),
    contentType: req.header('content-type')
  }
  telemetryService.dispatch(headers, req.body, res);
})

module.exports = router;