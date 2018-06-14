var util = require('util');
var winston = require('winston');
var uuidv1 = require('uuid/v1');
var kafka = require('kafka-node')
var HighLevelProducer = kafka.HighLevelProducer;
var KeyedMessage = kafka.KeyedMessage;
var defaultOptions = {
    kafkaHost: "localhost:9092",
    maxAsyncRequests: 100,
    topic: "local.ingestion"
}

function KafkaDispatcher(options) {
    this.options = Object.assign(defaultOptions, options);
    var client = new kafka.KafkaClient({
        kafkaHost: this.options.telemetry_kafka_broker_list,
        maxAsyncRequests: this.options.maxAsyncRequests
    })
    this.producer = new HighLevelProducer(client);
    this.producer.on('ready', function () {
        console.log("Kafa dispatcher is ready");
    });
    
    this.producer.on('error', function (err) {
        console.error("Unable to connect to kafka");
    })
}

util.inherits(KafkaDispatcher, winston.Transport);
winston.transports.Kafka = KafkaDispatcher;

KafkaDispatcher.prototype.log = function (level, msg, meta, callback) {
    const key = {did: meta.did, channel: meta.channel, appId: meta.appId, msgId: uuidv1(), encType: meta.encodingType, contentType: meta.contentType, syncts: new Date().getTime()};
    let km = new KeyedMessage(JSON.stringify(key), JSON.stringify(msg));
    this.producer.send([{topic: this.options.topic, messages: km}], callback);
}

module.exports.KafkaDispatcher = KafkaDispatcher;