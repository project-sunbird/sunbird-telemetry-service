package actors;

import static org.junit.Assert.assertTrue;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import controllers.TelemetryControllerTest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.telemetry.dispatcher.KafkaTelemetryDispatcherActor;
import util.Constant;

/**
 * This class will have test cases for kafka telemetry dispatch actor test.
 *
 * @author Manzarul
 */
public class KafkaTelemetryDispatcherActorTest {
  private static final Props props = Props.create(KafkaTelemetryDispatcherActor.class);
  private static ActorSystem system;
  String body =
      "{\"request\":{\"id\":\"sunbird.telemetry\",\"ver\":\"3.0\",\"ets\":1521629184223,\"events\":[{\"eid\":\"LOG\",\"ets\":1521629109520,\"ver\":\"3.0\",\"mid\":\"LOG:a473972af5b9235e68a7b641a5bd1188\",\"actor\":{\"id\":\"anonymous\",\"type\":\"User\"},\"context\":{\"channel\":\"sunbird\",\"pdata\":{\"id\":\"sunbird.portal\",\"ver\":\"1.0\",\"pid\":\"sunbird-portal\"},\"env\":\"home\",\"sid\":\"\",\"did\":\"6541fde589d55fdcaca385c75706960a\",\"cdata\":[],\"rollup\":{}},\"object\":{},\"tags\":[null],\"edata\":{\"type\":\"api_call\",\"level\":\"INFO\",\"message\":\"Content read\",\"pageid\":\"\"}}]}}";

  @BeforeClass
  public static void setUp() {
    system = ActorSystem.create("system");
  }

  @Test
  public void invalidOperationNameForKafkaTest() {
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);
    Request reqObj = new Request();
    reqObj.setOperation("INVALID_OPERATION");
    subject.tell(reqObj, probe.getRef());
    ProjectCommonException exc = probe.expectMsgClass(ProjectCommonException.class);
    assertTrue(exc.getCode().equals(ResponseCode.invalidOperationName.getErrorCode()));
    assertTrue(exc.getResponseCode() == ResponseCode.CLIENT_ERROR.getResponseCode());
  }

  @Test
  public void jsonRequestBodyTest() {
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);
    Request reqObj = new Request();
    reqObj.setOperation(Constant.DISPATCH_TELEMETRY_TO_KAFKA);
    reqObj.put(Constant.BODY, body);
    subject.tell(reqObj, probe.getRef());
    Response respone = probe.expectMsgClass(Response.class);
    Assert.assertEquals(JsonKey.SUCCESS, respone.get(JsonKey.RESPONSE));
  }

  @Test
  public void jsonRequestBodyWithEmptyText() {
    Map<String, String[]> headers = new HashMap<String, String[]>();
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);
    Request reqObj = new Request();
    reqObj.setOperation(Constant.DISPATCH_TELEMETRY_TO_KAFKA);
    // passing empty text in body
    reqObj.put(Constant.BODY, "");
    reqObj.put(Constant.HEADERS, headers);
    subject.tell(reqObj, probe.getRef());
    ProjectCommonException exc = probe.expectMsgClass(ProjectCommonException.class);
    assertTrue(exc.getCode().equals(ResponseCode.invalidRequestData.getErrorCode()));
    assertTrue(exc.getResponseCode() == ResponseCode.CLIENT_ERROR.getResponseCode());
  }

  @Test
  public void validateGzipDataSending() {
    File file = new File("test/resources/telemetry.few.gz");
    byte[] data = TelemetryControllerTest.readByteArrayFromZp(file);
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);
    Request reqObj = new Request();
    reqObj.setOperation(Constant.DISPATCH_TELEMETRY_TO_KAFKA);
    reqObj.put(Constant.BODY, data);
    subject.tell(reqObj, probe.getRef());
    Response respone = probe.expectMsgClass(Response.class);
    Assert.assertEquals(JsonKey.SUCCESS, respone.get(JsonKey.RESPONSE));
  }

  @Test
  public void validateWithIncorrectBody() {
    Map<String, Object> map = new HashMap<>();
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);
    Request reqObj = new Request();
    reqObj.setOperation(Constant.DISPATCH_TELEMETRY_TO_KAFKA);
    reqObj.put(Constant.BODY, map);
    subject.tell(reqObj, probe.getRef());
    ProjectCommonException exc = probe.expectMsgClass(ProjectCommonException.class);
    assertTrue(exc.getCode().equals(ResponseCode.invalidRequestData.getErrorCode()));
    assertTrue(exc.getResponseCode() == ResponseCode.CLIENT_ERROR.getResponseCode());
  }
}
