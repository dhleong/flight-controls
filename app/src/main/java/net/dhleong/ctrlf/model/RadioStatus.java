package net.dhleong.ctrlf.model;

import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectDataType;
import flightsim.simconnect.recv.RecvSimObjectData;
import net.dhleong.ctrlf.util.RadioUtil;

import java.io.IOException;

import static net.dhleong.ctrlf.util.SimConnectDataUtil.readBool;

/**
 * @author dhleong
 */
public class RadioStatus implements SimData {

    private static final String FREQ = "Frequency BCD16";

    /** Whether or not we have power to any radio */
    public final boolean avionicsPower;
    public final int com1Active, com1Standby;

    public RadioStatus(final RecvSimObjectData data) {
        avionicsPower = readBool(data);
        com1Active = readFrequency(data);
        com1Standby = readFrequency(data);
    }

    public RadioStatus(final boolean avionicsPower,
            final int com1Active, final int com1Standby) {
        this.avionicsPower = avionicsPower;
        this.com1Active = com1Active;
        this.com1Standby = com1Standby;
    }

    public static void bindDataDefinition(SimConnect sc, Enum id) throws IOException {
        sc.addToDataDefinition(id, "Circuit Avionics On", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Com Active Frequency:1", FREQ, SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Com Standby Frequency:1", FREQ, SimConnectDataType.INT32);
    }

    static int readFrequency(final RecvSimObjectData data) {
        return RadioUtil.paramAsFrequency(data.getDataInt32());
    }
}
