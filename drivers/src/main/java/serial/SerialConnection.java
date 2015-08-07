package serial;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;

public class SerialConnection {

	public static int BAUD = 57600;
	private Serial serial = null;

	
	public SerialConnection() {
		this(BAUD);
	}
	
	public SerialConnection(int baudrate) {

		BAUD = baudrate;
		
		// !! ATTENTION !!
		// By default, the serial port is configured as a console port 
		// for interacting with the Linux OS shell.  If you want to use 
		// the serial port in a software program, you must disable the 
		// OS from using this port.  Please see this blog article by  
		// Clayton Smith for step-by-step instructions on how to disable 
		// the OS console for this port:
		// http://www.irrational.net/2012/04/19/using-the-raspberry-pis-serial-port/

		System.out.println("<--Pi4J--> Serial Communication Example ... started.");
		System.out.println(" ... connect using settings: "+ BAUD +", N, 8, 1.");
		System.out.println(" ... data received on serial port should be displayed below.");

		// create an instance of the serial communications class
		serial = SerialFactory.createInstance();

		// create and register the serial data listener
		serial.addListener(new SerialDataListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				// print out the data received to the console
				System.out.print(event.getData());
			}            
		});

		try {
			serial.open(Serial.DEFAULT_COM_PORT, BAUD);
		}
		catch(Exception ex) {
			System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}
	}
	
	public void write(char data){
		serial.write(data);
	}
	
	public void write(byte data){
		serial.write(data);
	}
	
	public void write(byte[] data){
		serial.write(data);
	}
	
	public void write(String data){
		serial.write(data);
	}
		
	public void writeln(String data){
		serial.write(data +'\n');
	}
	

}

