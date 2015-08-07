package bmp085;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

/**
 * Refactored from c-code, original:
 * John Burns
 * http://www.john.geek.nz/2012/08/reading-data-from-a-bosch-bmp085-with-a-raspberry-pi/
 *
 */
public class BMP085 {

	/**
	 * sudo java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address="8000" -cp .:/opt/pi4j/lib/pi4j-core.jar bmp085/test085
		 bmp085.BMP085 bmp085 = new bmp085.BMP085();
		 System.out.println("Temperature: "+ bmp085.getTemperature() + " C");
		 System.out.println("Pressure: "+ bmp085.getPressure()+ " hPa");
		 System.out.println("Altitude: "+ bmp085.getAltitude()+ " m");
	 */

	private I2CBus bus;
	private I2CDevice bmp085;
	
	private short ac1;		// 16-bit short
	private short ac2;		// 16-bit short
	private short ac3;		// 16-bit short
	private int ac4;		// 16-bit unsigned short
	private int ac5;		// 16-bit unsigned short
	private int ac6;		// 16-bit unsigned short
	private short b1;		// 16-bit short
	private short b2;		// 16-bit short
	private short mb;		// 16-bit short
	private short mc;		// 16-bit short
	private short md;		// 16-bit short
	private int b5;

	private final int BMP085_OVERSAMPLING_SETTING = 3;
	
	public BMP085() {

        try {
            // get I2C bus instance
        	bus = I2CFactory.getInstance(I2CBus.BUS_1);
        	bmp085 = bus.getDevice(0x77);

        	// read calibration data from bmp085.BMP085
        	ac1 = (short) getUnsigned(bmp085.read(0xAA), bmp085.read(0xAB));
        	ac2 = (short) getUnsigned(bmp085.read(0xAC), bmp085.read(0xAD));
        	ac3 = (short) getUnsigned(bmp085.read(0xAE), bmp085.read(0xAF));

        	ac4 = getUnsigned(bmp085.read(0xB0), bmp085.read(0xB1));
        	ac5 = getUnsigned(bmp085.read(0xB2), bmp085.read(0xB3));
        	ac6 = getUnsigned(bmp085.read(0xB4), bmp085.read(0xB5));

        	b1 = (short) getUnsigned(bmp085.read(0xB6), bmp085.read(0xB7));
        	b2 = (short) getUnsigned(bmp085.read(0xB8), bmp085.read(0xB9));
        	mb = (short) getUnsigned(bmp085.read(0xBA), bmp085.read(0xBB));
        	mc = (short) getUnsigned(bmp085.read(0xBC), bmp085.read(0xBD));
        	md = (short) getUnsigned(bmp085.read(0xBE), bmp085.read(0xBF));
         		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// convenience method, because Java can't handle unsigned types
	private int getUnsigned(int msb, int lsb)
	{
		int b1 = 0, b2 = 0;
		b1 = (msb & 0xFF);
		b2 = (lsb & 0xFF);
		return ((b1 << 8) | b2);
	}
	
	// Read the uncompensated temperature value
	private int getUt()
	{
		int ut = 0;
		
		try {
			// Write 0x2E into Register 0xF4
			// This requests a temperature reading
			bmp085.write(0xF4, (byte) 0x2E);	

			// Wait at least 4.5ms
			Thread.sleep(5);

			// Read the two byte result from address 0xF6, 0xF7
			ut = getUnsigned(bmp085.read(0xF6), bmp085.read(0xF7));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return ut;
	}
	
	// Read the uncompensated pressure value
	private int getUp()
	{
		int up = 0;
		try {
			// Write 0x34+(BMP085_OVERSAMPLING_SETTING<<6) into register 0xF4
			// Request a pressure reading w/ oversampling setting
			byte b = (byte) ((byte)0x34 + (byte)(BMP085_OVERSAMPLING_SETTING<<6));
			bmp085.write(0xF4, b);

			// Wait for conversion, delay time dependent on oversampling setting
			Thread.sleep((BMP085_OVERSAMPLING_SETTING +1) *  5);

			// Read the three byte result from 0xF6
			// 0xF6 = MSB, 0xF7 = LSB and 0xF8 = XLSB
			byte[] values = new byte[3];
			bmp085.read(0xF6, values, 0, 3);

			// You really need to AND because Java will automatically 
			// do 2's complement conversion and mess up the variable
			int msb = values[0] & 0xFF;
			int lsb = values[1] & 0xFF;
			int xlsb = values[2] & 0xFF;
			up = (int)(((msb << 16) | (lsb << 8) | xlsb) >> (8-BMP085_OVERSAMPLING_SETTING));
	
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return up;
	}

	public double getTemperature()
	{	  
		int ut = this.getUt();
		int x1 = (((int)ut - (int)ac6)*(int)ac5) >> 15;
		int x2 = ((int)mc << 11)/(x1 + md);
		b5 = x1 + x2;

		int result = ((b5 + 8)>>4);

		return (double)((double)result / (double)10);
	}
	
	public double getPressure()
	{
		int up = getUp();
		int x1, x2, x3, b3, b6, p;
		int b4, b7;
	  
		b6 = b5 - 4000;
		// Calculate B3
		x1 = (b2 * (b6 * b6)>>12)>>11;
		x2 = (ac2 * b6)>>11;
		x3 = x1 + x2;
		b3 = (((((int)ac1)*4 + x3)<<BMP085_OVERSAMPLING_SETTING) + 2)>>2;
	  
		// Calculate B4
		x1 = (ac3 * b6)>>13;
		x2 = (b1 * ((b6 * b6)>>12))>>16;
		x3 = ((x1 + x2) + 2)>>2;
		b4 = (ac4 * (int)(x3 + 32768))>>15;
	  
		b7 = ((int)(up - b3) * (50000>>BMP085_OVERSAMPLING_SETTING));
		if (b7 < 0x80000000)
			p = (b7<<1)/b4;
		else
			p = (b7/b4)<<1;
		
		x1 = (p>>8) * (p>>8);
		x1 = (x1 * 3038)>>16;
		x2 = (-7357 * p)>>16;
		p += (x1 + x2 + 3791)>>4;
	  
		return (double)((double)p / (double)100);
	}
	
	public double getAltitude()
	{
		double pressure = getPressure();
		final double pSeaLevel = 1013.25;	// pressure at sea level
		double altitude = 0;
		
		altitude = 44330 * (1- Math.pow((pressure / pSeaLevel), (1/5.255)));
		return Math.floor(altitude*100)/100;		
	}
	
	@Override
	public String toString() {
		return "BMP085: \n" +
		"Temperature: "+ getTemperature() + " C\n"+
		"Pressure: "+ getPressure()+ " hPa\n"+
		"Altitude: "+ getAltitude()+ " m\n";
	}
}