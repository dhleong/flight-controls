package net.dhleong.ctrlf.model;

import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectDataType;
import flightsim.simconnect.recv.RecvSimObjectData;

import java.io.IOException;

/**
 * NB: The heading "bug" is attached to AutoPilotStatus;
 *  this is updated much less frequently, so let's not
 *  waste bandwidth. Besides, we turn it client-side, so
 *  we don't want to deal with race conditions there;
 *  furthermore, it *is* an autopilot property....
 *  
 * @author dhleong
 */
public class HeadingStatus implements SimData {

    /** In degrees */
    public final float heading;
    /** In degrees/second */
    public final float headingDeltaRate;

    @SuppressWarnings("unused")
    public HeadingStatus(final RecvSimObjectData data) {
        heading = data.getDataFloat32();
        headingDeltaRate = data.getDataFloat32();
    }

    public HeadingStatus(final float heading, final float headingDeltaRate) {
        this.heading = heading;
        this.headingDeltaRate = headingDeltaRate;
    }

    @Override
    public DataType getType() {
        return DataType.HEADING_STATUS;
    }

    @SuppressWarnings("unused")
    public static void bindDataDefinition(SimConnect sc, Enum id) throws IOException {
        sc.addToDataDefinition(id, "Heading Indicator", "Degrees", SimConnectDataType.FLOAT32);
        sc.addToDataDefinition(id, "Delta Heading Rate", "Degrees per second", SimConnectDataType.FLOAT32);
    }

}
