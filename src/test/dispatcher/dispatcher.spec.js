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
    };
  });

  it('should create dispatcher object with logger, dispatch and health property, if config is passed to constructor', function () {
    const dispatcher = new Dispatcher(config);
    expect(dispatcher).to.be.an('object');
    expect(dispatcher).to.have.property('logger');
    expect(dispatcher).to.have.property('dispatch');
    expect(dispatcher).to.have.property('health');
  });

  it('should not create dispatcher if config is not passed to constructor', function () {
    try {
      const dispatcher = new Dispatcher();
    } catch (err) {
      expect(err.message).to.be.a('string');
    }
  });

  it('should create kafka dispatcher if config.dispatcher is passed as "kafka"', function () {
    config.dispatcher = 'kafka';
    const dispatcher = new Dispatcher(config);
    expect(dispatcher.logger).to.have.property('log');
    // Winston 3.x stores transports in an array
    expect(dispatcher.logger.transports).to.be.an('array');
    expect(dispatcher.logger.transports).to.have.lengthOf(1);
    const kafkaTransport = dispatcher.logger.transports[0];
    expect(kafkaTransport).to.be.an('object');
    expect(kafkaTransport).to.have.property('options');
    expect(kafkaTransport.options.topic).to.equal('local.ingestion');
    expect(kafkaTransport.options.level).to.equal('info');
    expect(kafkaTransport.options.dispatcher).to.equal('kafka');
    expect(kafkaTransport.options.kafkaHost).to.equal('localhost:9092');
  });

  it('should log to kafka if config.dispatcher is passed as "kafka" and dispatch is called', function () {
    config.dispatcher = 'kafka';
    cb = () => {};
    const dispatcher = new Dispatcher(config);
    // Spy on transport.log instead of logger.log since we now call it directly
    sinon.spy(dispatcher.transport, 'log');
    dispatcher.dispatch('mid', {}, cb);
    // Winston 3.x stores transports in an array
    expect(dispatcher.logger.transports).to.be.an('array');
    expect(dispatcher.logger.transports).to.have.lengthOf(1);
    sinon.assert.calledOnce(dispatcher.transport.log);
    // Verify the info object structure
    const callArgs = dispatcher.transport.log.getCall(0).args;
    expect(callArgs[0]).to.have.property('level', 'info');
    expect(callArgs[0]).to.have.property('message');
    expect(callArgs[0]).to.have.property('mid', 'mid');
    expect(callArgs[1]).to.be.a('function'); // callback
  });

  it('should check health of kafka if config.dispatcher is passed as "kafka" and health is called', function () {
    config.dispatcher = 'kafka';
    const stub = { cb: () => {}} ;
    sinon.spy(stub , 'cb');
    const dispatcher = new Dispatcher(config);
    // Winston 3.x stores transports in an array, use the transport reference we already have
    sinon.spy(dispatcher.transport, 'health');
    dispatcher.health(stub.cb);
    // Winston 3.x stores transports in an array
    expect(dispatcher.logger.transports).to.be.an('array');
    expect(dispatcher.logger.transports).to.have.lengthOf(1);
    sinon.assert.calledOnce(dispatcher.transport.health);
    sinon.assert.calledWith(dispatcher.transport.health, stub.cb);
  });

  it('should create kafka dispatcher if config.dispatcher is not passed or other than "kafka/file/cassandra"', function () {
    config.dispatcher = 'console';
    const dispatcher = new Dispatcher(config);
    // Winston 3.x stores transports in an array
    expect(dispatcher.logger.transports).to.be.an('array');
    expect(dispatcher.logger.transports).to.have.lengthOf(1);
    expect(dispatcher.logger).to.have.property('log');
    expect(dispatcher.logger.transports[0]).to.be.an('object');
  });

  it('should log to console if config.dispatcher is passed as is not passed or other than "kafka/file/cassandra" and dispatch is called', function () {
    config.dispatcher = 'console';
    const dispatcher = new Dispatcher(config);
    cb = () => {};
    // Spy on transport.log instead of logger.log since we now call it directly
    sinon.spy(dispatcher.transport, 'log');
    dispatcher.dispatch('mid', {}, cb);
    // Winston 3.x stores transports in an array
    expect(dispatcher.logger.transports).to.be.an('array');
    expect(dispatcher.logger.transports).to.have.lengthOf(1);
    sinon.assert.calledOnce(dispatcher.transport.log);
    // Verify the info object structure
    const callArgs = dispatcher.transport.log.getCall(0).args;
    expect(callArgs[0]).to.have.property('level', 'info');
    expect(callArgs[0]).to.have.property('message');
    expect(callArgs[0]).to.have.property('mid', 'mid');
    expect(callArgs[1]).to.be.a('function'); // callback
  });

  it('should check health of dispatcher if config.dispatcher is passed as "console" or  other than "kafka/file/cassandra" and callback should be called with true', function () {
    config.dispatcher = 'console';
    const stub = { cb: () => {}} ;
    const dispatcher = new Dispatcher(config);
    sinon.spy(stub , 'cb');
    dispatcher.health(stub.cb);
    // Winston 3.x stores transports in an array
    expect(dispatcher.logger.transports).to.be.an('array');
    expect(dispatcher.logger.transports).to.have.lengthOf(1);
    sinon.assert.calledOnce(stub.cb);
    sinon.assert.calledWith(stub.cb, true);
  });

  it('should invoke callback after log completes successfully', function (done) {
    config.dispatcher = 'kafka';
    const dispatcher = new Dispatcher(config);
    // Stub the transport's log to simulate successful logging
    sinon.stub(dispatcher.transport, 'log').callsFake((info, callback) => {
      // Simulate async logging
      setImmediate(() => callback(null));
    });
    
    dispatcher.dispatch('mid', {}, (err) => {
      expect(err).to.be.null;
      sinon.assert.calledOnce(dispatcher.transport.log);
      done();
    });
  });

  it('should invoke callback with error when log fails', function (done) {
    config.dispatcher = 'kafka';
    const dispatcher = new Dispatcher(config);
    const testError = new Error('Log failed');
    // Stub the transport's log to simulate logging failure
    sinon.stub(dispatcher.transport, 'log').callsFake((info, callback) => {
      // Simulate async logging failure
      setImmediate(() => callback(testError));
    });
    
    dispatcher.dispatch('mid', {}, (err) => {
      expect(err).to.equal(testError);
      sinon.assert.calledOnce(dispatcher.transport.log);
      done();
    });
  });

  it('should check health of dispatcher if config.dispatcher is passed as "file/cassandra" and callback should be called with false', function () {
    config.dispatcher = 'file';
    const stub = { cb: () => {}} ;
    const dispatcher = new Dispatcher(config);
    sinon.spy(stub , 'cb');
    dispatcher.health(stub.cb);
    sinon.assert.calledOnce(stub.cb);
    sinon.assert.calledWith(stub.cb, false);
  });
});