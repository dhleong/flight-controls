package net.dhleong.ctrlf.model;

import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectDataType;
import flightsim.simconnect.recv.RecvSimObjectData;

import java.io.IOException;

/**
 * @author dhleong
 */
public class AltitudeStatus implements SimData {

    public final float altitude;
    public final float altitudeDeltaRate;

    @SuppressWarnings("unused")
    AltitudeStatus(final RecvSimObjectData data) {
        altitude = data.getDataFloat32();
        altitudeDeltaRate = data.getDataFloat32();
    }

    public AltitudeStatus(final float altitude,
            final float altitudeDeltaRate) {

        this.altitude = altitude;
        this.altitudeDeltaRate = altitudeDeltaRate;
    }

    @Override
    public DataType getType() {
        return DataType.ALTITUDE_STATUS;
    }

    @SuppressWarnings("unused")
    public static void bindDataDefinition(SimConnect sc, Enum id) throws IOException {
        sc.addToDataDefinition(id, "Indicated Altitude", "Feet", SimConnectDataType.FLOAT32);
        sc.addToDataDefinition(id, "Vertical Speed", "Feet per second", SimConnectDataType.FLOAT32);
    }
}
