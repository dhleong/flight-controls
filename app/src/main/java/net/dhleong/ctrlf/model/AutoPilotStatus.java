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

    AutoPilotStatus(final RecvSimObjectData data) {
        altitude = data.getDataInt32();
    }

    public AutoPilotStatus(final int altitude) {
        this.altitude = altitude;
    }

    @Override
    public DataType getType() {
        return DataType.AUTOPILOT_STATUS;
    }

    @SuppressWarnings("unused")
    public static void bindDataDefinition(SimConnect sc, Enum id) throws IOException {
        sc.addToDataDefinition(id, "Autopilot Altitude Lock Var", "Feet", SimConnectDataType.INT32);
    }

}
