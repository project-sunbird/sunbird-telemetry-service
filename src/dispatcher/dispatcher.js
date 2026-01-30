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
    this.logger = new(winston.Logger)({level: 'info'});
    this.options = options;
    this.transport = null;
    if (this.options.dispatcher == 'kafka') {
      require('./kafka-dispatcher');
      this.transport = new winston.transports.Kafka(this.options);
      this.logger.add(this.transport);
      console.log('Kafka transport enabled !!!');
    } else if (this.options.dispatcher == 'file') {
      require('winston-daily-rotate-file');
      const config = Object.assign(defaultFileOptions, this.options);
      this.transport = new winston.transports.DailyRotateFile(config);
      this.logger.add(this.transport);
      console.log('File transport enabled !!!');
    } else if (this.options.dispatcher === 'cassandra') {
      require('./cassandra-dispatcher');
      this.transport = new winston.transports.Cassandra(this.options);
      this.logger.add(this.transport);
      console.log('Cassandra transport enabled !!!');
    } else { // Log to console
      this.options.dispatcher = 'console';
      const config = Object.assign({json: true,stringify: (obj) => JSON.stringify(obj)}, this.options);
      this.transport = new winston.transports.Console(config);
      this.logger.add(this.transport);
      console.log('Console transport enabled !!!');
    }
  }

  dispatch(mid, message, callback) {
    this.logger.log('info', message, {mid: mid}, callback);
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
