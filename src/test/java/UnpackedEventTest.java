import loordgek.eventbus.api.Event;
import loordgek.eventbus.api.SubscribeEvent;
import loordgek.eventbus.api.Unpack;
import loordgek.eventbus.impl.EventBusIMPL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UnpackedEventTest {

    private final EventBusIMPL eventBus = new EventBusIMPL();

    public UnpackedEventTest() {
        eventBus.register(UnpackedEventTest.Listener.class);
    }

    @Test
    public void testEvent(){
        eventBus.post(new UnpackedEventTest.TestEvent(12));
    }

    public static class TestEvent implements Event {
        private final int number;

        private TestEvent(int number) {
            this.number = number;
        }

        @Unpack("number")
        public int getNumber() {
            return number;
        }
    }

    public static class Listener{

        @SubscribeEvent(eventClass = TestEvent.class)
        public static void unPackedEvent(int number){
            Assertions.assertEquals(12, number, "static event");
        }
    }
}
