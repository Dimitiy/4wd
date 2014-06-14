package com.android.connect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import com.android.client4wd.MainActivity;
import com.android.util.Logging;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class ConnectServer  {
	String address = "0.0.0.0";
	Socket socket;
	ConnectDevice connectDev;
	private Context mContext;
	String line = null;
	boolean isSocket = false;
	InputStream sin;
	OutputStream sout;
	DataInputStream in;
	DataOutputStream out;
	public final static String TAG = "ConnectServer";

	// private Context mContext;
	public ConnectServer(Context context, String address) {
		// TODO јвтоматически созданна€ заглушка конструктора
		this.address = address;
		this.mContext = context;

		connectDev = new ConnectDevice(mContext);
		Connect connect = new Connect();
		connect.execute(address);
		// Get UsbManager from Android.

	}

	public void connect(String address) {
		Logging.doLog(TAG, "connect", "connect");
		int serverPort = 10082; // здесь об€зательно нужно указать порт к
								// которому прив€зываетс€ сервер.
		this.address = address; // это IP-адрес компьютера, где исполн€етс€ наша
								// серверна€ программа.
								// «десь указан адрес того самого компьютера где
								// будет исполн€тьс€ и клиент.

		try {
			InetAddress ipAddress = InetAddress.getByName(address); // создаем
																	// объект
																	// который
																	// отображает
																	// вышеописанный
																	// IP-адрес.
			Logging.doLog(TAG, "Any of you heard of a socket with IP address "
					+ address + " and port " + serverPort + "?",
					"Any of you heard of a socket with IP address " + address
							+ " and port " + serverPort + "?");
			socket = new Socket(ipAddress, serverPort); // создаем сокет
														// использу€
														// IP-адрес и
														// порт сервера.
			isSocket = true;
			Logging.doLog(TAG, "Yes! I just got hold of the program.",
					"Yes! I just got hold of the program.");

			// Ѕерем входной и выходной потоки сокета, теперь можем получать и
			// отсылать данные клиентом.
			sin = socket.getInputStream();
			sout = socket.getOutputStream();

			//  онвертируем потоки в другой тип, чтоб легче обрабатывать
			// текстовые сообщени€.
			in = new DataInputStream(sin);
			out = new DataOutputStream(sout);
			Logging.doLog(
					TAG,
					"Type in something and press enter. Will send it to the server and tell ya what it thinks.",
					"Type in something and press enter. Will send it to the server and tell ya what it thinks.");

			while (true) {
				line = in.readUTF();
				if (String.valueOf(line).equals("END")) {
					Logging.doLog(TAG, line, line);
					out.writeUTF(line); // отсылаем введенную строку текста
										// серверу.
					out.flush(); // заставл€ем поток закончить передачу данных.
					closeConnect();
					return;
				} else
					sendDataToArduino(line);
			}
		} catch (Exception x) {
			x.printStackTrace();
			isSocket = false;
			
		}
	}

//	public void getDataFromServer() {
//		try {
//			line = in.readUTF();
//			if (String.valueOf(line).equals("END")) {
//				Logging.doLog(TAG, line, line);
//				out.writeUTF(line); // отсылаем введенную строку текста
//									// серверу.
//				out.flush(); // заставл€ем поток закончить передачу данных.
//				closeConnect();
//				return;
//			} else
//				sendDataToArduino(line);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} // ждем пока сервер отошлет строку текста.
//
//	}

	public void sendToServerData(String line) {
		try {
			if (socket != null) {
				out.writeUTF(line);
				Logging.doLog(TAG, line, line);
				
				// отсылаем введенную строку текста серверу.
				out.flush(); // заставл€ем поток закончить передачу данных.
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendDataToArduino(String line) {
		connectDev.onResume();
		connectDev.sendData(String.valueOf(line));
		Logging.doLog(TAG, "The server was very polite. It sent me this : "
				+ line, "The server was very polite. It sent me this : " + line);
	}

	public void closeConnect() {
		try {
			if (socket != null) {
				Logging.doLog(TAG, "socket.close", "socket.close");
				isSocket = false;
				socket.close();
			} else
				Logging.doLog(TAG, "nullSocket", "nullSocket");
		} catch (IOException e) {
			// TODO јвтоматически созданный блок catch
			e.printStackTrace();
		}
		Toast.makeText(mContext, "—оединение разорвано!", Toast.LENGTH_LONG).show();
	}
	public boolean isSocket() {
		return isSocket;
		
	}
	private class Connect extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			// TODO јвтоматически созданна€ заглушка метода
			String address = params[0];
			connect(address);
			return null;
		}

	}
}