const chai = require('chai'),
    sinon = require('sinon'),
    expect = chai.expect,
    chaiHttp = require('chai-http'),
    appPath = './../../app',
    request = require('request');
let req, res, app, mockServer;
chai.use(chaiHttp);
describe('route test', () => {

    beforeEach(() => {
        process.env.node_env = 'test'
        process.env.telemetry_local_storage_type = 'kafka';
        app = require(appPath);
        mockServer = chai.request(app);
    });

    after(() => {
        process.env.node_env = 'prod'
    });

    it('should return success if telemetry_local_storage_type is kafka if "/health" is called', function (done) {
        mockServer.get('/health').then(res => {
            expect(res.status).to.equal(200);
            mockServer.close();
            done()
        });
    });

    it('should return success if telemetry_local_storage_type is kafka if  "/v1/telemetry" is called', function (done) {
        mockServer.post('/v1/telemetry').then(res => {
            expect(res.status).to.equal(200);
            done()
        });
    });
});