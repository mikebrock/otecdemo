package org.mikebrock.livecode.client.local;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.otec.client.ClientOTBusService;
import org.jboss.errai.otec.client.ClientOTPeerImpl;
import org.jboss.errai.otec.client.EntitySyncCompletionCallback;
import org.jboss.errai.otec.client.OTClientEngine;
import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.atomizer.Atomizer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@EntryPoint
@Templated("#root")
public class App extends Composite {
  @Inject @DataField private TextArea myField;
  @Inject @DataField private Label reflector;

  @Inject MessageBus bus;

  @PostConstruct
  private void setupOtecTest() {
    MessageBuilder.createMessage()
        .toSubject("MyOTServerWiringBeanThing")
        .signalling()
        .noErrorHandling().sendNowWith(bus);

    final OTEngine engineWithSinglePeer = OTClientEngine.createEngineWithSinglePeer();

    ClientOTBusService.startOTService(bus, engineWithSinglePeer);

    engineWithSinglePeer.registerPeer(new ClientOTPeerImpl(bus, engineWithSinglePeer));
    engineWithSinglePeer.start();
    engineWithSinglePeer.syncRemoteEntity("<ServerEngine>", 1, new EntitySyncCompletionCallback() {
      @Override
      public void syncComplete(final OTEntity entity) {
        Atomizer.syncWidgetWith(engineWithSinglePeer, entity, myField);

        new Timer() {
          @Override
          public void run() {
            reflector.setText(String.valueOf(entity.getState().get()));
          }
        }.scheduleRepeating(250);
      }
    });


//
//    new Timer() {
//      @Override
//      public void run() {
//        final OTOperation build = engineWithSinglePeer.getOperationsFactory().createOperation(blah)
//            .add(MutationType.Insert, 0, ">>>")
//            .build();
//
//        engineWithSinglePeer.notifyOperation(build);
//      }
//    }.scheduleRepeating(2000);

//    new Timer() {
//      @Override
//      public void run() {
//        reflector.setText(String.valueOf(blah.getState().get()));
//      }
//    }.scheduleRepeating(250);
  }
}
