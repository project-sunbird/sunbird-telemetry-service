package org.sunbird.actor

import akka.actor.ActorRef

object AppActors {

  var actors: Map[String, ActorRef]  = Map();

  def setActors(actors: Map[String, ActorRef]) = {
    this.actors = actors;
  }

  def getActor(name: String) : ActorRef = {
    actors.get(name).get;
  }
}
