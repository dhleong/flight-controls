package net.dhleong.ctrlf.model;

/**
 * Send and receivable Sim Events
 * @author dhleong
 */
public enum SimEvent {
    COM1_SWAP,
    COM1_STANDBY,
    SET_TRANSPONDER,

    STROBES_TOGGLE,
    PANEL_LIGHTS_TOGGLE,
    LANDING_LIGHTS_TOGGLE,
    BEACON_LIGHTS_TOGGLE,
    TAXI_LIGHTS_TOGGLE,
    NAV_LIGHTS_TOGGLE,

    SET_AP_ALTITUDE,
}
