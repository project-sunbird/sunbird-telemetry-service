const chai = require('chai'),
    sinon = require('sinon'),
    expect = chai.expect,
    telemetryServicePath = './../../service/telemetry-service',
    envVariablesPath = './../../envVariables',
    request = require('request');
let req, res, date, fakePostMethod;

describe('telemetry Service', () => {

    beforeEach(() => {
        delete require.cache[require.resolve(envVariablesPath)] // deleting cached envVariables to test different configurations
        delete require.cache[require.resolve(telemetryServicePath)] // deleting cached telemetryService to test different configurations
        req = sinon.stub({header: () => true, get: () => 'header', body: {}});
        res = sinon.stub({status: () =>{}, json: ()=>{}});
        date = sinon.useFakeTimers();
        fakePostMethod = sinon.stub(request, "post");
    });

    afterEach(() => {
        fakePostMethod.restore(); // Unwraps the spy
    });

    it('should return telemetryService with dispatch/health methods', function () {
        telemetryService = require(telemetryServicePath);
        expect(telemetryService).to.be.an('object');
        expect(telemetryService).to.have.property('dispatch');
        expect(telemetryService).to.have.property('health');
    });

    it('should return telemetryService with config fetched from environment variables', function () {
        process.env.telemetry_local_storage_enabled = 'true';
        process.env.telemetry_proxy_enabled = 'false';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = 'kafka';
        telemetryService = require(telemetryServicePath);
        expect(telemetryService.config.localStorageEnabled).to.equal(process.env.telemetry_local_storage_enabled);
        expect(telemetryService.config.telemetryProxyEnabled).to.equal(process.env.telemetry_proxy_enabled);
        expect(telemetryService.config.level).to.equal(process.env.telemetry_log_level);
        expect(telemetryService.config.dispatcher).to.equal(process.env.telemetry_local_storage_type);
        expect(telemetryService.config.kafkaHost).to.equal(process.env.telemetry_kafka_broker_list);
        expect(telemetryService.config.topic).to.equal(process.env.telemetry_kafka_topic);
        expect(telemetryService.config.proxyURL).to.equal(process.env.telemetry_proxy_url);
        expect(telemetryService.config.proxyAuthKey).to.equal(process.env.telemetry_proxy_auth_key);
        expect(telemetryService.config.encodingType).to.equal(process.env.telemetry_encoding_type);
    });

    it('should return telemetryService with kafka dispatcher if "telemetry_local_storage_type" is "kafka"', function () {
        process.env.telemetry_local_storage_enabled = 'true';
        process.env.telemetry_proxy_enabled = 'false';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = 'kafka';
        telemetryService = require(telemetryServicePath);
        expect(telemetryService.dispatcher.logger.transports).to.have.property('kafka');
    });

    it('should return telemetryService with console dispatcher if "telemetry_local_storage_type" is "undefined" or other than "kafka/file/cassandra"', function () {
        process.env.telemetry_local_storage_enabled = 'true';
        process.env.telemetry_proxy_enabled = 'false';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = undefined;
        telemetryService = require(telemetryServicePath);
        expect(telemetryService.dispatcher.logger.transports).to.have.property('console');
    });

    it('should send success if health method is called, if configured dispatcher is health and (localStorageEnabled is true)  and (telemetryProxyEnabled is true/false)', function () {
        process.env.telemetry_local_storage_enabled = 'true';
        process.env.telemetry_proxy_enabled = 'false';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = 'kafka';
        telemetryService = require(telemetryServicePath);
        sinon.stub(telemetryService.dispatcher, 'health').callsFake((cb) => cb(true));
        telemetryService.health(req, res);
        sinon.assert.calledOnce(telemetryService.dispatcher.health);
        sinon.assert.calledWith(res.status, 200);
        sinon.assert.calledOnce(res.status);
        sinon.assert.calledOnce(res.json);
        sinon.assert.calledWith(res.json, {
            id: 'api.health',
            ver: '1.0',
            ets: 0,
            params: {},
            responseCode: 'SUCCESS'
        });
    });

    it('should send error if health method is called, if configured dispatcher is health and (localStorageEnabled is true)  and (telemetryProxyEnabled is true/false) ', function () {
        process.env.telemetry_local_storage_enabled = 'true';
        process.env.telemetry_proxy_enabled = 'false';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = 'kafka';
        telemetryService = require(telemetryServicePath);
        sinon.stub(telemetryService.dispatcher, 'health').callsFake((cb) => cb(false));
        telemetryService.health(req, res);
        sinon.assert.calledOnce(telemetryService.dispatcher.health);
        sinon.assert.calledWith(res.status, 500);
        sinon.assert.calledOnce(res.status);
        sinon.assert.calledOnce(res.json);
        sinon.assert.calledWith(res.json, {
            id: 'api.health',
            ver: '1.0',
            ets: 0,
            params: {err: 'Telemetry API is unhealthy'},
            responseCode: 'SERVER_ERROR'
        });
    });

    it('should send success if health method is called, if (localStorageEnabled is false)  and (telemetryProxyEnabled is true)', function () {
        process.env.telemetry_local_storage_enabled = 'false';
        process.env.telemetry_proxy_enabled = 'true';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = 'kafka';
        telemetryService = require(telemetryServicePath);
        telemetryService.health(req, res);
        sinon.assert.calledWith(res.status, 200);
        sinon.assert.calledOnce(res.status);
        sinon.assert.calledOnce(res.json);
        sinon.assert.calledWith(res.json, {
            id: 'api.health',
            ver: '1.0',
            ets: 0,
            params: {},
            responseCode: 'SUCCESS'
        });
    });

    it('should send error if health method is called, if (localStorageEnabled is false)  and (telemetryProxyEnabled is false)', function () {
        process.env.telemetry_local_storage_enabled = 'false';
        process.env.telemetry_proxy_enabled = 'false';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = 'kafka';
        telemetryService = require(telemetryServicePath);
        telemetryService.health(req, res);
        sinon.assert.calledWith(res.status, 500);
        sinon.assert.calledOnce(res.status);
        sinon.assert.calledOnce(res.json);
        sinon.assert.calledWith(res.json, {
            id: 'api.health',
            ver: '1.0',
            ets: 0,
            params: {err: 'Configuration error'},
            responseCode: 'SERVER_ERROR'
        });
    });

    it('should send success if dispatch method is called, if configured dispatcher dispatch/logs events and (localStorageEnabled is true) and should not proxy to configured url if (telemetryProxyEnabled is false)', function () {
        process.env.telemetry_local_storage_enabled = 'true';
        process.env.telemetry_proxy_enabled = 'false';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = 'kafka';
        telemetryService = require(telemetryServicePath);
        sinon.stub(telemetryService.dispatcher, 'dispatch').callsFake((message, body, cb) => cb(null, {body: {}}));
        telemetryService.dispatch(req, res);
        sinon.assert.calledOnce(telemetryService.dispatcher.dispatch);
        sinon.assert.notCalled(request.post);
        sinon.assert.calledWith(res.status, 200);
        sinon.assert.calledOnce(res.status);
        sinon.assert.calledOnce(res.json);
        sinon.assert.calledWith(res.json, {
            id: 'api.telemetry',
            ver: '1.0',
            ets: 0,
            params: {},
            responseCode: 'SUCCESS'
        });
    });

    it('should send error if dispatch method is called, if configured dispatcher fails to dispatch/logs events and (localStorageEnabled is true) and should not proxy to configured url if (telemetryProxyEnabled is false)', function () {
        process.env.telemetry_local_storage_enabled = 'true';
        process.env.telemetry_proxy_enabled = 'false';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = 'kafka';
        telemetryService = require(telemetryServicePath);
        sinon.stub(telemetryService.dispatcher, 'dispatch').callsFake((message, body, cb) => cb('not found', {body: {}}));
        telemetryService.dispatch(req, res);
        sinon.assert.calledOnce(telemetryService.dispatcher.dispatch);
        sinon.assert.notCalled(request.post);
        sinon.assert.calledWith(res.status, 500);
        sinon.assert.calledOnce(res.status);
        sinon.assert.calledOnce(res.json);
        sinon.assert.calledWith(res.json, {
            id: 'api.telemetry',
            ver: '1.0',
            ets: 0,
            params: { err: 'not found' },
            responseCode: 'SERVER_ERROR'
        });
    });

    it('should send success if dispatch method is called, if configured dispatcher dispatch/logs events and (localStorageEnabled is true), (telemetryProxyEnabled is true) and telemetry was Proxied to configured path', function () {
        process.env.telemetry_local_storage_enabled = 'true';
        process.env.telemetry_proxy_enabled = 'true';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = 'kafka';
        telemetryService = require(telemetryServicePath);
        fakePostMethod.callsFake(({}, cb) => { cb(null, { body: {mes: 'success' }})});
        sinon.stub(telemetryService.dispatcher, 'dispatch').callsFake((message, body, cb) => cb(null, {body: {}}));
        telemetryService.dispatch(req, res);
        sinon.assert.calledOnce(telemetryService.dispatcher.dispatch);
        sinon.assert.calledOnce(request.post);
        sinon.assert.calledWith(res.status, 200);
        sinon.assert.calledOnce(res.status);
        sinon.assert.calledOnce(res.json);
        sinon.assert.calledWith(res.json, {
            id: 'api.telemetry',
            ver: '1.0',
            ets: 0,
            params: {},
            responseCode: 'SUCCESS'
        });
    });

    it('should send success if dispatch method is called, if configured dispatcher dispatch/logs events and (localStorageEnabled is true), (telemetryProxyEnabled is true) and telemetry Proxy to configured path fails', function () {
        process.env.telemetry_local_storage_enabled = 'true';
        process.env.telemetry_proxy_enabled = 'true';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = 'kafka';
        telemetryService = require(telemetryServicePath);
        fakePostMethod.callsFake(({}, cb) => { cb({ mes: 'proxy failed' }, null )});
        sinon.stub(telemetryService.dispatcher, 'dispatch').callsFake((message, body, cb) => cb(null, {body: {}}));
        telemetryService.dispatch(req, res);
        sinon.assert.calledOnce(telemetryService.dispatcher.dispatch);
        sinon.assert.calledOnce(request.post);
        sinon.assert.calledWith(res.status, 200);
        sinon.assert.calledOnce(res.status);
        sinon.assert.calledOnce(res.json);
        sinon.assert.calledWith(res.json, {
            id: 'api.telemetry',
            ver: '1.0',
            ets: 0,
            params: {},
            responseCode: 'SUCCESS'
        });
    });

    it('should send success if dispatch method is called, (localStorageEnabled is false), (telemetryProxyEnabled is true) and telemetry was Proxied to configured path', function () {
        process.env.telemetry_local_storage_enabled = 'false';
        process.env.telemetry_proxy_enabled = 'true';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = 'kafka';
        telemetryService = require(telemetryServicePath);
        fakePostMethod.callsFake(({}, cb) => { cb(null, { body: {mes: 'success' }})});
        telemetryService.dispatch(req, res);
        expect(telemetryService.dispatcher).to.equal(undefined);
        sinon.assert.calledOnce(request.post);
        sinon.assert.calledWith(res.status, 200);
        sinon.assert.calledOnce(res.status);
        sinon.assert.calledOnce(res.json);
        sinon.assert.calledWith(res.json, {
            id: 'api.telemetry',
            ver: '1.0',
            ets: 0,
            params: {},
            responseCode: 'SUCCESS'
        });
    });

    it('should send error if dispatch method is called, (localStorageEnabled is false), (telemetryProxyEnabled is true) and telemetry was not Proxied to configured path', function () {
        process.env.telemetry_local_storage_enabled = 'false';
        process.env.telemetry_proxy_enabled = 'true';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = 'kafka';
        telemetryService = require(telemetryServicePath);
        fakePostMethod.callsFake(({}, cb) => { cb('proxy failed', null )});
        telemetryService.dispatch(req, res);
        expect(telemetryService.dispatcher).to.equal(undefined);
        sinon.assert.calledOnce(request.post);
        sinon.assert.calledWith(res.status, 500);
        sinon.assert.calledOnce(res.status);
        sinon.assert.calledOnce(res.json);
        sinon.assert.calledWith(res.json, {
            id: 'api.telemetry',
            ver: '1.0',
            ets: 0,
            params: { err: 'proxy failed' },
            responseCode: 'SERVER_ERROR'
        });
    });

    it('should send error if dispatch method is called, (localStorageEnabled is false) and (telemetryProxyEnabled is false)', function () {
        process.env.telemetry_local_storage_enabled = 'false';
        process.env.telemetry_proxy_enabled = 'false';
        process.env.telemetry_log_level = 'info';
        process.env.telemetry_local_storage_type = 'kafka';
        telemetryService = require(telemetryServicePath);
        telemetryService.dispatch(req, res);
        expect(telemetryService.dispatcher).to.equal(undefined);
        sinon.assert.notCalled(request.post);
        sinon.assert.calledWith(res.status, 500);
        sinon.assert.calledOnce(res.status);
        sinon.assert.calledOnce(res.json);
        sinon.assert.calledWith(res.json, {
            id: 'api.telemetry',
            ver: '1.0',
            ets: 0,
            params: { err: 'Configuration error' },
            responseCode: 'SERVER_ERROR'
        });
    });
})