var util = require('util');
var winston = require('winston');
var kafka = require('kafka-node')
var HighLevelProducer = kafka.HighLevelProducer;
var KeyedMessage = kafka.KeyedMessage;
var defaultOptions = {
    kafkaHost: "localhost:9092",
    maxAsyncRequests: 100,
    topic: "local.ingestion"
}

function KafkaDispatcher(options) {
    this.name = "kafka";
    this.options = Object.assign(defaultOptions, options);
    this.client = new kafka.KafkaClient({
        kafkaHost: this.options.kafkaHost,
        maxAsyncRequests: this.options.maxAsyncRequests
    })
    this.producer = new HighLevelProducer(this.client);
    this.producer.on('ready', function () {
        console.log("Kafa dispatcher is ready");
    });
    
    this.producer.on('error', function (err) {
        console.error("Unable to connect to kafka");
    });
}

util.inherits(KafkaDispatcher, winston.Transport);
winston.transports.Kafka = KafkaDispatcher;

KafkaDispatcher.prototype.log = function (level, msg, meta, callback) {
    this.producer.send([{topic: this.options.topic, key: meta.mid, messages: msg}], callback);
}

KafkaDispatcher.prototype.health = function(callback) {
    this.client.topicExists(this.options.topic, function(err) {
        if(err)
            callback(false);
        else 
            callback(true);
    });
}

module.exports.KafkaDispatcher = KafkaDispatcher;