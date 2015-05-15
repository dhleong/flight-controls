package net.dhleong.ctrlf;

import flightsim.simconnect.NotificationPriority;
import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectConstants;
import flightsim.simconnect.config.Configuration;
import flightsim.simconnect.recv.DispatcherTask;
import flightsim.simconnect.recv.ExceptionHandler;
import flightsim.simconnect.recv.OpenHandler;
import flightsim.simconnect.recv.RecvException;
import flightsim.simconnect.recv.RecvOpen;
import net.dhleong.ctrlf.util.RadioUtil;

import java.io.IOException;

/**
 * @author dhleong
 */
public class Test {
    private static final String NAME = "FlightControls";

    void run(SimConnect sc) throws IOException {
		final String event = "COM_STBY_RADIO_SET";

		sc.mapClientEventToSimEvent(1, event);
		int cid = 0;
//		int param = 0;
		int param = RadioUtil.frequencyAsParam(124_975);
//		if (args.length > 1) {
//			cid = Integer.parseInt(args[1], 16);
//		}
//		if (args.length > 2) {
//			param = Integer.parseInt(args[2]);
//		}
		System.out.println("Sending to " + Integer.toHexString(cid) + "<- " + param);
		sc.transmitClientEvent(cid, 1, param, NotificationPriority.HIGHEST.ordinal(), SimConnectConstants.EVENT_FLAG_GROUPID_IS_PRIORITY);

//        sc.subscribeToSystemEvent(1, "Sim");
//        sc.subscribeToSystemEvent(2, "1sec");
//
//        sc.addToDataDefinition(1, "PLANE LATITUDE", "DEGREES", SimConnectDataType.FLOAT64);
//        sc.addToDataDefinition(1, "PLANE LONGITUDE", "DEGREES", SimConnectDataType.FLOAT64);
//
//        sc.requestDataOnSimObject(1, 1, 1, SimConnectPeriod.SIM_FRAME);

		DispatcherTask dt = new DispatcherTask(sc);
		dt.addOpenHandler(new OpenHandler(){
			public void handleOpen(SimConnect sender, RecvOpen e) {
				System.out.println("Connected");

			}
		});
		dt.addExceptionHandler(new ExceptionHandler(){
			public void handleException(SimConnect sender, RecvException e) {
				System.out.println("Exception (" + e.getException() +") packet " + e.getSendID());
			}
		});
		while (true) {
			sc.callDispatch(dt);
		}
    }

    public static void main(final String[] args) {

        Configuration config = new Configuration();
        config.setAddress("localhost");
        config.setPort(44506);
        try {
            final SimConnect connection = new SimConnect(NAME, config);

            new Test().run(connection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
