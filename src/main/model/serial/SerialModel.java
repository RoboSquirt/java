package main.model.serial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import main.model.plate.PlateModel;
import main.model.tasks.TaskModel;
import main.view.dialogs.SimpleDialogs;
import main.view.panels.MainPanel;

/**
 * Model that handles all communication with the micro-controller. Will reach into the other model to
 * get data regarding wells and plates when needed.
 * @author Christian
 *
 */
public class SerialModel implements SerialPortEventListener {
	
	/**
	 * Adapter from serial model to the main view.
	 */
	private MainPanel view;
	
	/**
	 * Adapter from the task model to plate model.
	 */
	private TaskModel taskModel;
	
	/**
	 * Adapter from the task model to plate model.
	 */
	private PlateModel plateModel;
	
	/**
	 * Serial port the Arduino is found on.
	 */
	private SerialPort arduinoPort;
	
	/**
	 * Input stream is made a BufferedReader to more easily parse incoming data.
	 */
	private BufferedReader inputStream;
	
	/**
	 * Output stream to talk to the Arduino through.
	 */
	private OutputStream outputStream;
	
	/**
	 * Constructor that links the model to view via its adapter.
	 */
	public SerialModel(){
	}
	
	/**
	 * Called by the controller, performs any start up tasks necessary for the model.
	 */
	public void start(MainPanel view, TaskModel taskModel, PlateModel plateModel){
        this.view = view;
        this.taskModel = taskModel;
        this.plateModel = plateModel;
		scanForPorts();
	}
	
	/**
	 * Scans for any serial ports that are available on the computer (ones that have things connected to them, usually).
	 */
	public Iterable<String> scanForPorts(){

		//we know the ports will all be CommPortIdentifiers
		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
		ArrayList<String> serialPorts = new ArrayList<String>();
		while (ports.hasMoreElements())
        {
            CommPortIdentifier currPort = ports.nextElement();
            //get only serial ports
            if (currPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
            	serialPorts.add(currPort.getName());
            }
        }

		//ship it off to the view to be put in a combobox!
        return serialPorts;
		//view.addPortsToList(serialPorts);
	}
	
	/**
	 * Connects to the port matching the input port name.
	 * @param _portName - port to connect to
	 */
	public void connectToPort(String _portName){
		try {
			arduinoPort = (SerialPort) CommPortIdentifier.getPortIdentifier(_portName).open("Arduino", 2000);
			initIOStream();
			System.out.println("Successfully connected to the Arduino on port " + _portName + "!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Disconnects the current serial port. 
	 */
	public void disconnectPort(){
		arduinoPort.removeEventListener();
		arduinoPort.close();
	}
	
	/**
	 * Initializes the input and output streams on the pre-selected microcontroller port.
	 */
	public void initIOStream(){
		try {
			inputStream = new BufferedReader(new InputStreamReader(arduinoPort.getInputStream()));
			outputStream = arduinoPort.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			arduinoPort.addEventListener(this);
			arduinoPort.notifyOnDataAvailable(true);
		} catch (TooManyListenersException e){
			e.printStackTrace();
		}
	}

	/**
	 * Called whenever data is sent from the microcontroller through the serial port. Reads off the data
	 * and processes it accordingly.
	 */
	@Override
	public void serialEvent(SerialPortEvent thisEvent) {
		if (thisEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				if (inputStream.ready()){
					processSerialInput(inputStream.readLine());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Called when we get a valid line of serial data. When the Arduino is done with its current command, it
	 * sends "Done" back to queue up another one (if there is another one). When this is received, this serial
	 * model tells the plate model to execute the next task in its list.
	 */
	public void processSerialInput(String serialInput){
		System.out.println("From Arduino: " + serialInput);
		if (serialInput.equals("Done")){
			taskModel.executeNext();
		}
		if (serialInput.equals("Finished Calibration")){
			plateModel.calibrate();
		}
	}
	
	/**
	 * @return the OutputStream being used to talk to the Arduino
	 */
	public OutputStream getOutputStream(){
		return this.outputStream;
	}

	/**
	 * Send text directly to the Arduino.
	 * @param command - string to send Arduino
	 */
	public void sendText(String command) {
        if (outputStream == null) {
            SimpleDialogs.popNoSerialConnection(null);
        }
        else if (!checkSendCommand(command)) {
            SimpleDialogs.popBadText(null);
        }
        else {
            for (int i = 0; i < command.length(); i++){
                try {
                    outputStream.write(command.charAt(i));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}

    /**
     * Check if the serial command to be send conforms to the form "name(values)".
     */
    public boolean checkSendCommand(String toSend) {
        return toSend.matches("\\w+\\(\\w*\\)");
    }
}
