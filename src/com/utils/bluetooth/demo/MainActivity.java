
package com.utils.bluetooth.demo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity implements OnClickListener {

    private final String TAG = "MainActivity";
    private TextView tv;
    private ListView list;
    private List<BluetoothDevice> deviceList;
    private List<String> devices;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothReceiver receiver;
    private final String lockName = "BOLUTEK";
    private Bluetooth client;
    private EditText editText;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.w(TAG, "oncreate -------");
        tv = (TextView) findViewById(R.id.textView);
        // tv.setText("" + getDeviceId());
        list = (ListView) findViewById(R.id.listView);
        editText = (EditText) findViewById(R.id.edit_text);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        deviceList = new ArrayList<BluetoothDevice>();
        devices = new ArrayList<String>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        receiver = new BluetoothReceiver();
        registerReceiver(receiver, filter);

        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Log.w(TAG, "onItemClick ===============");
                BluetoothDevice device = deviceList.get(position);
                client = new Bluetooth(device, handler);
                try {
                    if (!client.isConnect()) {
                        client.connect();
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.w(TAG, "client exception ----------");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            Log.w(TAG, "action --------- " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.w(TAG, "device -------" + device.getName());
                if (isLock(device)) {
                    devices.add(device.getName());
                    Log.w(TAG, "devices ---------" + devices.toString());
                }
                deviceList.add(device);
                Log.w(TAG, "deviceList ----------- " + deviceList.toString());
            }
            showDevices();
        }
    }

    private void showDevices() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, devices);
        list.setAdapter(adapter);
    }

    private boolean isLock(BluetoothDevice device) {
        // boolean isLockName = (device.getName()).equals(lockName);
        boolean isSingleDevice = devices.indexOf(device.getName()) == -1;
        return /* isLockName && */isSingleDevice;
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Bluetooth.CONNECT_FAILED:
//                    Log.w(TAG, "connect filed ---");
                    try {
                        client.connect();
                    } catch (Exception e) {
                        // Log.e("TAG", e.toString());
                    }
                    break;
                case Bluetooth.CONNECT_SUCCESS:
//                    Log.w(TAG, "connect success ---");
                    break;
                case Bluetooth.READ_FAILED:
                    Log.w(TAG, "read filed ---");
                    break;
                case Bluetooth.WRITE_FAILED:
                    Log.w(TAG, "write filed ----");
                    break;
                case Bluetooth.DATA:
                    Log.w(TAG, "msg.arg1 --------- " + msg.arg1);
                    tv.setText(msg.arg1);
                    break;
            }
        }
    };

    private void getDevice() {
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        if (devices.size() > 0) {
            for (Iterator<BluetoothDevice> it = devices.iterator(); it.hasNext();) {
                BluetoothDevice device = (BluetoothDevice) it.next();
                // 打印出远程蓝牙设备的物理地址
                Log.w(TAG,
                        "device --- " + device.getName() + " address ---- " + device.getAddress());
            }
        } else {
            Log.w(TAG, "no device --- ");
        }
    }

    private void getConnectBt() {
        int a2dp = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int headset = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int health = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEALTH);
        int flag = -1;
        if (a2dp == BluetoothProfile.STATE_CONNECTED) {
            flag = a2dp;
        } else if (headset == BluetoothProfile.STATE_CONNECTED) {
            flag = headset;
        } else if (health == BluetoothProfile.STATE_CONNECTED) {
            flag = health;
        }
        Log.w(TAG, "flag -------" + flag);
        if (flag != -1) {
            bluetoothAdapter.getProfileProxy(MainActivity.this,
                    new BluetoothProfile.ServiceListener() {
                        @Override
                        public void onServiceDisconnected(int profile) {
                            Toast.makeText(MainActivity.this, profile + "", Toast.LENGTH_SHORT)
                                    .show();
                            Log.w(TAG, "onServiceDisconnected -------");
                        }

                        @Override
                        public void onServiceConnected(int profile, BluetoothProfile proxy) {
                            List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                            Log.w(TAG, "onServiceConnected -------");
                            if (mDevices != null && mDevices.size() > 0) {
                                for (BluetoothDevice device : mDevices) {
                                    Log.w(TAG, "device -------");
                                }
                            } else {
                                Log.w(TAG, "no device -------");
                            }
                        }
                    }, flag);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.button:
                String text = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(text)) {
                    Log.w(TAG, "send text---------");
                    client.sendKey(text);
                }
                break;

            default:
                break;
        }
    }

}
