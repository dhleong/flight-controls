package net.dhleong.ctrlf.model;

import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectDataType;
import flightsim.simconnect.recv.RecvSimObjectData;
import net.dhleong.ctrlf.util.RadioUtil;

import java.io.IOException;

/**
 * @author dhleong
 */
public class RadioStatus {

    private static final String FREQ = "Frequency BCD16";

    /** Whether or not we have power to any radio */
    public final boolean avionicsMaster;
    public final int com1Active, com1Standby;

    public RadioStatus(final RecvSimObjectData data) {
        avionicsMaster = readBool(data);
        com1Active = readFrequency(data);
        com1Standby = readFrequency(data);
    }

    public RadioStatus(final boolean avionicsMaster,
            final int com1Active, final int com1Standby) {
        this.avionicsMaster = avionicsMaster;
        this.com1Active = com1Active;
        this.com1Standby = com1Standby;
    }

    public static void bindDataDefinition(SimConnect sc, Enum id) throws IOException {
        sc.addToDataDefinition(id, "Avionics Master Switch", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Com Active Frequency:1", FREQ, SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Com Standby Frequency:1", FREQ, SimConnectDataType.INT32);
    }

    static boolean readBool(final RecvSimObjectData data) {
        return 0 != data.getDataInt32();
    }

    static int readFrequency(final RecvSimObjectData data) {
        return RadioUtil.paramAsFrequency(data.getDataInt32());
    }
}
