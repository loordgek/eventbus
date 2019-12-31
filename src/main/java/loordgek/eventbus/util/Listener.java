package loordgek.eventbus.util;

import loordgek.eventbus.api.SubscribeEvent;

public class Listener {
    @SubscribeEvent(eventClass = TestEvent.class)
    public int unPackedEvent(int number){
        return number + 1;
    }
}
