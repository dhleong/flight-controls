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
    public final boolean headingHold;
    public final boolean navHold;
    public final boolean approachHold;
    public final boolean revHold;
    public final boolean altitudeHold;

    public final float headingBug;
    public final int altitude;

    @SuppressWarnings("unused")
    AutoPilotStatus(final RecvSimObjectData data) {
        available = readBool(data);
        master = readBool(data);
        headingHold = readBool(data);
        navHold = readBool(data);
        approachHold = readBool(data);
        revHold = readBool(data);
        altitudeHold = readBool(data);

        headingBug = data.getDataFloat32();
        altitude = data.getDataInt32();
    }

    public AutoPilotStatus(final boolean available,
            final boolean master,
            final float headingBug, final int altitude) {
        this.available = available;
        this.master = master;
        this.headingBug = headingBug;
        this.altitude = altitude;

        // just be false by default on all these
        headingHold = navHold = approachHold = revHold = altitudeHold = false;
    }

    @Override
    public DataType getType() {
        return DataType.AUTOPILOT_STATUS;
    }

    @SuppressWarnings("unused")
    public static void bindDataDefinition(SimConnect sc, Enum id) throws IOException {
        sc.addToDataDefinition(id, "Autopilot Available", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Autopilot Master", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Autopilot Heading Lock", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Autopilot Nav1 Lock", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Autopilot Approach Hold", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Autopilot Backcourse Hold", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Autopilot Altitude Lock", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Autopilot Heading Lock Dir", "Degrees", SimConnectDataType.FLOAT32);
        sc.addToDataDefinition(id, "Autopilot Altitude Lock Var", "Feet", SimConnectDataType.INT32);
    }

}
