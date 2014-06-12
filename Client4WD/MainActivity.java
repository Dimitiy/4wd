package com.android.client4wd;

import android.content.Context;
import android.location.Location;
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
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.location.GetLocation;
import com.android.location.LocationTracker;
import com.example.client4wd.R;

public class MainActivity extends ActionBarActivity {
	public final static String TAG = "Client4WD";

	private int motorLeft = 0;
	private int motorRight = 0;
	private ToggleButton LightButton;
	private ToggleButton buttonWifi;

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
	LocationTracker locTracker;
	GetLocation getLocation;
	TextView text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		text = (TextView) findViewById(R.id.textView1);
		context = getApplicationContext();
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, new PlaceholderFragment()).commit();
		locTracker = new LocationTracker();
		getSocket = new ConnectServer(context, "192.168.1.41");
		buttonWifi = (ToggleButton) findViewById(R.id.toggleButton1);
		buttonWifi.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (buttonWifi.isChecked()) {
					Log.d("isChecked", "try");
					getSocket = new ConnectServer(context, "192.168.1.41");
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
		loadPref();
		btn_forward.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					motorLeft = -pwmBtnMotorLeft;
					motorRight = pwmBtnMotorRight;
					getSocket.sendData(String.valueOf(commandLeft + motorLeft + "\r"
							+ commandRight + motorRight + "\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					motorLeft = 0;
					motorRight = 0;
					getSocket.sendData(String.valueOf(commandLeft + motorLeft + "\r"
							+ commandRight + motorRight + "\r"));
				}
				return false;
			}
		});

		btn_left.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					motorLeft = pwmBtnMotorLeft;
					motorRight = pwmBtnMotorRight;
					getSocket.sendData(String.valueOf(commandLeft + motorLeft + "\r"
							+ commandRight + motorRight + "\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					motorLeft = 0;
					motorRight = 0;
					getSocket.sendData(String.valueOf(commandLeft + motorLeft + "\r"
							+ commandRight + motorRight + "\r"));
				}
				return false;
			}
		});

		btn_right.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					motorLeft = -pwmBtnMotorLeft;
					motorRight = -pwmBtnMotorRight;
					getSocket.sendData(String.valueOf(commandLeft + motorLeft + "\r"
							+ commandRight + motorRight + "\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					motorLeft = 0;
					motorRight = 0;
					getSocket.sendData(String.valueOf(commandLeft + motorLeft + "\r"
							+ commandRight + motorRight + "\r"));
				}
				return false;
			}
		});

		btn_backward.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					motorLeft = pwmBtnMotorLeft;
					motorRight = -pwmBtnMotorRight;

					getSocket.sendData(String.valueOf(commandLeft + motorLeft + "\r"
							+ commandRight + motorRight + "\r"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					motorLeft = 0;
					motorRight = 0;
					getSocket.sendData(String.valueOf(commandLeft + motorLeft + "\r"
							+ commandRight + motorRight + "\r"));
				}
				return false;
			}
		});

		LightButton = (ToggleButton) findViewById(R.id.LightButton);
		LightButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (LightButton.isChecked()) {
					getSocket.sendData(String.valueOf(commandHorn + "1\r"));
				} else {
					getSocket.sendData(String.valueOf(commandHorn + "0\r"));
				}
			}
		});
		

	}

	public void addLog(String logs) {
		final String log = logs;
		// Handler h = new Handler(getApplicationContext().getMainLooper());
		//
		// h.post(new Runnable() {
		// @Override
		// public void run() {
		if (log != null && text != null)
			text.append(log);
		else
			Log.d("Main", "log null");
		// }
		// });

	}

	public void forward(int motorLeft, int motorRight) {
		this.motorLeft = motorLeft;
		this.motorRight = motorRight;
		getSocket.sendData(String.valueOf(commandLeft + motorLeft + "\r" + commandRight
				+ motorRight + "\r"));
	}

	public void back(int motorLeft, int motorRight) {

		this.motorLeft = motorLeft;
		this.motorRight = -motorRight;

		getSocket.sendData(String.valueOf(commandLeft + motorLeft + "\r" + commandRight
				+ motorRight + "\r"));
	}

	public void left(int motorLeft, int motorRight) {
		this.motorLeft = motorLeft;
		this.motorRight = motorRight;
		getSocket.sendData(String.valueOf(commandLeft + motorLeft + "\r" + commandRight
				+ motorRight + "\r"));
	}

	public void right(int motorLeft, int motorRight) {
		this.motorLeft = -motorLeft;
		this.motorRight = -motorRight;
		getSocket.sendData(String.valueOf(commandLeft + motorLeft + "\r" + commandRight
				+ motorRight + "\r"));
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
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
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
		getSocket.onPause();
//		// check if the device is already closed
//		if (device != null) {
//			try {
//				device.close();
//			} catch (IOException e) {
//				// we couldn't close the device, but there's nothing we can do
//				// about it!
//			}
//			// remove the reference to the device
//			device = null;
//		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getSocket.onResume();
//		// get a USB to Serial device object
//		device = UsbSerialProber.acquire(usbManager);
//		if (device == null) {
//			// there is no device connected!
//			Log.d(TAG, "No USB serial device connected.");
//		} else {
//			try {
//				// open the device
//				device.open();
//				// set the communication speed
//				device.setBaudRate(115200); // make sure this matches your
//				// device's setting!
//			} catch (IOException err) {
//				Log.e(TAG, "Error setting up USB device: " + err.getMessage(),
//						err);
//				try {
//					// something failed, so try closing the device
//					device.close();
//				} catch (IOException err2) {
//					// couldn't close, but there's nothing more to do!
//				}
//				device = null;
//				return;
//			}
//		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	
}