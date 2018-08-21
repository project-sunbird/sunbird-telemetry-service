const uuidv1 = require('uuid/v1'),
    request = require('request'),
    DispatcherClass = require('../dispatcher/dispatcher').Dispatcher;
    config = require('../envVariables')

// TODO: Make this efficient. Implementation to be similar to typesafe config. Right now one configuration holds 
// together all supported transport configurations

class TelemetryService {
    constructor(Dispatcher, config) {
        this.config = config;
        this.dispatcher = this.config.localStorageEnabled === 'true' ? new Dispatcher(config) : undefined;
    }
    dispatch(req, res) {
        const message = req.body;
        message.did = req.get('x-device-id');
        message.channel = req.get('x-channel-id');
        message.pid = req.get('x-app-id');
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
                request.post(options, (err, data) => {
                    if (err) console.error('Proxy failed:', err);
                    else console.log('Proxy successful!  Server responded with:', data.body);
                });
                this.dispatcher.dispatch(message.mid, data, this.getRequestCallBack(req, res));
            } else if (this.config.localStorageEnabled !== 'true' && this.config.telemetryProxyEnabled === 'true') {
                // Just proxy
                const options = this.getProxyRequestObj(req, data);
                request.post(options, this.getRequestCallBack(req, res));
            }
        } else {
            this.sendError(res, { id: 'api.telemetry', params: { err: 'Configuration error' }});
        }
    }
    health(req, res) {
        if (this.config.localStorageEnabled === 'true') {
            this.dispatcher.health((healthy) => {
                if (healthy) 
                    this.sendSuccess(res, { id: 'api.health' });
                else 
                    this.sendError(res, { id: 'api.health', params: { err: 'Telemetry API is unhealthy' } });
            })
        } else if (this.config.telemetryProxyEnabled === 'true') {
            this.sendSuccess(res, { id: 'api.health' });
        } else {
            this.sendError(res, { id: 'api.health', params: { err: 'Configuration error' } });
        }
    }
    getRequestCallBack(req, res) {
        return (err, data) => {
            if (err) this.sendError(res, { id: 'api.telemetry', params: { err: err } });
            else this.sendSuccess(res, { id: 'api.telemetry' });
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
        res.status(500);
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
        res.status(200);
        res.json(resObj);
    }
    getProxyRequestObj(req, data) {
        const headers = { 'authorization': 'Bearer ' + config.proxyAuthKey };
        if (req.get('content-type')) headers['content-type'] = req.get('content-type');
        if (req.get('content-encoding')) headers['content-encoding'] = req.get('content-encoding');
        return {
            url: this.config.proxyURL,
            headers: headers,
            body: data
        };
    }
}

module.exports = new TelemetryService(DispatcherClass, config);
