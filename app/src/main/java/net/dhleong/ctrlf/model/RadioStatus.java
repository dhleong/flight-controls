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
    public final int nav1Active, nav1Standby;
    public final int transponder;

    public RadioStatus(final RecvSimObjectData data) {
        avionicsPower = readBool(data);
        com1Active = readFrequency(data);
        com1Standby = readFrequency(data);
        nav1Active = readFrequency(data);
        nav1Standby = readFrequency(data);
        transponder = readTransponder(data);
    }

    public RadioStatus(final boolean avionicsPower,
            final int com1Active, final int com1Standby,
            final int nav1Active, final int nav1Standby,
            final int transponder) {
        this.avionicsPower = avionicsPower;
        this.com1Active = com1Active;
        this.com1Standby = com1Standby;
        this.nav1Active = nav1Active;
        this.nav1Standby = nav1Standby;
        this.transponder = transponder;
    }

    @Override
    public DataType getType() {
        return DataType.RADIO_STATUS;
    }

    @SuppressWarnings("unused")
    public static void bindDataDefinition(SimConnect sc, Enum id) throws IOException {
        sc.addToDataDefinition(id, "Circuit Avionics On", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Com Active Frequency:1", FREQ, SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Com Standby Frequency:1", FREQ, SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Nav Active Frequency:1", FREQ, SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Nav Standby Frequency:1", FREQ, SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Transponder Code:1", "BCO 16", SimConnectDataType.INT32);
    }

    static int readFrequency(final RecvSimObjectData data) {
        return RadioUtil.paramAsFrequency(data.getDataInt32());
    }

    static int readTransponder(final RecvSimObjectData data) {
        return RadioUtil.paramAsTransponder(data.getDataInt32());
    }
}
