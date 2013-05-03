package org.mikebrock.livecode.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
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
import org.jboss.errai.otec.client.mutation.MutationType;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@EntryPoint
@Templated("#root")
public class App extends Composite {
  @Inject @DataField private TextArea myField;
  @Inject @DataField private Label reflector;
  @Inject @DataField private Button startAuto;

  @Inject MessageBus bus;

  final OTEngine engineWithSinglePeer = OTClientEngine.createEngineWithSinglePeer();
  OTEntity entity;

  final String typeText = " Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Typi non habent claritatem insitam; est usus legentis in iis qui facit eorum claritatem. Investigationes demonstraverunt lectores legere me lius quod ii legunt saepius. Claritas est etiam processus dynamicus, qui sequitur mutationem consuetudium lectorum. Mirum est notare quam littera gothica, quam nunc putamus parum claram, anteposuerit litterarum formas humanitatis per seacula quarta decima et quinta decima. Eodem modo typi, qui nunc nobis videntur parum clari, fiant sollemnes in futurum. ";
  int autoTextCursor = 0;


  final Timer timer = new Timer() {
    @Override
    public void run() {
      final StringBuilder builder = new StringBuilder();
      int i1 = Random.nextInt(12);
      if (i1 != 0 && i1 < 6) {
        for (int i = 0; i < i1; i++) {
          if (autoTextCursor == typeText.length()) {
            autoTextCursor = 0;
          }

          builder.append(typeText.charAt(autoTextCursor++));
        }

        final OTOperation build = engineWithSinglePeer.getOperationsFactory().createOperation(entity)
            .add(MutationType.Insert, myField.getCursorPos(), builder.toString())
            .build();

        engineWithSinglePeer.notifyOperation(build);
        myField.setCursorPos(myField.getCursorPos() + builder.length());
      }
    }
  };

  private boolean running = false;

  @PostConstruct
  private void setupOtecTest() {
    MessageBuilder.createMessage()
        .toSubject("MyOTServerWiringBeanThing")
        .signalling()
        .noErrorHandling().sendNowWith(bus);


    ClientOTBusService.startOTService(bus, engineWithSinglePeer);

    engineWithSinglePeer.registerPeer(new ClientOTPeerImpl(bus, engineWithSinglePeer));
    engineWithSinglePeer.start();
    engineWithSinglePeer.syncRemoteEntity("<ServerEngine>", 1, new EntitySyncCompletionCallback() {
      @Override
      public void syncComplete(final OTEntity entity) {
        Atomizer.syncWidgetWith(engineWithSinglePeer, entity, myField);

        App.this.entity = entity;

        new Timer() {
          @Override
          public void run() {
            reflector.setText(String.valueOf(entity.getState().get()));
          }
        }.scheduleRepeating(250);
      }
    });
  }

  @EventHandler("startAuto")
  private void onClickStart(ClickEvent event) {
    toggleAutoType();
  }

  private void toggleAutoType() {
    if (running) {
      timer.cancel();
      running = false;
    }
    else {
      timer.scheduleRepeating(250);
      running = true;
    }
  }
}
