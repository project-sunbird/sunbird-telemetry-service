import { Request, Response } from 'express';
import * as winston from 'winston';
import Transport from 'winston-transport';

export * from './telemetry-events';

/**
 * Dispatcher Types and Interfaces
 */
export type DispatcherMode = 'kafka' | 'file' | 'cassandra' | 'console';

export interface BaseDispatcherOptions {
  dispatcher?: DispatcherMode | string;
  [key: string]: any;
}

export interface KafkaDispatcherOptions extends BaseDispatcherOptions {
  kafkaHost?: string | string[];
  maxAsyncRequests?: number;
  topic?: string;
  compression_type?: 'none' | 'gzip' | 'snappy' | string;
}

export interface FileDispatcherOptions extends BaseDispatcherOptions {
  filename?: string;
  datePattern?: string;
  maxSize?: string;
  maxFiles?: string;
  zippedArchive?: boolean;
  json?: boolean;
}

export interface CassandraDispatcherOptions extends BaseDispatcherOptions {
  table?: string;
  keyspace?: string;
  contactPoints?: string[];
  cassandraTtl?: string | number;
  consistency?: any;
  partitionBy?: string;
}

export type DispatcherOptions =
  | KafkaDispatcherOptions
  | FileDispatcherOptions
  | CassandraDispatcherOptions
  | BaseDispatcherOptions;

export class KafkaDispatcher extends Transport {
  name: string;
  options: KafkaDispatcherOptions;
  compression_attribute: number;
  producer: {
    send(payloads: Array<{ topic?: string; key?: string; messages: string; attributes?: number; partition?: number }>, cb?: (err?: Error | null) => void): void;
  };
  client: {
    topicExists(topic: string, cb?: (err?: Error | null) => void): void;
  };
  constructor(options: KafkaDispatcherOptions);
  log(info: any, callback: (err?: Error | null) => void): void;
  health(callback: (healthy: boolean) => void): void;
}

export class Dispatcher {
  options: DispatcherOptions;
  transport: any;
  logger: winston.Logger;
  constructor(options: DispatcherOptions);
  dispatch(mid: string, message: string, callback?: (err: Error | null) => void): void;
  health(callback: (healthy: boolean) => void): void;
}

/**
 * Environment Variables Interface
 */
export interface EnvVariables {
  level: string;
  localStorageEnabled: string;
  telemetryProxyEnabled?: string;
  dispatcher?: DispatcherMode | string;
  proxyURL?: string;
  proxyAuthKey?: string;
  encodingType?: string;
  kafkaHost?: string | string[];
  topic?: string;
  compression_type?: string;
  filename?: string;
  maxSize?: string;
  maxFiles?: string;
  partitionBy?: string;
  keyspace?: string;
  contactPoints?: string[];
  cassandraTtl?: string | number;
  port: number | string;
  threads: number | string;
  dataset: string;
  allowedOrigins: string;
}

/**
 * Telemetry Service Response & Options
 */
export interface TelemetryApiResponse<T = any> {
  id: string;
  ver: string;
  ets: number;
  params: {
    err?: string;
    errmsg?: string;
    status?: string;
    [key: string]: any;
  };
  responseCode: string;
  result?: T;
}

export interface TelemetryServiceOptions {
  id: string;
  ver?: string;
  params?: Record<string, any>;
  responseCode?: string;
}

export interface TelemetryRequestHeaders {
  'x-device-id'?: string;
  'x-channel-id'?: string;
  'x-app-id'?: string;
  'content-type'?: string;
  'content-encoding'?: string;
  [key: string]: string | undefined;
}

/**
 * Telemetry Service Class Interface
 */
export class TelemetryService {
  config: EnvVariables;
  dispatcher?: Dispatcher;
  constructor(dispatcherClass: typeof Dispatcher, config: EnvVariables);
  dispatch(req: Request, res: Response): void;
  health(req: Request, res: Response): void;
  getRequestCallBack(req: Request, res: Response): (err: Error | null, data?: any) => void;
  sendError(res: Response, options: TelemetryServiceOptions): void;
  sendSuccess(res: Response, options: TelemetryServiceOptions): void;
  getProxyRequestObj(req: Request, data: string): { url?: string; headers: Record<string, string>; body: string };
}

declare const serviceInstance: TelemetryService;
export default serviceInstance;
