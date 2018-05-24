package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.models.util.ProjectLogger;

/*
 * this class is used for reading properties file
 * for telemetry micro service.
 * @author Amit Kumar
 */
public class TelemetryPropertiesCache {

  private final String[] fileName = {"telemetryresource.properties"};
  private final Properties configProp = new Properties();
  public final Map<String, Float> attributePercentageMap = new ConcurrentHashMap<>();
  private static TelemetryPropertiesCache propertiesCache = null;

  /** private default constructor */
  private TelemetryPropertiesCache() {
    for (String file : fileName) {
      InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
      try {
        configProp.load(in);
      } catch (IOException e) {
        ProjectLogger.log(
            "TelemetryPropertiesCache:TelemetryPropertiesCache:Error in properties cache", e);
      }
    }
  }

  /**
   * This method is responsible for providing singleton object for TelemetryPropertiesCache class.
   *
   * @return TelemetryPropertiesCache singleton instance.
   */
  public static TelemetryPropertiesCache getInstance() {
    if (null == propertiesCache) {
      synchronized (TelemetryPropertiesCache.class) {
        if (null == propertiesCache) {
          propertiesCache = new TelemetryPropertiesCache();
        }
      }
    }
    return propertiesCache;
  }

  /**
   * This method will first read value from System env, if value not present in evn then it will
   * read from resource file based on provided key.
   *
   * @param key key used inside properties file.
   * @return value associated with this key.
   */
  public String readProperty(String key) {
    String val = readPropertyFromEnv(key);
    if (StringUtils.isBlank(val)) {
      return configProp.getProperty(key);
    }
    return val;
  }

  /**
   * THis method will read value from environment variable.
   *
   * @param key used inside environment
   * @return this will return value set inside env. if value is not set then it will return null.
   */
  private String readPropertyFromEnv(String key) {
    return System.getenv(key);
  }
}
