package net.dhleong.ctrlf.model;

import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectDataType;
import flightsim.simconnect.recv.RecvSimObjectData;

import java.io.IOException;

import static net.dhleong.ctrlf.util.SimConnectDataUtil.readBool;

/**
 * @author dhleong
 */
public class AutoPilotStatus implements SimData {

    public final boolean available;

    public final boolean master;

    public final int altitude;
    public final float headingBug;

    @SuppressWarnings("unused")
    AutoPilotStatus(final RecvSimObjectData data) {
        available = readBool(data);
        master = readBool(data);
        altitude = data.getDataInt32();
        headingBug = data.getDataFloat32();
    }

    public AutoPilotStatus(final boolean available,
            final boolean master,
            final int altitude, final float headingBug) {
        this.available = available;
        this.master = master;
        this.altitude = altitude;
        this.headingBug = headingBug;
    }

    @Override
    public DataType getType() {
        return DataType.AUTOPILOT_STATUS;
    }

    @SuppressWarnings("unused")
    public static void bindDataDefinition(SimConnect sc, Enum id) throws IOException {
        sc.addToDataDefinition(id, "Autopilot Available", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Autopilot Master", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Autopilot Altitude Lock Var", "Feet", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Autopilot Heading Lock Dir", "Degrees", SimConnectDataType.FLOAT32);
    }

}
