var uuidv1 = require('uuid/v1');
var request = require('request');
var Dispatcher = require('../dispatcher/dispatcher').Dispatcher;

const localStorageEnabled = process.env.telemetry_local_storage_enabled || 'true';
const telemetryProxyEnabled = process.env.telemetry_proxy_enabled;
const proxyURL = process.env.telemetry_proxy_url;

// TODO: Make this efficient. Implementation to be similar to typesafe config. Right now one configuration holds 
// together all supported transport configurations
//
// const dispatcher = process.env.telemetry_local_storage_type;
// const dispatcherOptions = ConfigFactory.getConfig(dispatcher);
const config = {
    level: 'info',
    dispatcher: process.env.telemetry_local_storage_type,
    proxyURL: process.env.telemetry_proxy_pass_through_url,
    encodingType: process.env.telemetry_encoding_type,
    kafkaHost: process.env.telemetry_kafka_broker_list,
    topic: process.env.telemetry_kafka_topic,
    filename: process.env.telemetry_file_filename || 'telemetry-%DATE%.log',
    maxSize: process.env.telemetry_file_maxsize || '100m',
    maxFiles: process.env.telemetry_file_maxfiles || '100',
    partitionBy: process.env.telemetry_cassandra_partition_by || 'hour',
    keyspace: process.env.telemetry_cassandra_keyspace,
    contactPoints: (process.env.telemetry_cassandra_contactpoints || 'localhost').split(','),
    cassandraTtl: process.env.telemetry_cassandra_ttl
}

var dispatcher = undefined;

if(localStorageEnabled === 'true') {
    dispatcher = new Dispatcher(config);
}

const SUCCESS_RESP = {id: 'api.telemetry', ver: '1.0', ets: new Date().getTime(), params: {}, responseCode: "SUCCESS"};
const ERROR_RESP = {id: 'api.telemetry', ver: '1.0', ets: new Date().getTime(), params: {err: err}, responseCode: "SERVER_ERROR"};

exports.dispatch = function(req, res) {
    
    var message = req.body;
    if(!message.mid) message.mid = uuidv1();
    message.syncts = new Date().getTime();
    const data = JSON.stringify(message);
    const headers = {'content-type': req.header('content-type'), 'content-encoding': req.header('content-encoding')};
    
    if(localStorageEnabled === 'true' || telemetryProxyEnabled === 'true') {

        if(localStorageEnabled === 'true') {
            // Store locally and respond back with proper status code
            dispatcher.dispatch(message.mid, data, function(err, data) {
                if(err) {
                    console.log('error', err);
                    res.status(500)
                    res.json(ERROR_RESP);
                } else {
                    res.json(SUCCESS_RESP);
                }
            });
        }
        if(localStorageEnabled === 'true' && telemetryProxyEnabled === 'true') {
            // Store locally and proxy to the specified URL. If the proxy fails ignore the error as the local storage is successful. Do a sync later
            var options = {url:proxyURL, headers: headers, body: data};
            request.post(options, function optionalCallback(err, httpResponse, body) {
                if (err) {
                    return console.error('Proxy failed:', err);
                }
                console.log('Proxy successful!  Server responded with:', body);
            });
        } 
        if(localStorageEnabled !== 'true' && telemetryProxyEnabled === 'true') {
            
            // Just proxy
            var options = {url:proxyURL, headers: headers, body: data};
            request.post(options, function optionalCallback(err, httpResponse, body) {
                if (err) {
                    res.status(500)
                    res.json(ERROR_RESP);
                } else {
                    console.log('Proxy successful!  Server responded with:', body);
                    res.json(SUCCESS_RESP);
                }
            });
        }
    } else {
        res.status(500)
        res.json({id: 'api.telemetry', ver: '1.0', ets: new Date().getTime(), params: {err: 'Configuration error.'}, responseCode: "SERVER_ERROR"});
    }
}