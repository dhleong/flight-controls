package net.dhleong.ctrlf.model;

import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectDataType;
import flightsim.simconnect.recv.RecvSimObjectData;

import java.io.IOException;

import static net.dhleong.ctrlf.util.SimConnectDataUtil.readBool;

/**
 * @author dhleong
 */
public class LightsStatus implements SimData {

    static final int firstOrdinal = SimEvent.STROBES_TOGGLE.ordinal();
    static final int lastOrdinal = SimEvent.NAV_LIGHTS_TOGGLE.ordinal();

    private final boolean[] states = new boolean[6];

    public LightsStatus(final RecvSimObjectData data) {
        int index = 0;
        while (data.hasRemaining()) {
            states[index++] = readBool(data);
        }
    }

    public boolean getStatus(final SimEvent ev) {
        final int number = ev.ordinal();
        if (number < firstOrdinal || number > lastOrdinal) {
            throw new IllegalArgumentException(ev + " is not a Lights event");
        }

        return states[number - firstOrdinal];
    }

    public static void bindDataDefinition(SimConnect sc, Enum id) throws IOException {
        sc.addToDataDefinition(id, "Light Strobe", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Light Panel", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Light Landing", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Light Beacon", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Light Taxi", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Light Nav", "Bool", SimConnectDataType.INT32);
    }
}
