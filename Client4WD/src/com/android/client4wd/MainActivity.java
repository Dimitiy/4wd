package com.android.client4wd;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.overlay.location.MyLocationItem;
import ru.yandex.yandexmapkit.overlay.location.OnMyLocationListener;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.connect.ConnectDevice;
import com.android.connect.ConnectServer;
import com.android.location.GetLocation;
import com.android.util.Logging;
import com.example.client4wd.R;

public class MainActivity extends ActionBarActivity implements
		OnMyLocationListener {
	public final static String TAG = "Client4WD";
	ConnectDevice connectDev;
	GetLocation getLocation;
	private int motorLeft = 0;
	private int motorRight = 0;
	private ToggleButton buttonWifi, LightButton;

	private Button btn_forward, btn_backward, btn_left, btn_right;
	private int pwmBtnMotorLeft; // left PWM constant value from settings
									// (постоянное значение ШИМ для левого
									// двигателя из настроек)
	private int pwmBtnMotorRight; // right PWM constant value from settings
									// (постоянное значение ШИМ для правого
									// двигателя из настроек)
	private String commandLeft; // command symbol for left motor from settings
								// (символ команды левого двигателя из настроек)
	private String commandRight; // command symbol for right motor from settings
									// (символ команды правого двигателя из
									// настроек)
	private String commandHorn; // command symbol for optional command (for
								// example - horn) from settings (символ команды
								// для доп. канала (звуковой сигнал) из
								// настроек)

	Context context;
	ConnectServer getSocket;
	TextView text;
	/** Called when the activity is first created. */
	MapController mMapController;
	RelativeLayout mView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// ------------start Service------------
		// startService(new Intent(MainActivity.this, LocationTracker.class));
		// locationTrack = new LocationTracker();
		// -----------define element-------------
		text = (TextView) findViewById(R.id.textView1);
		context = getApplicationContext();
		connectDev = new ConnectDevice(context);
//		getSupportFragmentManager().beginTransaction()
//				.add(R.id.container, new PlaceholderFragment()).commit();
		buttonWifi = (ToggleButton) findViewById(R.id.toggleButton1);
		buttonWifi.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (buttonWifi.isChecked()) {
					Log.d("isChecked", "try");
					Toast toast = Toast.makeText(getApplicationContext(), 
							"IP: " + getIPAddress(true), Toast.LENGTH_SHORT); 
							toast.show(); 
					getSocket = new ConnectServer(context, "192.168.0.108");
				} else {
					Log.d("isChecked", "false");
					getSocket.closeConnect();
				}
			}
		});
		pwmBtnMotorLeft = Integer.parseInt((String) getResources().getText(
				R.string.default_pwmBtnMotorLeft));
		pwmBtnMotorRight = Integer.parseInt((String) getResources().getText(
				R.string.default_pwmBtnMotorRight));
		commandLeft = (String) getResources().getText(
				R.string.default_commandLeft);
		commandRight = (String) getResources().getText(
				R.string.default_commandRight);
		commandHorn = (String) getResources().getText(
				R.string.default_commandHorn);
		btn_forward = (Button) findViewById(R.id.forward);
		btn_backward = (Button) findViewById(R.id.backward);
		btn_left = (Button) findViewById(R.id.left);
		btn_right = (Button) findViewById(R.id.right);
		buttonWork(false);
		loadPref();
		btn_forward.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					motorLeft = -pwmBtnMotorLeft;
					motorRight = pwmBtnMotorRight;
					connectDev.sendData(String.valueOf(commandLeft + motorLeft
							+ "\r" + commandRight + motorRight + "\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					motorLeft = 0;
					motorRight = 0;
					connectDev.sendData(String.valueOf(commandLeft + motorLeft
							+ "\r" + commandRight + motorRight + "\r"));
				}
				return false;
			}
		});

		btn_left.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					motorLeft = pwmBtnMotorLeft;
					motorRight = pwmBtnMotorRight;
					connectDev.sendData(String.valueOf(commandLeft + motorLeft
							+ "\r" + commandRight + motorRight + "\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					motorLeft = 0;
					motorRight = 0;
					connectDev.sendData(String.valueOf(commandLeft + motorLeft
							+ "\r" + commandRight + motorRight + "\r"));
				}
				return false;
			}
		});

		btn_right.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					motorLeft = -pwmBtnMotorLeft;
					motorRight = -pwmBtnMotorRight;
					connectDev.sendData(String.valueOf(commandLeft + motorLeft
							+ "\r" + commandRight + motorRight + "\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					motorLeft = 0;
					motorRight = 0;
					connectDev.sendData(String.valueOf(commandLeft + motorLeft
							+ "\r" + commandRight + motorRight + "\r"));
				}
				return false;
			}
		});

		btn_backward.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					motorLeft = pwmBtnMotorLeft;
					motorRight = -pwmBtnMotorRight;

					connectDev.sendData(String.valueOf(commandLeft + motorLeft
							+ "\r" + commandRight + motorRight + "\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					motorLeft = 0;
					motorRight = 0;
					connectDev.sendData(String.valueOf(commandLeft + motorLeft
							+ "\r" + commandRight + motorRight + "\r"));
				}

				return false;
			}

		});

		LightButton = (ToggleButton) findViewById(R.id.LightButton);
		LightButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (connectDev.isWork() == true) {
					if (LightButton.isChecked()) {
						Logging.doLog(TAG, "LightButton.isChecked() ",
								"LightButton.isChecked() ");

						buttonWork(true);
						connectDev.sendData(String.valueOf(commandHorn + "1\r"));
					} else {
						connectDev.sendData(String.valueOf(commandHorn + "0\r"));

						buttonWork(false);
					}
				} else
					LightButton.setChecked(false);
			}
		});
		final MapView mapView = (MapView) findViewById(R.id.map);
		// mapView.showBuiltInScreenButtons(true);

		mMapController = mapView.getMapController();
		// add listener
		mMapController.getOverlayManager().getMyLocation()
				.addMyLocationListener(this);
		mMapController.setZoomCurrent(15);
		mView = (RelativeLayout) findViewById(R.id.container);

	}

	@Override
	public void onMyLocationChange(MyLocationItem myLocationItem) {
		// TODO Auto-generated method stub
		// final TextView textView = new TextView(this);
		text.setText(" Координаты [" + myLocationItem.getGeoPoint().getLat()
				+ "," + myLocationItem.getGeoPoint().getLon() + "]" + "\n"
				+ "Скорость: " + myLocationItem.getSpeed());
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// mView.addView(textView);
			}
		});

	}

	public void forward(int motorLeft, int motorRight) {
		this.motorLeft = motorLeft;
		this.motorRight = motorRight;
		connectDev.sendData(String.valueOf(commandLeft + motorLeft + "\r"
				+ commandRight + motorRight + "\r"));
	}

	public void back(int motorLeft, int motorRight) {

		this.motorLeft = motorLeft;
		this.motorRight = -motorRight;

		connectDev.sendData(String.valueOf(commandLeft + motorLeft + "\r"
				+ commandRight + motorRight + "\r"));
	}

	public void left(int motorLeft, int motorRight) {
		this.motorLeft = motorLeft;
		this.motorRight = motorRight;
		connectDev.sendData(String.valueOf(commandLeft + motorLeft + "\r"
				+ commandRight + motorRight + "\r"));
	}

	public void right(int motorLeft, int motorRight) {
		this.motorLeft = -motorLeft;
		this.motorRight = -motorRight;
		connectDev.sendData(String.valueOf(commandLeft + motorLeft + "\r"
				+ commandRight + motorRight + "\r"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
//	public static class PlaceholderFragment extends Fragment {
//
//		public PlaceholderFragment() {
//		}
//
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
//				Bundle savedInstanceState) {
//			View rootView = inflater.inflate(R.layout.fragment_main, container,
//					false);
//			return rootView;
//		}
//	}

	private void buttonWork(boolean sendWork) {
		if (sendWork == false) {
			btn_forward.setEnabled(false);
			btn_backward.setEnabled(false);
			btn_left.setEnabled(false);
			btn_right.setEnabled(false);
		} else {
			btn_forward.setEnabled(true);
			btn_backward.setEnabled(true);
			btn_left.setEnabled(true);
			btn_right.setEnabled(true);
		}

	}

	private void loadPref() {

		pwmBtnMotorLeft = 255;
		pwmBtnMotorRight = 255;
		commandLeft = "L";
		commandRight = "R";
		commandHorn = "H";
	}

	@Override
	protected void onPause() {
		super.onPause();
		connectDev.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();
		connectDev.onResume();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Get IP address from first non-localhost interface
	 * 
	 * @param ipv4
	 *            true=return ipv4, false=return ipv6
	 * @return address or empty string
	 */
	public static String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf
						.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase();
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4) {
							if (isIPv4)
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 port
																// suffix
								return delim < 0 ? sAddr : sAddr.substring(0,
										delim);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		} // for now eat exceptions
		return "";
	}

	
}