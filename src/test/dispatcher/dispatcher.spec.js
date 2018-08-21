const chai = require('chai'),
    sinon = require('sinon'),
    expect = chai.expect,
    Dispatcher = require('./../../dispatcher/dispatcher').Dispatcher;
let config;

describe('dispatcher Service', () => {
    beforeEach(function() {
        config = {
            level: 'info',
            filename: 'telemetry-%DATE%.log',
            maxSize: '100m',
            maxFiles: '100',
            partitionBy: 'hour',
            keyspace: 'telemetry',
            contactPoints: 'localhost:9002'
        }
    })

    it('should create dispatcher object with logger, dispatch and health property, if config is passed to constructor', function () {
        const dispatcher = new Dispatcher(config);
        expect(dispatcher).to.be.an('object');
        expect(dispatcher).to.have.property('logger');
        expect(dispatcher).to.have.property('dispatch');
        expect(dispatcher).to.have.property('health');
    })

    it('should not create dispatcher if config is not passed to constructor', function () {
        try {
            const dispatcher = new Dispatcher();
        } catch (err) {
            expect(err.message).to.be.a('string');
        }
    })

    it('should create kafka dispatcher if config.dispatcher is passed as "kafka"', function () {
        config.dispatcher = 'kafka';
        const dispatcher = new Dispatcher(config);
        expect(dispatcher.logger).to.have.property('log');
        expect(dispatcher.logger.transports).to.have.property('kafka');
        expect(dispatcher.logger.transports.kafka).to.be.an('object');
        expect(dispatcher.logger.transports.kafka).to.have.property('options');
        expect(dispatcher.logger.transports.kafka.options.topic).to.equal('local.ingestion')
        expect(dispatcher.logger.transports.kafka.options.level).to.equal('info')
        expect(dispatcher.logger.transports.kafka.options.dispatcher).to.equal('kafka')
        expect(dispatcher.logger.transports.kafka.options.kafkaHost).to.equal('localhost:9092')
    })

    it('should log to kafka if config.dispatcher is passed as "kafka" and dispatch is called', function () {
        config.dispatcher = 'kafka';
        cb = () => {};
        const dispatcher = new Dispatcher(config);
        sinon.spy(dispatcher.logger, 'log');
        dispatcher.dispatch('mid', {}, cb);
        expect(dispatcher.logger.transports).to.have.property('kafka');
        sinon.assert.calledOnce(dispatcher.logger.log);
        sinon.assert.calledWith(dispatcher.logger.log, 'info', {}, {
            mid: 'mid'
        }, cb);
    })

    it('should check health of kafka if config.dispatcher is passed as "kafka" and health is called', function () {
        config.dispatcher = 'kafka';
        const stub = { cb: () => {}} ;
        sinon.spy(stub , 'cb');
        const dispatcher = new Dispatcher(config);
        sinon.spy(dispatcher.logger.transports.kafka, 'health');
        dispatcher.health(stub.cb);
        expect(dispatcher.logger.transports).to.have.property('kafka');
        sinon.assert.calledOnce(dispatcher.logger.transports.kafka.health);
        sinon.assert.calledWith(dispatcher.logger.transports.kafka.health, stub.cb);
    })

    it('should create kafka dispatcher if config.dispatcher is not passed or other than "kafka/file/cassandra"', function () {
        config.dispatcher = 'console';
        const dispatcher = new Dispatcher(config);
        expect(dispatcher.logger.transports).to.have.property('console');
        expect(dispatcher.logger).to.have.property('log');
        expect(dispatcher.logger.transports.console).to.be.an('object');
    })

    it('should log to console if config.dispatcher is passed as is not passed or other than "kafka/file/cassandra" and dispatch is called', function () {
        config.dispatcher = 'console';
        const dispatcher = new Dispatcher(config);
        cb = () => {}
        sinon.spy(dispatcher.logger, 'log');
        dispatcher.dispatch('mid', {}, cb);
        expect(dispatcher.logger.transports).to.have.property('console');
        sinon.assert.calledOnce(dispatcher.logger.log);
        sinon.assert.calledWith(dispatcher.logger.log, 'info', {}, {
            mid: 'mid'
        }, cb);
    })

    it('should check health of dispatcher if config.dispatcher is passed as "console" or  other than "kafka/file/cassandra" and callback should be called with true', function () {
        config.dispatcher = 'console';
        const stub = { cb: () => {}} ;
        const dispatcher = new Dispatcher(config);
        sinon.spy(stub , 'cb');
        dispatcher.health(stub.cb);
        expect(dispatcher.logger.transports).to.have.property('console');
        sinon.assert.calledOnce(stub.cb);
        sinon.assert.calledWith(stub.cb, true);
    })

    it('should check health of dispatcher if config.dispatcher is passed as "file/cassandra" and callback should be called with false', function () {
        config.dispatcher = 'file';
        const stub = { cb: () => {}} ;
        const dispatcher = new Dispatcher(config);
        sinon.spy(stub , 'cb');
        dispatcher.health(stub.cb);
        sinon.assert.calledOnce(stub.cb);
        sinon.assert.calledWith(stub.cb, false);
    })
});