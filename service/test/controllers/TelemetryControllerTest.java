package controllers;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Files;

import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;

/**
 * 
 * @author Mahesh Kumar Gangula
 *
 */

public class TelemetryControllerTest {
	
	private static FakeApplication app;
	
	@BeforeClass
	public static void before() {
		app = Helpers.fakeApplication();
		Helpers.start(app);
	}
	
	
	@Test
	public void testSendTelemetryUsingTextBodyInvalidHeaders() {
		String body = "{\"request\":{\"id\":\"sunbird.telemetry\",\"ver\":\"3.0\",\"ets\":1521629184223,\"events\":[{\"eid\":\"LOG\",\"ets\":1521629109520,\"ver\":\"3.0\",\"mid\":\"LOG:a473972af5b9235e68a7b641a5bd1188\",\"actor\":{\"id\":\"anonymous\",\"type\":\"User\"},\"context\":{\"channel\":\"sunbird\",\"pdata\":{\"id\":\"sunbird.portal\",\"ver\":\"1.0\",\"pid\":\"sunbird-portal\"},\"env\":\"home\",\"sid\":\"\",\"did\":\"6541fde589d55fdcaca385c75706960a\",\"cdata\":[],\"rollup\":{}},\"object\":{},\"tags\":[null],\"edata\":{\"type\":\"api_call\",\"level\":\"INFO\",\"message\":\"Content read\",\"pageid\":\"\"}}]}}";
		RequestBuilder request = new RequestBuilder().bodyText(body).uri("/v1/telemetry").method(Helpers.POST);
		Map<String, String[]> headers = new HashMap<String, String[]>();
		request.headers(headers);
		Result result = Helpers.route(request);
		String response = Helpers.contentAsString(result);
		assertTrue(response.contains("INVALID_REQUESTED_DATA"));
		assertTrue(response.contains("Please provide valid headers"));
		assertEquals(400, result.status());
	}
	
	/**
	 * Send telemetry as body with proper headers and data - Should return successful response.
	 */
//	@Test
	public void testSendTelemetryUsingTextBody() {
		String body = "{\"request\":{\"id\":\"sunbird.telemetry\",\"ver\":\"3.0\",\"ets\":1521629184223,\"events\":[{\"eid\":\"LOG\",\"ets\":1521629109520,\"ver\":\"3.0\",\"mid\":\"LOG:a473972af5b9235e68a7b641a5bd1188\",\"actor\":{\"id\":\"anonymous\",\"type\":\"User\"},\"context\":{\"channel\":\"sunbird\",\"pdata\":{\"id\":\"sunbird.portal\",\"ver\":\"1.0\",\"pid\":\"sunbird-portal\"},\"env\":\"home\",\"sid\":\"\",\"did\":\"6541fde589d55fdcaca385c75706960a\",\"cdata\":[],\"rollup\":{}},\"object\":{},\"tags\":[],\"edata\":{\"type\":\"api_call\",\"level\":\"INFO\",\"message\":\"Content read\",\"pageid\":\"\"}}]}}";
		RequestBuilder request = new RequestBuilder().bodyText(body).uri("/v1/telemetry").method(Helpers.POST);
		Map<String, String[]> headers = new HashMap<String, String[]>();
		headers.put("Content-Type", new String[] {"application/json"});
		request.headers(headers);
		Result result = Helpers.route(request);
		System.out.println("Content: "+ Helpers.contentAsString(result));
		assertEquals(200, result.status());
	}
	
	
	
//	@Test
	public void testSendTelemetryUsingGZip() throws Exception {
		File file = app.getFile("test/resources/telemetry.few.gz");
		byte[] data = Files.toByteArray(file);
		Map<String, String[]> headers = new HashMap<String, String[]>();
		headers.put("Content-Type", new String[] {"application/zip"});
		headers.put("accept-encoding", new String[] {"gzip"});
		RequestBuilder request = new RequestBuilder().headers(headers).bodyRaw(data).uri("/v1/telemetry").method(Helpers.POST);
		Result result = Helpers.route(request);
		System.out.println("Content: "+ Helpers.contentAsString(result));
		System.out.println("Status: "+ result.status());
//		assertEquals(200, result.status());
	}

}
