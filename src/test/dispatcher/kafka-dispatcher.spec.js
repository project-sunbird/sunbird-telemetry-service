const chai = require('chai'),
    sinon = require('sinon'),
    expect = chai.expect,
    KafkaDispatcher = require('./../../dispatcher/kafka-dispatcher').KafkaDispatcher;
let config;
describe('kafka-dispatcher Service', () => {
    beforeEach(function() {
        config = {
            level: 'info',
            filename: 'telemetry-%DATE%.log',
            maxSize: '100m',
            maxFiles: '100',
            partitionBy: 'hour',
        }
    })

    it('should create kafka dispatcher object with log and health property, if config is passed to constructor', function () {
        const dispatcher = new KafkaDispatcher(config);
        expect(dispatcher).to.be.an('object');
        expect(dispatcher).to.have.property('log');
        expect(dispatcher).to.have.property('producer');
        expect(dispatcher).to.have.property('health');
        expect(dispatcher).to.have.property('client');
    })
    it('should log to kafka if log method is called', function () {
        const dispatcher = new KafkaDispatcher(config);
        cb = () => {};
        sinon.spy(dispatcher.producer, 'send');
        dispatcher.log('level', 'msg', {
            mid: '54335'
        }, cb);
        sinon.assert.calledOnce(dispatcher.producer.send);
        sinon.assert.calledWith(dispatcher.producer.send, [{
            attributes: 0,
            partition: 0,
            topic: "local.ingestion",
            key: '54335',
            messages: 'msg'
        }], cb);
    })
    it('should log to kafka if log method is called', function () {
        const dispatcher = new KafkaDispatcher(config);
        callback = () => {};
        cb = (err) => {
            if (err) callback(false);
            else callback(true);
        };
        sinon.spy(dispatcher.client, 'topicExists');
        dispatcher.health(callback);
        sinon.assert.calledOnce(dispatcher.client.topicExists);
        sinon.assert.calledWith(dispatcher.client.topicExists, "local.ingestion");
    })
});