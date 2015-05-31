package net.dhleong.ctrlf.model;

import net.dhleong.ctrlf.model.Connection.Lifecycle;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class FsxConnectionTest {

    FsxConnection connection;

    @Before
    public void setUp() {
        // NB: Thanks to RxJava we don't have to messily
        //  mock out the network connection. This is not
        //  exactly a black box test, but it models the
        //  actual behavior closely enough to be fine
        connection = new FsxConnection();
    }

    @Test
    public void connectedStateReplayed() {
        submitLifecycle(connection, Lifecycle.CONNECTED);

        assertThat(slurpEvents(connection))
                .containsExactly(Lifecycle.CONNECTED);
    }

    @Test
    public void startedStateReplayed() {
        submitLifecycle(connection, Lifecycle.CONNECTED);
        submitLifecycle(connection, Lifecycle.SIM_START);

        assertThat(slurpEvents(connection))
                .containsExactly(
                        Lifecycle.CONNECTED,
                        Lifecycle.SIM_START);
    }

    @Test
    public void connectionErrorEarlySubscription() {
        final Observable<Lifecycle> events = connection.lifecycleEvents();

        // disconnected before sim start, but after
        //  we've already subscribed...
        submitLifecycle(connection, Lifecycle.CONNECTED);
        submitLifecycle(connection, Lifecycle.DISCONNECTED);

        // ... we want to have heard everything
        assertThat(slurpEvents(connection, events))
                .containsExactly(
                        Lifecycle.CONNECTED,
                        Lifecycle.DISCONNECTED);
    }

    @Test
    public void connectionError() {
        // connected before we start observing,
        //  but disconnected before sim start
        submitLifecycle(connection, Lifecycle.CONNECTED);

        // if we'd started observing after the disconnect, then
        //  we wouldn't need to know about the connect, either
        final Observable<Lifecycle> events = connection.lifecycleEvents();
        submitLifecycle(connection, Lifecycle.DISCONNECTED);

        assertThat(slurpEvents(connection, events))
                .containsExactly(
                        Lifecycle.CONNECTED,
                        Lifecycle.DISCONNECTED);
    }

    @Test
    public void connectionErrorLateSubscription() {
        submitLifecycle(connection, Lifecycle.CONNECTED);
        submitLifecycle(connection, Lifecycle.DISCONNECTED);

        // if a connection fails and nobody's there to hear it....
        assertThat(slurpEvents(connection)).isEmpty();
    }

    @Test
    public void reconnect() {
        submitLifecycle(connection, Lifecycle.CONNECTED);
        submitLifecycle(connection, Lifecycle.SIM_START);
        submitLifecycle(connection, Lifecycle.SIM_QUIT);
        submitLifecycle(connection, Lifecycle.DISCONNECTED);
        submitLifecycle(connection, Lifecycle.CONNECTED);

        assertThat(slurpEvents(connection))
                .containsExactly(
                        Lifecycle.CONNECTED);
    }

    static void submitLifecycle(FsxConnection connection, Lifecycle event) {
        connection.lifecycleSubject.onNext(event);
    }

    /**
     * "Finish" the lifecycle events on the connection and
     *  slurp all the events that can be slurped from the
     *  event stream into an array.
     * NB: Further lifecycle event submissions to the connection
     *  will result in errors!
     */
    static List<Lifecycle> slurpEvents(final FsxConnection connection) {
        return slurpEvents(connection, connection.lifecycleEvents());
    }

    static List<Lifecycle> slurpEvents(final FsxConnection connection,
            Observable<Lifecycle> observable) {
        connection.lifecycleSubject.onCompleted();
        return observable.toList().toBlocking().single();
    }

}
