const winston = require('winston'),
  _ = require('lodash'),
  { Kafka, CompressionTypes } = require('kafkajs'),
  config = require('../envVariables'),
  defaultOptions = {
    kafkaHost: 'localhost:9092',
    maxAsyncRequests: 100,
    topic: 'local.ingestion',
    compression_type: 'none'
  };

function mapCompressionAttr(attr) {
  // kafka-node used numeric attributes: 0 = none, 1 = gzip, 2 = snappy
  if (attr === 2) return CompressionTypes.Snappy;
  if (attr === 1) return CompressionTypes.GZIP;
  return CompressionTypes.None;
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

    // kafkajs expects an array of broker strings
    const brokers = (typeof this.options.kafkaHost === 'string') ? [this.options.kafkaHost] : this.options.kafkaHost;
    this._kafka = new Kafka({ brokers });
    this._producer = this._kafka.producer();
    this._admin = this._kafka.admin();

    // Backwards-compatible lightweight wrappers so existing code/tests that
    // expect producer.send(payloads, cb) and client.topicExists(topic, cb)
    // continue to work.
    this.producer = {
      send: (payloads, cb) => {
        // payloads is an array like [{ topic, key, messages, attributes, partition }]
        const topicMessages = payloads.map(p => {
          const msg = { key: p.key, value: p.messages };
          if (p.hasOwnProperty('partition')) msg.partition = p.partition;
          return {
            topic: p.topic,
            messages: [msg],
            compression: mapCompressionAttr(p.attributes)
          };
        });

        // connect producer, send batch, then call callback
        this._producer.connect()
          .then(() => this._producer.sendBatch({ topicMessages }))
          .then(() => { if (cb) cb(); })
          .catch(err => { if (cb) cb(err); });
      }
    };

    this.client = {
      topicExists: (topic, cb) => {
        // kafkajs admin.fetchTopicMetadata throws if topic doesn't exist
        this._admin.connect()
          .then(() => this._admin.fetchTopicMetadata({ topics: [topic] }))
          .then(() => this._admin.disconnect())
          .then(() => cb && cb(null))
          .catch(err => {
            // ensure disconnect
            this._admin.disconnect().catch(() => {});
            cb && cb(err);
          });
      }
    };

    // log basic connection info asynchronously
    this._producer.connect()
      .then(() => console.log('kafka dispatcher producer connected'))
      .catch(err => console.error('Unable to connect kafka producer', err));
    this._admin.connect()
      .then(() => this._admin.disconnect())
      .catch(() => {});
  }

  log(level, msg, meta, callback) {
    // preserve the older kafka-node send signature by delegating to the wrapper
    // msg is expected to be a JSON string. Inject a top-level dataset key
    // from configuration if provided and not already present.
    let outgoing = msg;
    try {
      if (typeof msg === 'string') {
        const parsed = JSON.parse(msg);
        if (parsed && typeof parsed === 'object') {
          if (config.dataset && !parsed.hasOwnProperty('dataset')) {
            parsed.dataset = config.dataset;
          }
          outgoing = JSON.stringify(parsed);
        }
      }
    } catch (e) {
      // if parsing fails, leave the message as-is
    }

    this.producer.send([{
      topic: this.options.topic,
      key: meta && meta.mid,
      messages: outgoing,
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
