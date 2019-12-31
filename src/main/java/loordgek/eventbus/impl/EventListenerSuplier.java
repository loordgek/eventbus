package loordgek.eventbus.impl;

public interface EventListenerSuplier {

    IEventListener get(Object target);
}
