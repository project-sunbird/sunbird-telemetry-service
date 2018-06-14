var uuidv1 = require('uuid/v1');
var Dispatcher = require('../dispatcher/dispatcher').Dispatcher;

const localStorageEnabled = process.env.telemetry_local_storage_enabled;
const proxyTelemetryData = process.env.telemetry_proxy_pass_through;

// TODO: Make this efficient. Implementation to be similar to typesafe config
const config = {
    dispatcher: process.env.telemetry_local_storage_type,
    proxyURL: process.env.telemetry_proxy_pass_through_url,
    encodingType: process.env.telemetry_encoding_type,
    kafkaHost: process.env.telemetry_kafka_broker_list,
    topic: process.env.telemetry_kafka_topic,
    fileName: 'telemetry-%DATE%.log',
    level: 'info'
}

var dispatcher = undefined;

if(localStorageEnabled === 'true') {
    dispatcher = new Dispatcher(config);
}

exports.dispatch = function(headers, message, res) {
    if(localStorageEnabled === 'true') {
        dispatcher.dispatch(headers, message, function(err, data) {
            if(err) {
                console.log('error', err);
                res.status(500)
                res.json({responseCode: "SERVER_ERROR"});
            } else {
                res.json({responseCode: "OK"});
            }
        });
    } else {
        res.status(500)
        res.json({responseCode: "NO_DISPATCHER_FOUND"});
    }
}