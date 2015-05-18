package net.dhleong.ctrlf.module;

import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.SimEvent;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Convenient subclass of AppModule allowing
 *  for the injection of a mocked Connection
 *
 * @author dhleong
 */
public abstract class TestModule extends AppModule {

    Connection connection;

    public TestModule() {
        super(null);
    }

    @Override
    public Connection provideConnection() {
        // mimic ScopedProvider
        Connection result = connection;
        if (result == null) {
            synchronized (this) {
                //noinspection ConstantConditions
                if (result == null) {
                    result = connection = createConnection();
                }
            }
        }
        return result;
    }

    private Connection createConnection() {
        final Connection connection = mock(Connection.class);
        mockConnection(connection);
        return connection;
    }

    protected abstract void mockConnection(final Connection mock);

    public static Answer storeEvent(final List<SimEvent> destination) {
        return new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final SimEvent param = (SimEvent) invocation.getArguments()[0];
                destination.add(param);
                return null;
            }
        };
    }

    public static Answer storeParam(final List<Integer> destination) {
        return new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final Integer param = (Integer) invocation.getArguments()[1];
                destination.add(param);
                return null;
            }
        };
    }

}
