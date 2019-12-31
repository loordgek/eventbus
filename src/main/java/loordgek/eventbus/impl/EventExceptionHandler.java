package loordgek.eventbus.impl;

public interface EventExceptionHandler {

    void handleException(Exception e, IEventListener listener);
}
