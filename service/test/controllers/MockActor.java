package controllers;

import akka.actor.ActorRef;
import akka.actor.UntypedAbstractActor;
import org.sunbird.common.models.response.Response;

/**
 * Mock actor class. All the actor call for testing will be handle by this class.
 *
 * @author Manzarul
 */
public class MockActor extends UntypedAbstractActor {

  @Override
  public void onReceive(Object message) throws Throwable {
    Response response = new Response();
    sender().tell(response, ActorRef.noSender());
  }
}
