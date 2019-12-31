package loordgek.eventbus.api;

public interface Event {

    interface Cancelable extends Event {

        @Unpack("isCanceled")
        boolean isCanceled();

        void setCanceled(boolean cancel);
    }
}
