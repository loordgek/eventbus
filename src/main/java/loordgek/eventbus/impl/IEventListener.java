package loordgek.eventbus.impl;

import loordgek.eventbus.api.Event;
import loordgek.eventbus.api.EventPriority;

public interface IEventListener {

    void invoke(Event event);

    default EventPriority getPriority() {
        return EventPriority.NORMAL;
    }

    Object owner();
}
