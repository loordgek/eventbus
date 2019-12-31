import loordgek.eventbus.api.Event;
import loordgek.eventbus.api.SubscribeEvent;
import loordgek.eventbus.api.Unpack;
import loordgek.eventbus.impl.EventBusIMPL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.objectweb.asm.Type;

public class EventTest {

    private final EventBusIMPL eventBus = new EventBusIMPL();

    public EventTest() {
        eventBus.register(Listener.class);
    }

    @Test
    public void testEvent(){
        eventBus.post(new TestEvent(12));
    }

    public static class TestEvent implements Event {
        private final int number;

        private TestEvent(int number) {
            this.number = number;
            System.out.println(Type.getInternalName(TestEvent.class));
        }

        public int getNumber() {
            return number;
        }
    }

    public static class Listener{

        @SubscribeEvent
        public static void packedEvent(EventTest.TestEvent event){
            Assertions.assertEquals(12, event.getNumber(), "static event");
        }
    }
}
