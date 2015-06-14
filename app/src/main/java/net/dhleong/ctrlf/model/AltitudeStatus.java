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
    public final int kohlsmanMb;

    @SuppressWarnings("unused")
    AltitudeStatus(final RecvSimObjectData data) {
        altitude = data.getDataFloat32();
        altitudeDeltaRate = data.getDataFloat32();
        kohlsmanMb = data.getDataInt32();
    }

    public AltitudeStatus(final float altitude,
            final float altitudeDeltaRate,
            final int kohlsmanMb) {

        this.altitude = altitude;
        this.altitudeDeltaRate = altitudeDeltaRate;
        this.kohlsmanMb = kohlsmanMb;
    }

    @Override
    public DataType getType() {
        return DataType.ALTITUDE_STATUS;
    }

    @SuppressWarnings("unused")
    public static void bindDataDefinition(SimConnect sc, Enum id) throws IOException {
        sc.addToDataDefinition(id, "Indicated Altitude", "Feet", SimConnectDataType.FLOAT32);
        sc.addToDataDefinition(id, "Vertical Speed", "Feet per second", SimConnectDataType.FLOAT32);
        sc.addToDataDefinition(id, "Kohlsman Setting MB", "Millibars", SimConnectDataType.INT32);
    }
}
