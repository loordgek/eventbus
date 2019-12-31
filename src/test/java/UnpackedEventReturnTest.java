import loordgek.eventbus.api.Event;
import loordgek.eventbus.api.SubscribeEvent;
import loordgek.eventbus.api.Unpack;
import loordgek.eventbus.impl.EventBusIMPL;
import loordgek.eventbus.util.TestEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UnpackedEventReturnTest {
    private final EventBusIMPL eventBus = new EventBusIMPL();

    public UnpackedEventReturnTest() {
        eventBus.register(new Listener());
    }

    @Test
    public void test(){
        Assertions.assertEquals(12, eventBus.post(new TestEvent(11)).getNumber());
    }

    public static class Listener{

        @SubscribeEvent(eventClass = TestEvent.class)
        public int unPackedEvent(int number){
            return number + 1;
        }
    }
}
