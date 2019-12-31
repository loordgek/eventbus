import loordgek.eventbus.api.Event;
import loordgek.eventbus.api.EventBus;
import loordgek.eventbus.api.SubscribeEvent;
import loordgek.eventbus.impl.EventBusIMPL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CancelableEventTest {

    EventBusIMPL eventBus = new EventBusIMPL();

    @Test
    public void test(){
        eventBus.register(CancelableEventTest.class);
        CancelableEvent event = eventBus.post(new CancelableEvent());
        Assertions.assertEquals(5, event.number);
    }

    @SubscribeEvent
    public static void eventHandler(CancelableEvent event){
        event.number = 10;
    }

    public static class CancelableEvent implements Event.Cancelable{
        private int number = 5;

        @Override
        public boolean isCanceled() {
            return true;
        }

        @Override
        public void setCanceled(boolean cancel) {

        }
    }
}
