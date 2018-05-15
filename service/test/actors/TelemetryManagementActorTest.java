package actors;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import controllers.TelemetryController;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.telemetry.actor.TelemetryManagerActor;
import org.sunbird.telemetry.dispatcher.EkstepTelemetryDispatcher;

/** @author Manzarul */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@PrepareForTest(EkstepTelemetryDispatcher.class)
public class TelemetryManagementActorTest {
  private static final Props props = Props.create(TelemetryManagerActor.class);
  private static ActorSystem system;
  String body =
      "{\"request\":{\"id\":\"sunbird.telemetry\",\"ver\":\"3.0\",\"ets\":1521629184223,\"events\":[{\"eid\":\"LOG\",\"ets\":1521629109520,\"ver\":\"3.0\",\"mid\":\"LOG:a473972af5b9235e68a7b641a5bd1188\",\"actor\":{\"id\":\"anonymous\",\"type\":\"User\"},\"context\":{\"channel\":\"sunbird\",\"pdata\":{\"id\":\"sunbird.portal\",\"ver\":\"1.0\",\"pid\":\"sunbird-portal\"},\"env\":\"home\",\"sid\":\"\",\"did\":\"6541fde589d55fdcaca385c75706960a\",\"cdata\":[],\"rollup\":{}},\"object\":{},\"tags\":[null],\"edata\":{\"type\":\"api_call\",\"level\":\"INFO\",\"message\":\"Content read\",\"pageid\":\"\"}}]}}";

  @BeforeClass
  public static void setUp() {
    system = ActorSystem.create("system");
  }

  // @Test
  public void invalidOperationNameTest() {
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);
    Request reqObj = new Request();
    reqObj.setOperation("INVALID_OPERATION");
    subject.tell(reqObj, probe.getRef());
    ProjectCommonException exc = probe.expectMsgClass(ProjectCommonException.class);
    assertTrue(exc.getCode().equals(ResponseCode.invalidRequestData.getErrorCode()));
    assertTrue(exc.getResponseCode() == ResponseCode.CLIENT_ERROR.getResponseCode());
  }

  @Test
  public void jsonRequestBodyTest() {
    PowerMockito.mockStatic(EkstepTelemetryDispatcher.class);
    try {
      when(EkstepTelemetryDispatcher.dispatch(new HashMap<String, String[]>(), ""))
          .thenReturn(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);
    Request reqObj = new Request();
    reqObj.setOperation("dispatchtelemetry");
    reqObj.put(TelemetryController.BODY, body);
    subject.tell(reqObj, probe.getRef());
    Response respone = probe.expectMsgClass(Response.class);
    System.out.println("response value===========" + respone.get(JsonKey.RESPONSE));
    Assert.assertEquals(JsonKey.SUCCESS, respone.get(JsonKey.RESPONSE));
  }
}
