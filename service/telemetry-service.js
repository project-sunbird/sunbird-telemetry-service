var uuidv1 = require('uuid/v1');
var Dispatcher = require('../dispatcher/dispatcher').Dispatcher;

const localStorageEnabled = process.env.telemetry_local_storage_enabled || 'true';
const proxyTelemetryData = process.env.telemetry_proxy_pass_through;

// TODO: Make this efficient. Implementation to be similar to typesafe config
const config = {
    dispatcher: process.env.telemetry_local_storage_type,
    proxyURL: process.env.telemetry_proxy_pass_through_url,
    encodingType: process.env.telemetry_encoding_type,
    kafkaHost: process.env.telemetry_kafka_broker_list,
    topic: process.env.telemetry_kafka_topic,
    fileName: 'telemetry-%DATE%.log',
    level: 'info',
    partitionBy: process.env.telemetry_cassandra_partition_by || 'hour',
    keyspace: process.env.telemetry_cassandra_keyspace,
    contactPoints: (process.env.telemetry_cassandra_contactpoints || 'localhost').split(',')
}

var dispatcher = undefined;

if(localStorageEnabled === 'true') {
    dispatcher = new Dispatcher(config);
}

exports.dispatch = function(message, res) {
    if(!message.mid) message.mid = uuidv1();
    message.syncts = new Date().getTime();
    const data = JSON.stringify(message);
    if(localStorageEnabled === 'true') {
        dispatcher.dispatch(message.mid, data, function(err, data) {
            if(err) {
                console.log('error', err);
                res.status(500)
                res.json({id: 'api.telemetry', ver: '1.0', ets: new Date().getTime(), params: {err: err}, responseCode: "SERVER_ERROR"});
            } else {
                res.json({id: 'api.telemetry', ver: '1.0', ets: new Date().getTime(), params: {}, responseCode: "SUCCESS"});
            }
        });
    } else {
        res.status(500)
        res.json({id: 'api.telemetry', ver: '1.0', ets: new Date().getTime(), params: {err: 'Configuration error.'}, responseCode: "SERVER_ERROR"});
    }
}