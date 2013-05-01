package org.mikebrock.livecode.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.StringState;
import org.jboss.errai.otec.server.OTServerEngine;
import org.jboss.errai.otec.server.ServerOTBusService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped @Service
public class MyOTServerWiringBeanThing implements MessageCallback {
  @Inject MessageBus messageBus;

  @PostConstruct
  public void setup() {
    final OTEngine engineWithMultiplePeers = OTServerEngine.createEngineWithMultiplePeers();
    engineWithMultiplePeers.getEntityStateSpace().addEntity(StringState.of("The quick brown fox ..."));
    engineWithMultiplePeers.start();

    ServerOTBusService.startOTService(messageBus, engineWithMultiplePeers);
  }


  // this is a hack because CDI sucks.
  @Override
  public void callback(Message message) {
  }
}
