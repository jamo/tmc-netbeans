package fi.helsinki.cs.tmc.spyware;

import fi.helsinki.cs.tmc.model.ServerAccess;
import fi.helsinki.cs.tmc.utilities.CancellableCallable;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EventSenderTest {
    @Mock
    private SpywareSettings settings;
    @Mock
    private ServerAccess serverAccess;

    @Captor
    private volatile ArgumentCaptor<ArrayList<LoggableEvent>> sentEvents;

    private EventSender sender;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(settings.isSpywareEnabled()).thenReturn(true);
        when(settings.isDetailedSpywareEnabled()).thenReturn(true);

        when(serverAccess.getSendEventLogJob(sentEvents.capture())).thenReturn(new CancellableCallable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }

            @Override
            public boolean cancel() {
                return true;
            }
        });

        sender = new EventSender(settings, serverAccess);
    }

    @After
    public void tearDown() {
        sender.close();
    }

    private LoggableEvent mkEvent() {
        return new LoggableEvent("foo", "bar", "baz", new byte[0]);
    }

    private LoggableEvent ev1 = mkEvent();
    private LoggableEvent ev2 = mkEvent();
    private LoggableEvent ev3 = mkEvent();
    private LoggableEvent ev4 = mkEvent();
    private LoggableEvent ev5 = mkEvent();


    @Test
    public void testBufferingAndTakingFromTheBuffer() {
        sender.receiveEvent(ev1);
        sender.receiveEvent(ev2);
        sender.prependEvents(Arrays.asList(ev3, ev4, ev5));

        ArrayList<LoggableEvent> taken1 = sender.takeEvents(2);
        ArrayList<LoggableEvent> taken2 = sender.takeEvents(100);
        assertArrayEquals(new Object[] {ev3, ev4}, taken1.toArray());
        assertArrayEquals(new Object[] {ev5, ev1, ev2}, taken2.toArray());
    }


    @Test
    public void testSendingEvents() throws InterruptedException {
        sender.receiveEvent(ev1);
        sender.receiveEvent(ev2);
        sender.sendNow();

        sender.waitUntilCurrentSendingFinished(1000);
        assertArrayEquals(new Object[] { ev1, ev2 }, sentEvents.getValue().toArray());
    }

    @Test
    public void testDoesNotSendTooManyEventsAtATime() throws InterruptedException {
        int wayTooMuch = 10000;
        for (int i = 0; i < wayTooMuch; ++i) {
            sender.receiveEvent(ev1);
        }
        sender.sendNow();

        sender.waitUntilCurrentSendingFinished(1000);

        assertTrue(sentEvents.getAllValues().size() > 1);

        int sum = 0;
        for (ArrayList<LoggableEvent> msg : sentEvents.getAllValues()) {
            assertTrue(msg.size() < wayTooMuch);
            sum += msg.size();
        }
        assertEquals(wayTooMuch, sum);
    }
}
