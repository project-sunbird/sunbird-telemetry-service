import {
  TelemetryEvent,
  BaseTelemetryEvent,
  StartEventEData,
  ImpressionEventEData,
  DispatcherOptions,
  EnvVariables,
  TelemetryApiResponse,
  Dispatcher,
  TelemetryService
} from '../types';

describe('TypeScript Type Definitions Validation', () => {
  it('should compile valid Sunbird Telemetry V3 Event objects', () => {
    const startEventData: StartEventEData = {
      type: 'player',
      mode: 'play',
      pageid: 'splash-screen'
    };

    const telemetryEvent: TelemetryEvent = {
      eid: 'START',
      ets: Date.now(),
      ver: '3.0',
      mid: 'unique-mid-12345',
      actor: {
        id: 'user-789',
        type: 'User'
      },
      context: {
        channel: 'in.sunbird',
        env: 'app',
        did: 'device-abc-123',
        pdata: {
          id: 'org.sunbird.app',
          ver: '1.0.0',
          pid: 'sunbird.portal'
        }
      },
      edata: startEventData
    };

    const impressionEvent: BaseTelemetryEvent<ImpressionEventEData> = {
      eid: 'IMPRESSION',
      ets: Date.now(),
      ver: '3.0',
      actor: { id: 'sys-01', type: 'System' },
      context: { channel: 'channel-1', env: 'home' },
      edata: {
        type: 'view',
        pageid: 'home-page',
        uri: '/home'
      }
    };

    if (telemetryEvent.eid !== 'START' || impressionEvent.eid !== 'IMPRESSION') {
      throw new Error('Type validation failed');
    }
  });

  it('should compile valid Service & Config type structures', () => {
    const dummyConfig: EnvVariables = {
      level: 'info',
      localStorageEnabled: 'true',
      port: 9001,
      threads: 2,
      dataset: 'sb-telemetry',
      allowedOrigins: '*'
    };

    const apiResponse: TelemetryApiResponse = {
      id: 'api.health',
      ver: '1.0',
      ets: Date.now(),
      params: {},
      responseCode: 'SUCCESS'
    };

    if (dummyConfig.port !== 9001 || apiResponse.responseCode !== 'SUCCESS') {
      throw new Error('Config type validation failed');
    }
  });
});
