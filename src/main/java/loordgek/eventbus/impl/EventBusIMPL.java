package loordgek.eventbus.impl;

import loordgek.eventbus.api.Event;
import loordgek.eventbus.api.EventBus;
import loordgek.eventbus.api.EventDummy;
import loordgek.eventbus.api.SubscribeEvent;
import loordgek.eventbus.util.AnnotationHelper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;


public class EventBusIMPL implements EventBus, EventExceptionHandler {
    private static final Map<Class<?>, Map<Integer, Optional<EventListenerSuplier>>> cache = new IdentityHashMap<>();
    private final Map<Class<? extends Event>, EventList> eventListenerMap = new IdentityHashMap<>();
    private final EventExceptionHandler handler;

    public EventBusIMPL(EventExceptionHandler handler) {
        this.handler = handler;
    }

    public EventBusIMPL() {
        this.handler = this;
    }

    @Override
    public int getListenersForEvent(Class<Event> eventClass) {
        return eventListenerMap.containsKey(eventClass) ? eventListenerMap.get(eventClass).size() : 0;
    }

    @Override
    public <T extends Event> T post(T event) {
        if (eventListenerMap.containsKey(event.getClass())) {
            eventListenerMap.get(event.getClass()).postEvent(event, handler);
        }
        return event;
    }

    @Override
    public void register(Object target) {
        boolean isStatic = target.getClass() == Class.class;
        Class<?> eventListenerClass = (isStatic ? (Class<?>) target : target.getClass());

        Method[] methods = eventListenerClass.getMethods();

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            SubscribeEvent subscribeEvent = AnnotationHelper.getAnnotation(method, SubscribeEvent.class);
            if (subscribeEvent != null) {
                Class<? extends Event> eventClass = getEventClass(method, subscribeEvent);
                Optional<EventListenerSuplier> eventListener = cache.computeIfAbsent(eventListenerClass, c -> new HashMap<>()).computeIfAbsent(i, integer -> {
                    if (subscribeEvent.deferred() && method.getAnnotation(SubscribeEvent.class) == null ) {
                        return Optional.empty();
                    } else {
                        return Optional.of(EventListener.createListener(method, subscribeEvent, isStatic, eventListenerClass, eventClass));
                    }
                });
                eventListener.ifPresent(t -> eventListenerMap.computeIfAbsent(eventClass, c -> new EventList()).addEvent(t.get(target)));
            }
        }
    }

    @Override
    public void unRegister(Object target) {
        boolean isStatic = target.getClass() == Class.class;
        Class<?> eventListenerClass = (isStatic ? (Class<?>) target : target.getClass());

        Method[] methods = eventListenerClass.getMethods();
        for (Method method : methods) {
            SubscribeEvent subscribeEvent = AnnotationHelper.getAnnotation(method, SubscribeEvent.class);
            if (subscribeEvent != null){
                Class<? extends Event> eventClass = getEventClass(method, subscribeEvent);
                EventList list = eventListenerMap.get(eventClass);
                if (list != null){
                    list.removeEvent(target);
                    if (list.isEmpty())
                        eventListenerMap.remove(eventClass);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Event> getEventClass(Method method, SubscribeEvent subscribeEvent) {
        Class<? extends Event> eventClass = null;
        if (subscribeEvent.eventClass() == EventDummy.class) {
            if (method.getParameterCount() == 1 && Event.class.isAssignableFrom(method.getParameterTypes()[0]))
                eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
        } else eventClass = subscribeEvent.eventClass();

        return eventClass;
    }

    @Override
    public void handleException(Exception e, IEventListener listener) {
        System.out.println(e.getMessage());
    }
}
