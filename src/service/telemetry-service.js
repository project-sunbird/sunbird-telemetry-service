const uuidv1 = require('uuid/v1'),
    request = require('request'),
    Dispatcher = require('../dispatcher/dispatcher').Dispatcher;
// TODO: Make this efficient. Implementation to be similar to typesafe config. Right now one configuration holds 
// together all supported transport configurations

const config = {
    level: 'info',
    dispatcher: process.env.telemetry_local_storage_type || 'kafka',
    proxyURL: process.env.telemetry_proxy_url,
    proxyAuthKey: process.env.telemetry_proxy_auth_key,
    encodingType: process.env.telemetry_encoding_type,
    kafkaHost: process.env.telemetry_kafka_broker_list,
    topic: process.env.telemetry_kafka_topic,
    filename: process.env.telemetry_file_filename || 'telemetry-%DATE%.log',
    maxSize: process.env.telemetry_file_maxsize || '100m',
    maxFiles: process.env.telemetry_file_maxfiles || '100',
    partitionBy: process.env.telemetry_cassandra_partition_by || 'hour',
    keyspace: process.env.telemetry_cassandra_keyspace,
    contactPoints: (process.env.telemetry_cassandra_contactpoints || 'localhost').split(','),
    cassandraTtl: process.env.telemetry_cassandra_ttl,
    localStorageEnabled: process.env.telemetry_local_storage_enabled || 'true',
    telemetryProxyEnabled: process.env.telemetry_proxy_enabled
}

class TelemetryService {
    constructor(config) {
        this.config = config;
        this.dispatcher;
        if (this.config.localStorageEnabled === 'true') {
            this.dispatcher = new Dispatcher(config);
        }
    }
    dispatch(req, res) {
        const message = req.body;
        message.did = req.header('x-device-id');
        message.channel = req.header('x-channel-id');
        message.pid = req.header('x-app-id');
        if (!message.mid) message.mid = uuidv1();
        message.syncts = new Date().getTime();
        const data = JSON.stringify(message);
        if (this.config.localStorageEnabled === 'true' || this.config.telemetryProxyEnabled === 'true') {
            if (this.config.localStorageEnabled === 'true' && this.config.telemetryProxyEnabled !== 'true') {
                // Store locally and respond back with proper status code
                this.dispatcher.dispatch(message.mid, data, this.getRequestCallBack(req, res));
            } else if (this.config.localStorageEnabled === 'true' && this.config.telemetryProxyEnabled === 'true') {
                // Store locally and proxy to the specified URL. If the proxy fails ignore the error as the local storage is successful. Do a sync later
                const options = this.getProxyRequestObj(req, data);
                request.post(options, (err, res, body) => {
                    if (err) console.error('Proxy failed:', err);
                    else console.log('Proxy successful!  Server responded with:', body);
                });
                this.dispatcher.dispatch(message.mid, data, this.getRequestCallBack(req, res));
            } else if (this.config.localStorageEnabled !== 'true' && this.config.telemetryProxyEnabled === 'true') {
                // Just proxy
                const options = this.getProxyRequestObj(req, data);
                request.post(options, this.getRequestCallBack(req, res));
            }
        } else {
            this.sendError(res, {
                id: 'api.telemetry',
                params: {
                    err: 'Configuration error.'
                },
            });
        }
    }
    health(req, res) {
        if (this.config.localStorageEnabled === 'true') {
            this.dispatcher.health((healthy) => {
                if (healthy) {
                    this.sendSuccess(res, {
                        id: 'api.health',
                    });
                } else {
                    this.sendError(res, {
                        id: 'api.health',
                        params: {
                            err: 'Telemetry API is unhealthy'
                        }
                    });
                }
            })
        } else if (this.config.telemetryProxyEnabled === 'true') {
            this.sendSuccess(res, {
                id: 'api.health'
            });
        } else {
            this.sendError(res, {
                id: 'api.health',
                params: {
                    err: 'Configuration error.'
                }
            });
        }
    }
    getRequestCallBack(req, res) {
        return (err, data) => {
            if (err) {
                this.sendError(res, {
                    id: 'api.telemetry',
                    params: {
                        err: err
                    },
                });
            } else {
                console.log('Proxy successful!  Server responded with:', data.body);
                this.sendSuccess(res, {
                    id: 'api.telemetry',
                });
            }
        }
    }
    sendError(res, options) {
        const resObj = {
            id: options.id,
            ver: options.ver || '1.0',
            ets: new Date().getTime(),
            params: options.params || {},
            responseCode: options.responseCode || 'SERVER_ERROR'
        }
        res.status(500)
        res.json(resObj);
    }
    sendSuccess(res, options) {
        const resObj = {
            id: options.id,
            ver: options.ver || '1.0',
            ets: new Date().getTime(),
            params: options.params || {},
            responseCode: options.responseCode || 'SUCCESS'
        }
        res.status(200)
        res.json(resObj);
    }
    getProxyRequestObj(req, data) {
        const headers = {
            'authorization': 'Bearer ' + config.proxyAuthKey
        };
        if (req.header('content-type'))
            headers['content-type'] = req.get('content-type');
        if (req.header('content-encoding'))
            headers['content-encoding'] = req.get('content-encoding');
        return {
            url: this.config.proxyURL,
            headers: headers,
            body: data
        };
    }
}

const telemetryService = new TelemetryService(config);

module.exports = telemetryService;
