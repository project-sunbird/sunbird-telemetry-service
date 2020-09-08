const winston = require('winston'),
    kafka = require('kafka-node'),
    _ = require('lodash'),
    HighLevelProducer = kafka.HighLevelProducer,
    defaultOptions = {
        kafkaHost: 'localhost:9092',
        maxAsyncRequests: 100,
        topic: 'local.ingestion',
        compression_type: 'none'
    }

class KafkaDispatcher extends winston.Transport {
    constructor(options) {
        super();
        this.name = 'kafka';
        this.options = _.assignInWith(defaultOptions, options, (objValue, srcValue) => srcValue ? srcValue : objValue);
        if (this.options.compression_type == 'snappy') {
            this.compression_attribute = 2;
        } else if(this.options.compression_type == 'gzip') {
            this.compression_attribute = 1;
        } else {
            this.compression_attribute = 0;
        }
        this.client = new kafka.KafkaClient({
            kafkaHost: this.options.kafkaHost,
            maxAsyncRequests: this.options.maxAsyncRequests
        })
        this.producer = new HighLevelProducer(this.client);
        this.producer.on('ready', () => console.log('kafka dispatcher is ready'));
        this.producer.on('error', (err) => console.error('Unable to connect to kafka', err));
    }
    log(level, msg, meta, callback) {
        this.producer.send([{
            topic: this.options.topic,
            key: meta.mid,
            messages: msg,
            attributes: this.compression_attribute
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

module.exports = { KafkaDispatcher };
