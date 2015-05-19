package net.dhleong.ctrlf.model;

/**
 * Marker interface for data objects returned by the
 *  simulation. All implementations MUST have a constructor
 *  that accepts a <code>RecvSimObjectData</code> and a
 *  static method <code>bindDataDefinition(SimConnect, Enum<?>)</code>
 *
 * @author dhleong
 */
public interface SimData {

    /** Basically a marker to remind you to add an item to DataType */
    @SuppressWarnings("unused")
    DataType getType();
}
