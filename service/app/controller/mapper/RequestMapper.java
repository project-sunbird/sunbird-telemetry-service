package controller.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.responsecode.ResponseCode;
import play.libs.Json;

/**
 * This class will map the requested JSON data into custom class. if request data is incorrect
 * format then it will throw ProjectCommonException with 400 error code.
 *
 * @author Manzarul
 */
public class RequestMapper {

  /**
   * This method will map request json data to provided class. if data is not able to mapped with
   * provided class then it will throw ProjectCommonException with status code 400 and message
   * invalid request data.
   *
   * @param requestData JsonNode
   * @param obj Class<T>
   * @return <T> Object
   * @throws ProjectCommonException
   */
  public static <T> Object mapRequest(JsonNode requestData, Class<T> obj)
      throws ProjectCommonException {
    try {
      return Json.fromJson(requestData, obj);
    } catch (Exception e) {
      ProjectLogger.log("ControllerRequestMapper error : ", e);
      throw new ProjectCommonException(
          ResponseCode.invalidRequestData.getErrorCode(),
          ResponseCode.invalidRequestData.getErrorMessage(),
          ResponseCode.CLIENT_ERROR.getResponseCode());
    }
  }
}
