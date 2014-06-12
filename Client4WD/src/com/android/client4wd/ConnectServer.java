package com.android.client4wd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

public class ConnectServer {
	String address = "0.0.0.0";
	Socket socket;
	private UsbManager usbManager;
	private UsbSerialDriver device;
	private Context mContext;
	public final static String TAG = "ConnectServer";

	// private Context mContext;
	public ConnectServer(Context context, String address) {
		// TODO Автоматически созданная заглушка конструктора
		this.address = address;
		this.mContext = context;
		usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		Connect connect = new Connect();
		connect.execute(address);
		// Get UsbManager from Android.

	}

	protected void onPause() {
		// check if the device is already closed
		if (device != null) {
			try {
				device.close();
			} catch (IOException e) {
				// we couldn't close the device, but there's nothing we can do
				// about it!
			}
			// remove the reference to the device
			device = null;
		}
	}

	public void onResume() {
		// get a USB to Serial device object
		device = UsbSerialProber.acquire(usbManager);
		if (device == null) {
			// there is no device connected!
			Log.d(TAG, "No USB serial device connected.");
		} else {
			try {
				// open the device
				device.open();
				// set the communication speed
				device.setBaudRate(115200); // make sure this matches your
				// device's setting!
			} catch (IOException err) {
				Log.e(TAG, "Error setting up USB device: " + err.getMessage(),
						err);
				try {
					// something failed, so try closing the device
					device.close();
				} catch (IOException err2) {
					// couldn't close, but there's nothing more to do!
				}
				device = null;
				return;
			}
		}
	}

	public void sendData(String message) {
		byte[] dataToSend = message.getBytes();
		Log.i(TAG, "Send data: " + message);

		// remove spurious line endings from color bytes so the serial device
		// doesn't get confused
		for (int i = 0; i < dataToSend.length - 1; i++) {
			if (dataToSend[i] == 0x0A) {
				dataToSend[i] = 0x0B;
				Log.i(TAG, "Send data: " + dataToSend[i]);

			}
		}
		// send the color to the serial device
		if (device != null) {
			try {
				device.write(dataToSend, 500);

			} catch (IOException e) {
				Log.e(TAG, "couldn't write color bytes to serial device");
			}
		} else {
			Log.d(TAG, "device = null");
		}
	}

	public void connect(String address) {
		Log.d("ConnectServ", "connect");
		int serverPort = 10082; // здесь обязательно нужно указать порт к
								// которому привязывается сервер.
		this.address = address; // это IP-адрес компьютера, где исполняется наша
								// серверная программа.
								// Здесь указан адрес того самого компьютера где
								// будет исполняться и клиент.

		try {
			InetAddress ipAddress = InetAddress.getByName(address); // создаем
																	// объект
																	// который
																	// отображает
																	// вышеописанный
																	// IP-адрес.
			Log.d("ConnectServ",
					"Any of you heard of a socket with IP address " + address
							+ " and port " + serverPort + "?");
			socket = new Socket(ipAddress, serverPort); // создаем сокет
														// используя
														// IP-адрес и
														// порт сервера.
			Log.d("ConnectServ1", "Yes! I just got hold of the program.");

			// Берем входной и выходной потоки сокета, теперь можем получать и
			// отсылать данные клиентом.
			InputStream sin = socket.getInputStream();
			OutputStream sout = socket.getOutputStream();

			// Конвертируем потоки в другой тип, чтоб легче обрабатывать
			// текстовые сообщения.
			DataInputStream in = new DataInputStream(sin);
			DataOutputStream out = new DataOutputStream(sout);

			// // Создаем поток для чтения с клавиатуры.
			// BufferedReader keyboard = new BufferedReader(new
			// InputStreamReader(
			// System.in));
			String line = null;
			Log.d("ConnectServ",
					"Type in something and press enter. Will send it to the server and tell ya what it thinks.");

			while (true) {
				line = in.readUTF(); // ждем пока сервер отошлет строку текста.
				sendData(String.valueOf(line));
				Log.d("ConnectServ",
						"The server was very polite. It sent me this : " + line);
				Log.d("ConnectServ",
						"Looks like the server is pleased with us. Go ahead and enter more lines.");
				// line = keyboard.readLine(); // ждем пока пользователь введет
				// // что-то и нажмет кнопку Enter.
				System.out.println("Sending this line to the server...");
				out.writeUTF(line); // отсылаем введенную строку текста серверу.
				out.flush(); // заставляем поток закончить передачу данных.

			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public void closeConnect() {
		try {
			if (socket != null) {
				socket.close();
			} else
				Log.d("ConnectService", "nullSocket");
		} catch (IOException e) {
			// TODO Автоматически созданный блок catch
			e.printStackTrace();
		}
	}

	private class Connect extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			// TODO Автоматически созданная заглушка метода
			String address = params[0];
			connect(address);
			return null;
		}

	}
}