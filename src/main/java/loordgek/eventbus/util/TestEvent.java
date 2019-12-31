package loordgek.eventbus.util;

import loordgek.eventbus.api.Event;
import loordgek.eventbus.api.Unpack;

public class TestEvent implements Event {
    public Object instance;
    private int number;

    public TestEvent(int number) {
        this.number = number;
    }

    public void setNumber(int number) {
         this.number = number;
    }

    @Unpack("number")
    public int getNumber() {
        return number;
    }
}
