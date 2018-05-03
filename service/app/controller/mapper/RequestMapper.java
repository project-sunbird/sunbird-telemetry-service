/**
 * 
 */
package controller.mapper;

import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.responsecode.ResponseCode;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;

/**
 * This class will map the requested JSON data into custom class.
 * if request data is incorrect format then it will throw ProjectCommonException 
 * with 400 error code.
 * @author Manzarul
 *
 */
public class RequestMapper {

  /**
   * Method to map request
   * 
   * @param requestData JsonNode
   * @param obj Class<T>
   * @exception ProjectCommonException
   * @return Object <T>
   */
	public static <T> Object mapRequest(JsonNode requestData, Class<T> obj) throws ProjectCommonException {
		try {
			return Json.fromJson(requestData, obj);
		} catch (Exception e) {
			ProjectLogger.log("ControllerRequestMapper error : ", e);
			throw new ProjectCommonException(ResponseCode.invalidRequestData.getErrorCode(),
					ResponseCode.invalidRequestData.getErrorMessage(), ResponseCode.CLIENT_ERROR.getResponseCode());
		}
	}

}
