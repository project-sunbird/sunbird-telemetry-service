/**
 * Sunbird Telemetry V3 Specification Type Definitions
 * Official event structures for Sunbird telemetry processing.
 */

export type EventId =
  | 'START'
  | 'END'
  | 'IMPRESSION'
  | 'INTERACT'
  | 'LOG'
  | 'ERROR'
  | 'AUDIT'
  | 'SEARCH'
  | 'EXDATA'
  | 'FEEDBACK'
  | 'INTERRUPT'
  | 'SHARE'
  | 'SUMMARY'
  | 'METRICS';

export type ActorType = 'User' | 'System' | 'Device';

/**
 * Entity generating the telemetry event
 */
export interface TelemetryActor {
  id: string;
  type: ActorType | string;
}

/**
 * Producer application info
 */
export interface TelemetryPData {
  id: string;
  ver?: string;
  pid?: string;
}

/**
 * Contextual data elements (tags, correlation ids)
 */
export interface TelemetryCData {
  id: string;
  type: string;
}

/**
 * Rollup hierarchy context (l1 to l4)
 */
export interface TelemetryRollup {
  l1?: string;
  l2?: string;
  l3?: string;
  l4?: string;
  [key: string]: string | undefined;
}

/**
 * Event context (channel, environment, device ID, producer data)
 */
export interface TelemetryContext {
  channel: string;
  env: string;
  sid?: string;
  did?: string;
  pdata?: TelemetryPData;
  cdata?: TelemetryCData[];
  rollup?: TelemetryRollup;
  [key: string]: any;
}

/**
 * Target object of the telemetry action
 */
export interface TelemetryObject {
  id: string;
  type: string;
  ver?: string;
  rollup?: TelemetryRollup;
}

/**
 * Event specific data (edata) for START event
 */
export interface StartEventEData {
  type: string;
  mode?: string;
  stageid?: string;
  pageid?: string;
  duration?: number;
  [key: string]: any;
}

/**
 * Event specific data (edata) for END event
 */
export interface EndEventEData {
  type: string;
  mode?: string;
  duration?: number;
  pageid?: string;
  summary?: Array<Record<string, any>>;
  [key: string]: any;
}

/**
 * Event specific data (edata) for IMPRESSION event
 */
export interface ImpressionEventEData {
  type: string;
  subtype?: string;
  pageid: string;
  uri?: string;
  duration?: number;
  visits?: Array<{ id: string; type: string }>;
  [key: string]: any;
}

/**
 * Event specific data (edata) for INTERACT event
 */
export interface InteractEventEData {
  type: string;
  id: string;
  'sub-type'?: string;
  subtype?: string;
  pageid?: string;
  extra?: Record<string, any>;
  target?: Record<string, any>;
  plugin?: { id: string; ver: string };
  [key: string]: any;
}

/**
 * Event specific data (edata) for LOG event
 */
export interface LogEventEData {
  type: string;
  level: 'TRACE' | 'DEBUG' | 'INFO' | 'WARN' | 'ERROR' | 'FATAL' | string;
  message?: string;
  params?: Array<Record<string, any>>;
  [key: string]: any;
}

/**
 * Event specific data (edata) for ERROR event
 */
export interface ErrorEventEData {
  err: string;
  errtype: string;
  stacktrace?: string;
  action?: string;
  object?: { id: string; type: string; ver?: string };
  [key: string]: any;
}

/**
 * Event specific data (edata) for AUDIT event
 */
export interface AuditEventEData {
  props?: string[];
  state?: string;
  prevstate?: string;
  [key: string]: any;
}

/**
 * Event specific data (edata) for SEARCH event
 */
export interface SearchEventEData {
  type: string;
  query: string;
  filters?: Record<string, any>;
  sort?: Record<string, any>;
  correlationid?: string;
  size?: number;
  topn?: Array<Record<string, any>>;
  [key: string]: any;
}

/**
 * Event specific data (edata) for EXDATA event
 */
export interface ExDataEventEData {
  type: string;
  data: string;
  [key: string]: any;
}

/**
 * Event specific data (edata) for FEEDBACK event
 */
export interface FeedbackEventEData {
  rating?: number;
  comments?: string;
  [key: string]: any;
}

/**
 * Event specific data (edata) for SHARE event
 */
export interface ShareEventEData {
  dir: 'In' | 'Out' | string;
  type: string;
  items: Array<{ id: string; type: string; ver?: string; params?: Array<Record<string, any>> }>;
  [key: string]: any;
}

export type TelemetryEData =
  | StartEventEData
  | EndEventEData
  | ImpressionEventEData
  | InteractEventEData
  | LogEventEData
  | ErrorEventEData
  | AuditEventEData
  | SearchEventEData
  | ExDataEventEData
  | FeedbackEventEData
  | ShareEventEData
  | Record<string, any>;

/**
 * Generic Base Telemetry Event structure
 */
export interface BaseTelemetryEvent<T = TelemetryEData> {
  eid: EventId | string;
  ets: number;
  ver: string;
  mid?: string;
  actor: TelemetryActor;
  context: TelemetryContext;
  edata: T;
  object?: TelemetryObject;
  tags?: string[];
  did?: string;
  channel?: string;
  pid?: string;
  syncts?: number;
  dataset?: string;
  [key: string]: any;
}

export type TelemetryEvent = BaseTelemetryEvent;
