package controllers;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.response.ResponseParams;
import org.sunbird.common.models.response.ResponseParams.StatusType;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.common.request.ExecutionContext;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;

import play.mvc.Result;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Results;

/**
 * This controller we can use for writing some common method.
 * 
 * @author Manzarul
 */
public class BaseController extends Controller {

	public static Result createCommonExceptionResult(String path, Exception exception) {
		Response reponse = createResponseOnException(path, exception);
		int status = ResponseCode.SERVER_ERROR.getResponseCode();
		if (exception instanceof ProjectCommonException) {
			ProjectCommonException me = (ProjectCommonException) exception;
			status = me.getResponseCode();
		}
		return Results.status(status, Json.toJson(reponse));
	}

	/**
	 * This method will handle response in case of exception
	 *
	 * @param request
	 *            play.mvc.Http.Request
	 * @param exception
	 *            ProjectCommonException
	 * @return Response
	 */
	public static Response createResponseOnException(String path, Exception exception) {
		Response response = getErrorResponse(exception);
		response.setVer("");
		if (path != null) {
			response.setVer(getApiVersion(path));
		}
		response.setId(getApiResponseId());
		response.setTs(ProjectUtil.getFormattedDate());
		return response;
	}

	protected static Response getErrorResponse(Exception e) {
		Response response = new Response();
		ResponseParams resStatus = new ResponseParams();
		String message = setMessage(e);
		resStatus.setErrmsg(message);
		resStatus.setStatus(StatusType.failed.name());
		if (e instanceof ProjectCommonException) {
			ProjectCommonException me = (ProjectCommonException) e;
			resStatus.setErr(me.getCode());
			response.setResponseCode(ResponseCode.getHeaderResponseCode(me.getResponseCode()));
		} else {
			resStatus.setErr(ResponseCode.SERVER_ERROR.name());
			response.setResponseCode(ResponseCode.SERVER_ERROR);
		}
		response.setParams(resStatus);
		return response;
	}

	protected static String setMessage(Exception e) {
		if (e instanceof ProjectCommonException) {
			return e.getMessage();
		} else {
			return "Something went wrong in server while processing the request";
		}
	}

	/**
	 * This method will create response parameter
	 *
	 * @param code
	 *            ResponseCode
	 * @return ResponseParams
	 */
	public static ResponseParams createResponseParamObj(ResponseCode code) {
		ResponseParams params = new ResponseParams();
		if (code.getResponseCode() != 200) {
			params.setErr(code.getErrorCode());
			params.setErrmsg(code.getErrorMessage());
		}
		params.setMsgid(ExecutionContext.getRequestId());
		params.setStatus(ResponseCode.getHeaderResponseCode(code.getResponseCode()).name());
		return params;
	}

	public Result createCommonResponse(String path, Response response) {
		return Results.ok(
		        Json.toJson(BaseController.createSuccessResponse(path, response)));
	}
	
	/**
	 * This method will create data for success response.
	 *
	 * @param request
	 *            play.mvc.Http.Request
	 * @param response
	 *            Response
	 * @return Response
	 */
	public static Response createSuccessResponse(String path, Response response) {

		if (StringUtils.isNotBlank(path)) {
			response.setVer(getApiVersion(path));
		} else {
			response.setVer("");
		}
		response.setId(getApiResponseId());
		response.setTs(ProjectUtil.getFormattedDate());
		ResponseCode code = ResponseCode.getResponse(ResponseCode.success.getErrorCode());
		code.setResponseCode(ResponseCode.OK.getResponseCode());
		response.setParams(createResponseParamObj(code));
		return response;
	}

	/**
	 * This method will provide api version.
	 *
	 * @param request
	 *            String
	 * @return String
	 */
	public static String getApiVersion(String path) {

		return path.split("[/]")[1];
	}

	/**
	 * 
	 * @param request
	 *            play.mvc.Http.Request
	 * @return String
	 */
	private static String getApiResponseId() {
		return "api.telemetry";
	}

}
