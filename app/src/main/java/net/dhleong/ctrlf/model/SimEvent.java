package net.dhleong.ctrlf.model;

import net.dhleong.ctrlf.ui.MagnetoSwitchView.MagnetoMode;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Send and receivable Sim Events
 * @author dhleong
 */
public enum SimEvent {
    COM1_STANDBY("COM_STBY_RADIO_SET"),
    COM1_SWAP("COM_STBY_RADIO_SWAP"),
    NAV1_STANDBY("NAV1_STBY_SET"),
    NAV1_SWAP("NAV1_RADIO_SWAP"),
    SET_TRANSPONDER("XPNDR_SET"),

    STROBES_TOGGLE("STROBES_TOGGLE"),
    PANEL_LIGHTS_TOGGLE("PANEL_LIGHTS_TOGGLE"),
    LANDING_LIGHTS_TOGGLE("LANDING_LIGHTS_TOGGLE"),
    BEACON_LIGHTS_TOGGLE("TOGGLE_BEACON_LIGHTS"),
    TAXI_LIGHTS_TOGGLE("TOGGLE_TAXI_LIGHTS"),
    NAV_LIGHTS_TOGGLE("TOGGLE_NAV_LIGHTS"),

    SET_AP_ALTITUDE("AP_ALT_VAR_SET_ENGLISH"),
    SET_AP_HEADING("HEADING_BUG_SET"),
    AP_MASTER_TOGGLE("AP_MASTER"),
    AP_NAV_TOGGLE("AP_NAV1_HOLD"),
    AP_APR_TOGGLE("AP_APR_HOLD"),
    AP_BACKCOURSE_TOGGLE("AP_BC_HOLD"),
    AP_HEADING_TOGGLE("AP_PANEL_HEADING_HOLD"),
    AP_ALTITUDE_TOGGLE("AP_PANEL_ALTITUDE_HOLD"),

    /** NB: This is in millibars * 16 to avoid having to add floating point */
    ALTIMETER_KOHLSMAN_16("KOHLSMAN_SET"),

    MAGNETO1_OFF("MAGNETO1_OFF"),
    MAGNETO1_RIGHT("MAGNETO1_RIGHT"),
    MAGNETO1_LEFT("MAGNETO1_LEFT"),
    MAGNETO1_BOTH("MAGNETO1_BOTH"),
    MAGNETO1_START("MAGNETO1_START"),
    MAGNETO2_OFF("MAGNETO2_OFF"),
    MAGNETO2_RIGHT("MAGNETO2_RIGHT"),
    MAGNETO2_LEFT("MAGNETO2_LEFT"),
    MAGNETO2_BOTH("MAGNETO2_BOTH"),
    MAGNETO2_START("MAGNETO2_START"),
    MAGNETO3_OFF("MAGNETO3_OFF"),
    MAGNETO3_RIGHT("MAGNETO3_RIGHT"),
    MAGNETO3_LEFT("MAGNETO3_LEFT"),
    MAGNETO3_BOTH("MAGNETO3_BOTH"),
    MAGNETO3_START("MAGNETO3_START"),
    MAGNETO4_OFF("MAGNETO4_OFF"),
    MAGNETO4_RIGHT("MAGNETO4_RIGHT"),
    MAGNETO4_LEFT("MAGNETO4_LEFT"),
    MAGNETO4_BOTH("MAGNETO4_BOTH"),
    MAGNETO4_START("MAGNETO4_START"),
    ;

    static final SimEvent MAGNETOS[][] = {
        {MAGNETO1_OFF, MAGNETO1_RIGHT, MAGNETO1_LEFT, MAGNETO1_BOTH, MAGNETO1_START},
        {MAGNETO2_OFF, MAGNETO2_RIGHT, MAGNETO2_LEFT, MAGNETO2_BOTH, MAGNETO2_START},
        {MAGNETO3_OFF, MAGNETO3_RIGHT, MAGNETO3_LEFT, MAGNETO3_BOTH, MAGNETO3_START},
        {MAGNETO4_OFF, MAGNETO4_RIGHT, MAGNETO4_LEFT, MAGNETO4_BOTH, MAGNETO4_START},
    };

    private final String simConnectEventName;

    SimEvent(final String simConnectEventName) {
        this.simConnectEventName = simConnectEventName;
    }

    public String getSimConnectEventName() {
        return simConnectEventName;
    }

    public static SimEvent getMagnetoEvent(final int magnetoIndex,
            final MagnetoMode magnetoMode) {
        // relies on a strict one-to-one mapping from MagnetoMode to
        //  the events in MAGNETOS, but I think we can safely make
        //  that assumption...
        return MAGNETOS[magnetoIndex][magnetoMode.ordinal()];
    }

    public static Collection<SimEvent> getMagnetoEvents(final int magnetoIndex,
            final MagnetoMode magnetoMode) {
        final SimEvent base = getMagnetoEvent(magnetoIndex, magnetoMode);
        switch (magnetoMode) {
        case OFF:
        case START:
        case BOTH:
            return Collections.singleton(base);

        default:
            // for left/right, we need to turn OFF first
            //  to ensure consistent state
            return Arrays.asList(
                    getMagnetoEvent(magnetoIndex, MagnetoMode.OFF),
                    base);
        }
    }
}
