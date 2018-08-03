var winston = require('winston');
var util = require('util');
var Cassandra = require('winston-cassandra');

Cassandra.prototype._createSchema = function (callback) {
    var createQuery = 'CREATE TABLE ' + this.options.table +
      ' (key text, mid text, date timestamp, level text, message text, meta text, PRIMARY KEY(key, mid));';
    var self = this;
  
    this.client.metadata.getTable(this.options.keyspace, this.options.table, function (err, tableInfo) {
      if (err) return callback(err);
      if (tableInfo) {
        //table is already created
        self.schemaStatus.created = true;
        return callback();
      }
  
      return self.client.execute(createQuery, function (err) {
        self.schemaStatus.created = !err;
        return callback(err);
      });
    });
};

Cassandra.prototype._insertLog = function (level, msg, meta, callback) {
    var key = this.getKey();
    if (!key) {
      return callback(new Error('Partition ' + this.options.partitionBy + ' not supported'), false);
    }
    var query = 'INSERT INTO ' + this.options.table + ' (key, mid, date, level, message, meta) VALUES (?, ?, ?, ?, ?, ?)' + (this.options.cassandraTtl ? (' USING TTL ' + this.options.cassandraTtl) : '');
    //execute as a prepared query as it would be executed multiple times
    return this.client.execute(
      query,
      [key, meta.mid, new Date(), level, msg, util.inspect(meta)],
      {prepare: true, consistency: this.options.consistency},
      callback);
};

winston.transports.Cassandra = Cassandra;
module.exports = Cassandra;