package net.dhleong.ctrlf.module;

import net.dhleong.ctrlf.model.Connection;

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
}
