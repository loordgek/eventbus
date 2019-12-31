package loordgek.eventbus.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(value = RUNTIME)
@Target(value = METHOD)
public @interface SubscribeEvent {
    Class<? extends Event> eventClass() default EventDummy.class;

    EventPriority priority() default EventPriority.NORMAL;

    boolean receiveCanceled() default false;

    boolean deferred() default false;

    String methodReturnName() default "";
}
