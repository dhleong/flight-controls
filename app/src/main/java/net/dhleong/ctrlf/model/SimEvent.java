package net.dhleong.ctrlf.model;

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
    ;


    private final String simConnectEventName;

    SimEvent(final String simConnectEventName) {
        this.simConnectEventName = simConnectEventName;
    }

    public String getSimConnectEventName() {
        return simConnectEventName;
    }
}
