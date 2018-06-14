var winston = require('winston');
require('winston-daily-rotate-file');

const defaultFileOptions = {
    filename: 'dispatcher-%DATE%.log',
    datePattern: 'YYYY-MM-DD',
    maxsize: '100m',
    maxFiles: '100',
    zippedArchive: true,
    json: true, 
    stringify: function(obj){return JSON.stringify(obj)}
}

function Dispatcher (options) {
    if (!options) {
        throw new Error('Dispatcher options are required');
    }
    this.logger = new (winston.Logger)({level: 'info'});
    if(options.dispatcher == 'kafka') {
        require('./kafka-dispatcher');
        this.logger.add(winston.transports.Kafka, options);
    } else if(options.dispatcher == 'file') {
        const config = Object.assign(defaultFileOptions, options);
        this.logger.add(winston.transports.DailyRotateFile, config);
    } else {
        // Log to console
        const config = Object.assign({json: true, stringify: function(obj){return JSON.stringify(obj)}}, options);
        this.logger.add(winston.transports.Console, config);
    }
}

Dispatcher.prototype.dispatch = function(headers, message, cb) {
    this.logger.log('info', message, headers, cb);
};
module.exports.Dispatcher = Dispatcher;