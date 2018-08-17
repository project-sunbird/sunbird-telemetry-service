const winston = require('winston'),
kafka = require('kafka-node'),
_ = require('lodash'),
HighLevelProducer = kafka.HighLevelProducer,
defaultOptions = {
    kafkaHost: "localhost:9092",
    maxAsyncRequests: 100,
    topic: "local.ingestion"
}

class KafkaDispatcher extends winston.Transport {
    constructor(options) {
        super();
        this.name = "kafka";
        this.options = _.assignInWith(defaultOptions, options, (objValue, srcValue) => srcValue ? srcValue : objValue);
        this.client = new kafka.KafkaClient({
            kafkaHost: this.options.kafkaHost,
            maxAsyncRequests: this.options.maxAsyncRequests
        })
        this.producer = new HighLevelProducer(this.client);
        this.producer.on('ready', () => console.log("kafka dispatcher is ready"));
        this.producer.on('error', (err) => console.error("Unable to connect to kafka"));
    }
    log(level, msg, meta, callback) {
        this.producer.send([{
            topic: this.options.topic,
            key: meta.mid,
            messages: msg
        }], callback);
    }
    health(callback) {
        this.client.topicExists(this.options.topic, (err) => {
            if (err) callback(false);
            else callback(true);
        });
    }
}
winston.transports.Kafka = KafkaDispatcher;

module.exports.KafkaDispatcher = KafkaDispatcher;