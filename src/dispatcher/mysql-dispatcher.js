"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var mysql = require('mysql');
const winston = require('winston');

class MysqlDispatcher extends winston.Transport {

    constructor(options) {
        super();

        this.options = options;

        this.connection = mysql.createConnection({
            host: this.options.mysqlHost,
            port: this.options.mysqlPort,
            user: this.options.mysqlUser,
            password: this.options.mysqlPassword,
            database: this.options.mysqlDatabase
        });

        // check mysql connection
        this.connection.connect(function (err) {

            // check if any issue with connection
            if (err) {
                console.log("Mysql Connection Error",err)
                process.exit(1);
            } else {
                console.log("MySQL DB Connected!");
            }

        });

        if (this.connection) {

            let table_name = this.options.table;

            // At the first time run, This command will run to create table if table not exist
            let sql = "CREATE TABLE IF NOT EXISTS ?? (id int(11) NOT NULL AUTO_INCREMENT,api_id varchar(255) NOT NULL,ver varchar(15) NOT NULL,params longtext NOT NULL CHECK (json_valid(params)),ets bigint(20) NOT NULL,events longtext NOT NULL CHECK(json_valid(events)),channel varchar(50) NOT NULL,pid varchar(50) NOT NULL,mid varchar(50) NOT NULL,syncts bigint(20) NOT NULL,PRIMARY KEY (id))";

            // for run table creation query
            this.connection.query(sql,[table_name], function (err, result) {
                if (err) throw console.log("error while creating db");
                console.log(result.message);
                console.log("Telemetry table created");
            });
        }
    }

    log(level, msg, meta, callback) {
        console.log("msg", msg);
        let msgData = JSON.parse(msg);
        console.log("msgData", msgData);

        let promises = [];
        let table_name = this.options.table;

        try {
            for (const iterator of msgData.events) {

                //insert query to mysql table one by one
                promises.push(
                    this.connection.query("INSERT INTO ?? (api_id, ver, params, ets, events, channel, pid, mid, syncts) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)", [table_name, msgData.id, msgData.ver, JSON.stringify(msgData.params), msgData.ets, JSON.stringify(iterator), iterator.context.channel, iterator.context.pdata.pid, msgData.mid, msgData.syncts])
                );

            }

        } catch (err) {
            return callback(err);
        }

        Promise.all(promises)
            .then(() => {
                console.log("data inserted successfully!");
                callback()
            }).catch((err) => {
                console.log("Unable to insert data", err);
                return callback(err);
            })
        }
}

winston.transports.mysql = MysqlDispatcher;

module.exports = { MysqlDispatcher }
