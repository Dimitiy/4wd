package com.android.connect;

import java.io.IOException;

import android.content.Context;
import android.hardware.usb.UsbManager;

import com.android.util.Logging;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

public class ConnectDevice {
	private UsbManager usbManager;
	private UsbSerialDriver device;
	private Context mContext;
	private String TAG = "ConnectDevice";
	
	private boolean work;

	public ConnectDevice(Context context) {
		this.mContext = context;
		usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
	
	}

	public void onPause() {
		// check if the device is already closed
		if (device != null) {
			try {
				device.close();
			} catch (IOException e) {
				// we couldn't close the device, but there's nothing we can do
				// about it!
			}
			// remove the reference to the device
			Logging.doLog(TAG, "device = null ", "device = null  ");
			
			device = null;
		}
		Logging.doLog(TAG, "pause device  ", "pause device ");
		work = false;

	}

	public void onResume() {
		// get a USB to Serial device object
		device = UsbSerialProber.acquire(usbManager);
		if (device == null) {
			// there is no device connected!
			Logging.doLog(TAG, "No USB serial device connected.",
					"No USB serial device connected.");
			Logging.doLog(TAG, "device = null ", "device = null ");

			work = false;

		} else {
			try {

				// open the device
				device.open();
				// set the communication speed
				device.setBaudRate(115200); // make sure this matches your
				// device's setting!
				Logging.doLog(TAG, "device.open() ", "device.open() ");
			
				work = true;
			} catch (IOException err) {
				Logging.doLog(TAG,
						"Error setting up USB device: " + err.getMessage(),
						"Error setting up USB device: " + err.getMessage());
				try {
					// something failed, so try closing the device
					device.close();
					Logging.doLog(TAG, "device.close(); ", "device.close(); ");
					} catch (IOException err2) {
					// couldn't close, but there's nothing more to do!
				}
				work = false;
				Logging.doLog(TAG, "Resume device = null; ", "Resume device = null ");
				
				device = null;
				return;
			}
		}
	}

	public void sendData(String message) {
		byte[] dataToSend = message.getBytes();
		Logging.doLog(TAG, "Send data: " + message, "Send data: " + message);

		// remove spurious line endings from color bytes so the serial device
		// doesn't get confused
		for (int i = 0; i < dataToSend.length - 1; i++) {
			if (dataToSend[i] == 0x0A) {
				dataToSend[i] = 0x0B;
				Logging.doLog(TAG, "Send data: " + dataToSend[i], "Send data: "
						+ dataToSend[i]);

			}
		}
		// send the color to the serial device
		if (device != null) {
			try {
				device.write(dataToSend, 500);

			} catch (IOException e) {
				Logging.doLog(TAG,
						"couldn't write bytes to serial device",
						"couldn't write bytes to serial device");
			}
		} else {
			Logging.doLog(TAG, "device = null", "device = null");
		}
	}

	public boolean isWork() {
		return work;
	}

}
