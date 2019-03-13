const express = require('express'),
  router = express.Router(),
  telemetryService = require('../service/telemetry-service');

router.post('/v1/telemetry', (req, res) => telemetryService.dispatch(req, res));

router.get('/health', (req, res) => telemetryService.health(req, res));

router.get('/service/health', (req, res) => telemetryService.telemetryServiceHealth(req, res));

module.exports = router;
