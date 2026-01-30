const winston = require('./node_modules/winston');
const Transport = require('./node_modules/winston-transport');

class TestTransport extends Transport {
  log(info, callback) {
    console.log('TestTransport.log called');
    setTimeout(() => {
      console.log('TestTransport completing');
      if (callback) callback();
    }, 100);
  }
}

const transport = new TestTransport();
const logger = winston.createLogger({
  transports: [transport]
});

console.log('Logging...');
logger.info('test message');
console.log('After logger.info');

setTimeout(() => {
  console.log('Done');
}, 200);
