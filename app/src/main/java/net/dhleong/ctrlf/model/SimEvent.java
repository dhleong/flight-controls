package net.dhleong.ctrlf.model;

/**
 * Send and receivable Sim Events
 * @author dhleong
 */
public enum SimEvent {
    COM1_STANDBY("COM_STBY_RADIO_SET"),
    COM1_SWAP("COM_STBY_RADIO_SWAP"),
    SET_TRANSPONDER("XPNDR_SET"),

    STROBES_TOGGLE("STROBES_TOGGLE"),
    PANEL_LIGHTS_TOGGLE("PANEL_LIGHTS_TOGGLE"),
    LANDING_LIGHTS_TOGGLE("LANDING_LIGHTS_TOGGLE"),
    BEACON_LIGHTS_TOGGLE("TOGGLE_BEACON_LIGHTS"),
    TAXI_LIGHTS_TOGGLE("TOGGLE_TAXI_LIGHTS"),
    NAV_LIGHTS_TOGGLE("TOGGLE_NAV_LIGHTS"),

    SET_AP_ALTITUDE("AP_ALT_VAR_SET_ENGLISH");


    private final String simConnectEventName;

    SimEvent(final String simConnectEventName) {
        this.simConnectEventName = simConnectEventName;
    }

    public String getSimConnectEventName() {
        return simConnectEventName;
    }
}
