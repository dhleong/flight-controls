package net.dhleong.ctrlf.util;

import flightsim.simconnect.recv.RecvSimObjectData;

/**
 * @author dhleong
 */
public class SimConnectDataUtil {
    public static boolean readBool(final RecvSimObjectData data) {
        return 0 != data.getDataInt32();
    }
}
