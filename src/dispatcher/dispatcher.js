const winston = require('winston');

const defaultFileOptions = {
  filename: 'dispatcher-%DATE%.log',
  datePattern: 'YYYY-MM-DD',
  maxSize: '100m',
  maxFiles: '100',
  zippedArchive: true,
  json: true
};

class Dispatcher {
  constructor(options) {
    if (!options) throw new Error('Dispatcher options are required');
    this.options = options;
    this.transport = null;
    
    if (this.options.dispatcher === 'kafka') {
      const { KafkaDispatcher } = require('./kafka-dispatcher');
      this.transport = new KafkaDispatcher(this.options);
      console.log('Kafka transport enabled !!!');
    } else if (this.options.dispatcher === 'file') {
      require('winston-daily-rotate-file');
      const config = Object.assign(defaultFileOptions, this.options);
      this.transport = new winston.transports.DailyRotateFile(config);
      console.log('File transport enabled !!!');
    } else if (this.options.dispatcher === 'cassandra') {
      require('./cassandra-dispatcher');
      this.transport = new winston.transports.Cassandra(this.options);
      console.log('Cassandra transport enabled !!!');
    } else { // Log to console
      this.options.dispatcher = 'console';
      const config = Object.assign({json: true,stringify: (obj) => JSON.stringify(obj)}, this.options);
      this.transport = new winston.transports.Console(config);
      console.log('Console transport enabled !!!');
    }
    
    this.logger = winston.createLogger({
      level: 'info',
      transports: [this.transport]
    });
  }

  dispatch(mid, message, callback) {
    // Winston 3.x doesn't support callbacks on logger.log methods
    // The callback is invoked immediately for backward compatibility with existing code
    // If you need to wait for the log to be written, listen to the logger's 'finish' event instead
    this.logger.log('info', message, {mid: mid});
    if (callback) {
      // Call callback immediately to maintain backward compatibility
      setImmediate(callback);
    }
  }

  health(callback) {
    if (this.options.dispatcher === 'kafka') {
      this.transport.health(callback);
    } else if (this.options.dispatcher === 'console') {
      callback(true);
    } else { // need to add health method for file/cassandra
      callback(false);
    }
  }
}

module.exports = { Dispatcher };
