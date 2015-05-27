package net.dhleong.ctrlf.model;

/**
 * Registry for SimData VALUES. Useful for Connections to
 *  register different VALUES and determine how to parse
 *  requested objects. Also how data is requested
 *
 * @author dhleong
 */
public enum DataType {
    AUTOPILOT_STATUS(AutoPilotStatus.class),
    ENGINE_STATUS(EngineStatus.class),
    HEADING_STATUS(HeadingStatus.class),
    LIGHT_STATUS(LightsStatus.class),
    RADIO_STATUS(RadioStatus.class);

    final Class<? extends SimData> implementationType;

    DataType(Class<? extends SimData> implementationType) {
        this.implementationType = implementationType;
    }

    /**
     * Shared array of values in this enum;
     *  Prefer this to the method call, since
     *  that constructs a new array each time
     */
    static final DataType[] VALUES = values();

    static DataType fromInt(final int input) {
        return VALUES[input];
    }
}
