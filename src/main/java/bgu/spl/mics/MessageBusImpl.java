package bgu.spl.mics;

import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

    private ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentLinkedQueue<MicroService>> eventSubscribers;
    private ConcurrentHashMap<Class<? extends Broadcast>, Vector<MicroService>> broadcastSubscribers;
    private ConcurrentHashMap<MicroService, ConcurrentLinkedQueue<Message>> microServicesQueues;
    private ConcurrentHashMap<Event<?>, Future> results;

    private static class MessageBusImplHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }

    private MessageBusImpl() {
        this.eventSubscribers = new ConcurrentHashMap<>();
        this.broadcastSubscribers = new ConcurrentHashMap<>();
        this.microServicesQueues = new ConcurrentHashMap<>();
        this.results = new ConcurrentHashMap<>();

    }

    public static MessageBusImpl getInstance() {
        return MessageBusImplHolder.instance;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {

        synchronized (eventSubscribers) {
            if (!eventSubscribers.containsKey(type)) { //if the event is not registered, create a new queue for it
                eventSubscribers.put(type, new ConcurrentLinkedQueue<>());
            }
            eventSubscribers.get(type).add(m); //adds 'm' to the event round-robin queue
        }

    }

    @Override
    public synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        synchronized (broadcastSubscribers) {
            if (!broadcastSubscribers.containsKey(type)) { //if the broadcast is not registered, create a new vector for it
                broadcastSubscribers.put(type, new Vector<>());
            }
            broadcastSubscribers.get(type).add(m); //adds 'm' to the broadcast round-robin queue
        }
    }

    @Override @SuppressWarnings("unchecked")
    public <T> void complete(Event<T> e, T result) {
        if (e != null && results.get(e) != null)
            results.get(e).resolve(result);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        synchronized (broadcastSubscribers.get(b.getClass())) {
            if (broadcastSubscribers.containsKey(b.getClass())) {
                for (int i = 0; i < broadcastSubscribers.get(b.getClass()).size(); i++) { //adds the broadcast to its subscribers vectors
                    microServicesQueues.get(broadcastSubscribers.get(b.getClass()).get(i)).add(b);


                    synchronized (microServicesQueues.get(broadcastSubscribers.get(b.getClass()).get(i))) {
                        microServicesQueues.get(broadcastSubscribers.get(b.getClass()).get(i)).notifyAll();
                    }
                }
            }
        }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {

        Future<T> future = null;

        synchronized (eventSubscribers.get(e.getClass())) {

            if (!eventSubscribers.containsKey(e.getClass()) || eventSubscribers.get(e.getClass()).isEmpty()) {
                return null;
            }
            future = new Future<T>();
            results.put(e, future);
            MicroService mic = eventSubscribers.get(e.getClass()).poll();
            microServicesQueues.get(mic).add(e);
            eventSubscribers.get(e.getClass()).add(mic);
            synchronized (microServicesQueues.get(mic)) {
                microServicesQueues.get(mic).notifyAll();
            }
        }
        return future;
    }

    @Override
    public synchronized void register(MicroService m) {

        if (microServicesQueues.get(m) == null) { //register if the micro service is not registered
            microServicesQueues.put(m, new ConcurrentLinkedQueue<>());
        }
    }

    @Override @SuppressWarnings("unchecked")
    public void unregister(MicroService m) {
        eventSubscribers.forEachValue(1, (value) -> { //removes 'm' from the round-robin queue of each event
            synchronized (value) {
                value.remove(m);
            }
        });
        broadcastSubscribers.forEachValue(1, (value) -> { //removes 'm' from the round-robin queue of each broadcast
            synchronized (value) {
                value.remove(m);
            }
        });

        synchronized (microServicesQueues.get(m)) {
            microServicesQueues.get(m).forEach(event -> {
                if(event.getClass() != TickBroadcast.class & event.getClass() != TerminateBroadcast.class) //if there are events left to resolve, resolves them
                complete((Event) event, null);
            });

        }
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        if (!microServicesQueues.containsKey(m))
            throw new IllegalStateException();
        synchronized (microServicesQueues.get(m)) {
            try {
                while (microServicesQueues.get(m).isEmpty())
                    microServicesQueues.get(m).wait();
            } catch (InterruptedException ignored) {
            }
            Message first = microServicesQueues.get(m).poll();
            return first;
        }
    }
}
