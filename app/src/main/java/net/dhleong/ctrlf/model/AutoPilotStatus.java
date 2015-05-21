package net.dhleong.ctrlf.model;

import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectDataType;
import flightsim.simconnect.recv.RecvSimObjectData;

import java.io.IOException;

/**
 * @author dhleong
 */
public class AutoPilotStatus implements SimData {

    public final int altitude;
    public final float headingBug;

    @SuppressWarnings("unused")
    AutoPilotStatus(final RecvSimObjectData data) {
        altitude = data.getDataInt32();
        headingBug = data.getDataFloat32();
    }

    public AutoPilotStatus(final int altitude, final float headingBug) {
        this.altitude = altitude;
        this.headingBug = headingBug;
    }

    @Override
    public DataType getType() {
        return DataType.AUTOPILOT_STATUS;
    }

    @SuppressWarnings("unused")
    public static void bindDataDefinition(SimConnect sc, Enum id) throws IOException {
        sc.addToDataDefinition(id, "Autopilot Altitude Lock Var", "Feet", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Autopilot Heading Lock Dir", "Degrees", SimConnectDataType.FLOAT32);
    }

}
