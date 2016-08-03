package co.lujun.sample;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import co.lujun.lmbluetoothsdk.BluetoothLEController;
import co.lujun.lmbluetoothsdk.base.BluetoothLEListener;

/**
 * Created by nikola on 03.08.16..
 */
@TargetApi(21)
public class MutinyCarControllerActivity extends AppCompatActivity {

    private BluetoothLEController mBLEController;

    private List<String> mList;

    private BaseAdapter mFoundAdapter;

    private ListView lvDevices;

    private Button btnScan, btnDisconnect, btnReconnect;

    private TextView tvConnState;

    private Button bForward, bBackward, bLeft, bRigth;

    private static final String TAG = "LMBluetoothSdk";

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private static final String RC_CAR_FORWARD = "1";

    private static final String RC_CAR_BACKWARD = "2";

    private static final String RC_CAR_RIGHT = "3";

    private static final String RC_CAR_LEFT = "4";

    private static final String TITLE = "Mutiny Car Controller";

    /**
     * Setup layout, toolbar, initialize layout elements
     * and create custom controls for RC-Car
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mutinycarcontroller);
        getSupportActionBar().setTitle(TITLE);
        init();
        grantPermissions();
        rcCarCustomControlls();
    }

    /**
     * BLE listener - listening for Bluetooth changes
     */

    private BluetoothLEListener mBluetoothLEListener = new BluetoothLEListener() {
        @Override
        public void onReadData(final BluetoothGattCharacteristic characteristic) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("CAR-READ", ":" + parseData(characteristic));
                }
            });
        }

        @Override
        public void onWriteData(final BluetoothGattCharacteristic characteristic) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("CAR-WRITE", ":" + parseData(characteristic));
                }
            });
        }

        @Override
        public void onDataChanged(final BluetoothGattCharacteristic characteristic) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("CAR-READ(dataChanged)", ":" + parseData(characteristic));
                }
            });
        }

        @Override
        public void onActionStateChanged(int preState, int state) {
            Log.d(TAG, "onActionStateChanged: " + state);
        }

        @Override
        public void onActionDiscoveryStateChanged(String discoveryState) {
            if (discoveryState.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Toast.makeText(MutinyCarControllerActivity.this, "scanning!", Toast.LENGTH_SHORT).show();
            } else if (discoveryState.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Toast.makeText(MutinyCarControllerActivity.this, "scan finished!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onActionScanModeChanged(int preScanMode, int scanMode) {
            Log.d(TAG, "onActionScanModeChanged:  " + scanMode);
        }

        @Override
        public void onBluetoothServiceStateChanged(final int state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvConnState.setText("Conn state: " + Utils.transConnStateAsString(state));
                }
            });
        }

        @Override
        public void onActionDeviceFound(final BluetoothDevice device, short rssi) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mList.add(device.getName() + "@" + device.getAddress());
                    mFoundAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private void init() {
        mBLEController = BluetoothLEController.getInstance().build(this);
        mBLEController.setBluetoothListener(mBluetoothLEListener);

        mList = new ArrayList<String>();
        mFoundAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mList);

        lvDevices = (ListView) findViewById(R.id.lv_ble_devices);
        btnScan = (Button) findViewById(R.id.btn_ble_scan);
        btnDisconnect = (Button) findViewById(R.id.btn_ble_disconnect);
        btnReconnect = (Button) findViewById(R.id.btn_ble_reconnect);

        tvConnState = (TextView) findViewById(R.id.tv_ble_conn_state);

        bForward = (Button) findViewById(R.id.bForward);
        bBackward = (Button) findViewById(R.id.bBackward);
        bLeft = (Button) findViewById(R.id.bLeft);
        bRigth = (Button) findViewById(R.id.bRight);

        lvDevices.setAdapter(mFoundAdapter);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mList.clear();
                mFoundAdapter.notifyDataSetChanged();
                if (mBLEController.startScan()) {
                    Toast.makeText(MutinyCarControllerActivity.this, "Scanning!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBLEController.disconnect();
            }
        });
        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBLEController.reConnect();
            }
        });

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemStr = mList.get(position);
                mBLEController.connect(itemStr.substring(itemStr.length() - 17));
            }
        });

        if (!mBLEController.isSupportBLE()) {
            Toast.makeText(MutinyCarControllerActivity.this, "Unsupport BLE!", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    /**
     * We must ask for runtime persmission (Android M and greater)
     */

    private void grantPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("ScanActivity", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage(
                            "Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    private void rcCarCustomControlls() {
        bForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBLEController.write(RC_CAR_FORWARD.getBytes());
            }
        });

        bBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBLEController.write(RC_CAR_BACKWARD.getBytes());
            }
        });

        bRigth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBLEController.write(RC_CAR_RIGHT.getBytes());
            }
        });

        bLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBLEController.write(RC_CAR_LEFT.getBytes());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBLEController.release();
    }

    private String parseData(BluetoothGattCharacteristic characteristic) {
        String result = "";
      /*   This is special handling for the Heart Rate Measurement profile.  Data parsing is
         carried out as per profile specifications:
         http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
         For all other profiles, writes the data formatted in HEX.*/
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            result = new String(data);
        }
        //   }
        return result;
    }
}
