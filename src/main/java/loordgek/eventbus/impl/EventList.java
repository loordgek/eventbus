package loordgek.eventbus.impl;

import loordgek.eventbus.api.Event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventList {
    private final List<IEventListener> events = new ArrayList<>();

    public void addEvent(IEventListener listener){
        events.add(listener);
        events.sort(Comparator.comparingInt(i -> i.getPriority().ordinal()));
    }

    public void removeEvent(Object o){
        events.removeIf(i -> i.owner() == o);
    }

    public void postEvent(Event event, EventExceptionHandler handler){
        int index = 0;
        try {

            for (; index < events.size(); index++) {
                events.get(index).invoke(event);
            }

        } catch (Exception e) {
            handler.handleException(e, events.get(index));
        }
    }

    public boolean isEmpty(){
        return events.isEmpty();
    }

    public int size() {
        return events.size();
    }
}
