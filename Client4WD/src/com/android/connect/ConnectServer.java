package com.android.connect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class ConnectServer {
	String address = "0.0.0.0";
	Socket socket;
	ConnectDevice connectDev;
	private Context mContext;
	
	public final static String TAG = "ConnectServer";

	// private Context mContext;
	public ConnectServer(Context context,String address) {
		// TODO ������������� ��������� �������� ������������
		this.address = address;
		this.mContext = context;
		connectDev = new ConnectDevice(mContext);
		Connect connect = new Connect();
		connect.execute(address);
		// Get UsbManager from Android.
		
	}

	public void connect(String address) {
		Log.d("ConnectServ", "connect");
		int serverPort = 10082; // ����� ����������� ����� ������� ���� �
								// �������� ������������� ������.
		this.address = address; // ��� IP-����� ����������, ��� ����������� ����
								// ��������� ���������.
								// ����� ������ ����� ���� ������ ���������� ���
								// ����� ����������� � ������.

		try {
			InetAddress ipAddress = InetAddress.getByName(address); // �������
																	// ������
																	// �������
																	// ����������
																	// �������������
																	// IP-�����.
			Log.d("ConnectServ",
					"Any of you heard of a socket with IP address " + address
							+ " and port " + serverPort + "?");
			socket = new Socket(ipAddress, serverPort); // ������� �����
														// ���������
														// IP-����� �
														// ���� �������.
			Log.d("ConnectServ1", "Yes! I just got hold of the program.");

			// ����� ������� � �������� ������ ������, ������ ����� �������� �
			// �������� ������ ��������.
			InputStream sin = socket.getInputStream();
			OutputStream sout = socket.getOutputStream();

			// ������������ ������ � ������ ���, ���� ����� ������������
			// ��������� ���������.
			DataInputStream in = new DataInputStream(sin);
			DataOutputStream out = new DataOutputStream(sout);

			// // ������� ����� ��� ������ � ����������.
			// BufferedReader keyboard = new BufferedReader(new
			// InputStreamReader(
			// System.in));
			String line = null;
			Log.d("ConnectServ",
					"Type in something and press enter. Will send it to the server and tell ya what it thinks.");

			while (true) {
				line = in.readUTF(); // ���� ���� ������ ������� ������ ������.
				connectDev.onResume();
				connectDev.sendData(String.valueOf(line));
				Log.d("ConnectServ",
						"The server was very polite. It sent me this : " + line);
				Log.d("ConnectServ",
						"Looks like the server is pleased with us. Go ahead and enter more lines.");
				// line = keyboard.readLine(); // ���� ���� ������������ ������
				// // ���-�� � ������ ������ Enter.
				System.out.println("Sending this line to the server...");
				out.writeUTF(line); // �������� ��������� ������ ������ �������.
				out.flush(); // ���������� ����� ��������� �������� ������.

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
			// TODO ������������� ��������� ���� catch
			e.printStackTrace();
		}
	}

	private class Connect extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			// TODO ������������� ��������� �������� ������
			String address = params[0];
			connect(address);
			return null;
		}

	}
}