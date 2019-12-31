package loordgek.eventbus.api;

public interface EventBus {

    int getListenersForEvent(Class<Event> eventClass);

    <T extends Event> T post(T event);

    void register(Object target);

    void unRegister(Object target);
}
