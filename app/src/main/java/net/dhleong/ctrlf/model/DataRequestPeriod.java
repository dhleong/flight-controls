package net.dhleong.ctrlf.model;

/**
 * @author dhleong
 */
public enum DataRequestPeriod {
    /** A single request */
    SINGLE,

    /** Latency is not an issue; data may be returned at any rate */
    SLOW,

    /** Data should be returned as fast as possible */
    FAST

}
