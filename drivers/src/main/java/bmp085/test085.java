package bmp085;

public class test085 {

//	sudo java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address="8000" -cp .:/opt/pi4j/lib/pi4j-core.jar bmp085/test085
// sudo java -classpath .:classes:/opt/pi4j/lib/'*' SerialExample

	public static void main(String[] args) {

		BMP085 bmp085 = new BMP085();
    	System.out.println(bmp085.toString());
	}

}
