package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.controllers.BaseController;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;
import util.Constant;
import util.Message;

/**
 * This class will contains test cases for telemetry controller. this will mock the actor call.
 *
 * @author Mahesh Kumar Gangula
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(PowerMockRunner.class)
public class TelemetryControllerTest {

  private static FakeApplication app;
  private static ActorSystem system;
  private static final Props props = Props.create(MockActor.class);

  @BeforeClass
  public static void before() {
    app = Helpers.fakeApplication();
    Helpers.start(app);
    system = ActorSystem.create("system");
    ActorRef subject = system.actorOf(props);
    BaseController.setActorRef(subject);
  }

  @Test
  public void testSendTelemetryUsingTextBodyInvalidHeaders() {
    String body =
        "{\"request\":{\"id\":\"sunbird.telemetry\",\"ver\":\"3.0\",\"ets\":1521629184223,\"events\":[{\"eid\":\"LOG\",\"ets\":1521629109520,\"ver\":\"3.0\",\"mid\":\"LOG:a473972af5b9235e68a7b641a5bd1188\",\"actor\":{\"id\":\"anonymous\",\"type\":\"User\"},\"context\":{\"channel\":\"sunbird\",\"pdata\":{\"id\":\"sunbird.portal\",\"ver\":\"1.0\",\"pid\":\"sunbird-portal\"},\"env\":\"home\",\"sid\":\"\",\"did\":\"6541fde589d55fdcaca385c75706960a\",\"cdata\":[],\"rollup\":{}},\"object\":{},\"tags\":[null],\"edata\":{\"type\":\"api_call\",\"level\":\"INFO\",\"message\":\"Content read\",\"pageid\":\"\"}}]}}";
    RequestBuilder request =
        new RequestBuilder().bodyText(body).uri("/v1/telemetry").method(Helpers.POST);
    Map<String, String[]> headers = new HashMap<String, String[]>();
    request.headers(headers);
    Result result = Helpers.route(request);
    String response = Helpers.contentAsString(result);
    assertTrue(response.contains(ResponseCode.invalidRequestData.getErrorCode()));
    assertTrue(response.contains(Message.INVALID_HEADER_MSG_ERROR));
    assertEquals(400, result.status());
  }

  /** Send telemetry as body with proper headers and data - Should return successful response. */
  @Test
  public void testSendTelemetryUsingTextBody() {
    String body =
        "{\"request\":{\"id\":\"sunbird.telemetry\",\"ver\":\"3.0\",\"ets\":1521629184223,\"events\":[{\"eid\":\"LOG\",\"ets\":1521629109520,\"ver\":\"3.0\",\"mid\":\"LOG:a473972af5b9235e68a7b641a5bd1188\",\"actor\":{\"id\":\"anonymous\",\"type\":\"User\"},\"context\":{\"channel\":\"sunbird\",\"pdata\":{\"id\":\"sunbird.portal\",\"ver\":\"1.0\",\"pid\":\"sunbird-portal\"},\"env\":\"home\",\"sid\":\"\",\"did\":\"6541fde589d55fdcaca385c75706960a\",\"cdata\":[],\"rollup\":{}},\"object\":{},\"tags\":[],\"edata\":{\"type\":\"api_call\",\"level\":\"INFO\",\"message\":\"Content read\",\"pageid\":\"\"}}]}}";
    RequestBuilder request =
        new RequestBuilder().bodyText(body).uri("/v1/telemetry").method(Helpers.POST);
    Map<String, String[]> headers = new HashMap<String, String[]>();
    headers.put(Constant.CONTENT_TYPE, new String[] {Constant.APPLICATION_JSON});
    request.headers(headers);
    Result result = Helpers.route(request);
    assertEquals(200, result.status());
  }

  @Test
  public void testSendTelemetryUsingGZip() throws Exception {
    File file = app.getFile("test/resources/telemetry.few.gz");
    byte[] data = readByteArrayFromZp(file);
    Map<String, String[]> headers = new HashMap<String, String[]>();
    headers.put(Constant.CONTENT_TYPE, new String[] {Constant.APPLICATION_ZIP});
    headers.put(Constant.ACCEPT_ENCODING, new String[] {"gzip"});
    RequestBuilder request =
        new RequestBuilder()
            .headers(headers)
            .bodyRaw(data)
            .uri("/v1/telemetry")
            .method(Helpers.POST);
    Result result = Helpers.route(request);
    assertEquals(200, result.status());
  }

  @Test
  public void testSendTelemetryWithOutEventsKey() {
    String body =
        "{\"request\":{\"id\":\"sunbird.telemetry\",\"ver\":\"3.0\",\"ets\":1521629184223}}";
    RequestBuilder request =
        new RequestBuilder().bodyText(body).uri("/v1/telemetry").method(Helpers.POST);
    Map<String, String[]> headers = new HashMap<String, String[]>();
    headers.put(Constant.CONTENT_TYPE, new String[] {Constant.APPLICATION_JSON});
    request.headers(headers);
    Result result = Helpers.route(request);
    assertEquals(400, result.status());
  }

  @Test
  public void testSendTelemetryWithEmptyBody() {
    String body = "{}";
    RequestBuilder request =
        new RequestBuilder().bodyText(body).uri("/v1/telemetry").method(Helpers.POST);
    Map<String, String[]> headers = new HashMap<String, String[]>();
    headers.put(Constant.CONTENT_TYPE, new String[] {Constant.APPLICATION_JSON});
    request.headers(headers);
    Result result = Helpers.route(request);
    assertEquals(400, result.status());
  }

  @Test
  public void testSendTelemetryUsingEmptyEventBody() {
    String body =
        "{\"request\":{\"id\":\"sunbird.telemetry\",\"ver\":\"3.0\",\"ets\":1521629184223,\"events\":[]}}";
    RequestBuilder request =
        new RequestBuilder().bodyText(body).uri("/v1/telemetry").method(Helpers.POST);
    Map<String, String[]> headers = new HashMap<String, String[]>();
    headers.put(Constant.CONTENT_TYPE, new String[] {Constant.APPLICATION_JSON});
    request.headers(headers);
    Result result = Helpers.route(request);
    assertEquals(400, result.status());
  }

  @Test
  public void testSendTelemetryWithOutGZip() throws Exception {
    byte[] data = new byte[0];
    Map<String, String[]> headers = new HashMap<String, String[]>();
    headers.put(Constant.CONTENT_TYPE, new String[] {Constant.APPLICATION_ZIP});
    headers.put(Constant.ACCEPT_ENCODING, new String[] {"gzip"});
    RequestBuilder request =
        new RequestBuilder()
            .headers(headers)
            .bodyRaw(data)
            .uri("/v1/telemetry")
            .method(Helpers.POST);
    Result result = Helpers.route(request);
    assertEquals(400, result.status());
  }

  /**
   * Convert gz file to byte array.
   *
   * @param file
   * @return
   */
  public static byte[] readByteArrayFromZp(File file) {
    byte[] fileData = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPInputStream in = null;
    try {
      in = new GZIPInputStream(new FileInputStream(file));
      int bufsize = 1024;
      byte[] buf = new byte[bufsize];
      int readbytes = 0;
      readbytes = in.read(buf);
      while (readbytes != -1) {
        baos.write(buf, 0, readbytes);
        readbytes = in.read(buf);
      }
      baos.flush();
      return baos.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        in.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return fileData;
  }
}
