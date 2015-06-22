package net.dhleong.ctrlf.history;

/**
 * @author dhleong
 */
public class HistoricalConnection {

    final String host;
    final int port;

    public HistoricalConnection(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return String.format("%s:%d", host, port);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoricalConnection)) return false;

        final HistoricalConnection that = (HistoricalConnection) o;

        if (port != that.port) return false;
        return host.equals(that.host);

    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }
}
