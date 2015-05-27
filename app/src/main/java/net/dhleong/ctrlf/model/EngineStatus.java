package net.dhleong.ctrlf.model;

import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectDataType;
import flightsim.simconnect.recv.RecvSimObjectData;
import net.dhleong.ctrlf.ui.MagnetoSwitchView.MagnetoMode;

import java.io.IOException;

import static net.dhleong.ctrlf.util.SimConnectDataUtil.readBool;

/**
 * @author dhleong
 */
public class EngineStatus implements SimData {

    public enum EngineType {
        NONE,
        JET,
        TURBOPROP,
        HELO_TURBINE,
        PISTON,
        UNKNOWN
    }

    public final EngineType type;
    public final int engines;
    private final boolean[] leftMagnetos = new boolean[4];
    private final boolean[] rightMagnetos = new boolean[4];

    EngineStatus(final RecvSimObjectData data) {
        type = readEngineType(data);
        engines = data.getDataInt32();
        leftMagnetos[0] = readBool(data);
        leftMagnetos[1] = readBool(data);
        leftMagnetos[2] = readBool(data);
        leftMagnetos[3] = readBool(data);
        rightMagnetos[0] = readBool(data);
        rightMagnetos[1] = readBool(data);
        rightMagnetos[2] = readBool(data);
        rightMagnetos[3] = readBool(data);
    }

    public EngineStatus(final EngineType type, final MagnetoMode...modes) {
        this.type = type;
        engines = modes.length;
        for (int i=0; i < engines; i++) {
            leftMagnetos[i] = modes[i].hasLeft;
            rightMagnetos[i] = modes[i].hasRight;
        }
    }


    public MagnetoMode getMagnetoMode(final int index) {
        if (index < 0 || index >= engines) {
            throw new IllegalArgumentException("Invalid magneto index: " + index);
        }

        final boolean left = leftMagnetos[index];
        final boolean right = rightMagnetos[index];
        if (left && right) {
            return MagnetoMode.BOTH;
        } else if (left) {
            return MagnetoMode.LEFT;
        } else if (right) {
            return MagnetoMode.RIGHT;
        } else {
            return MagnetoMode.OFF;
        }
    }

    @Override
    public DataType getType() {
        return DataType.ENGINE_STATUS;
    }

    private static EngineType readEngineType(final RecvSimObjectData data) {
        int index = data.getDataInt32();
        switch (index) {
        case 0: return EngineType.PISTON;
        case 1: return EngineType.JET;
        case 2: return EngineType.NONE;
        case 3: return EngineType.HELO_TURBINE;
        case 5: return EngineType.TURBOPROP;

        default:
        case 4: // for SimConnect, this is "unsupported"
            return EngineType.UNKNOWN;
        }
    }


    @SuppressWarnings("unused")
    public static void bindDataDefinition(SimConnect sc, Enum id) throws IOException {
        sc.addToDataDefinition(id, "Engine Type", "Enum", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Number of Engines", "Number", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Recip Eng Left Magneto:1", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Recip Eng Left Magneto:2", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Recip Eng Left Magneto:3", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Recip Eng Left Magneto:4", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Recip Eng Right Magneto:1", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Recip Eng Right Magneto:2", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Recip Eng Right Magneto:3", "Bool", SimConnectDataType.INT32);
        sc.addToDataDefinition(id, "Recip Eng Right Magneto:4", "Bool", SimConnectDataType.INT32);
    }
}
