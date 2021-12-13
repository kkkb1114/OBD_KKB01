package com.example.obd_kkb01;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class BlueToothSearch extends AppCompatActivity{

    Context context;
    TextView title, OBD2_connect_Text;;
    UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    RecyclerView BlueToothSearch_list, BlueToothSearch_list2;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothAdapter bluetoothAdapter_search = null;
    BluetoothSocket bluetoothSocket = null;
    BlueToothSearch_device_Adapter blueToothSearch_device_adapter = null;
    HashMap BlueToothSearch_device_map = new HashMap();
    ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    BluetoothDevice bluetoothDevice;
    Set<BluetoothDevice> pairedDevices;
    //ArrayAdapter<String> bluetoothArrayAdapter;
    BlueToothSearch_Adapter blueToothSearch_adapter;
    ArrayList<String> deviceAddressArray, deviceNameArray, Search_deviceAddressArray, Search_deviceNameArray, BlueToothSearch_device;
    int REQUEST_ENABLE_BT = 1;
    BlueToothSearch_Thead blueToothSearch_thead;
    EditText send_commend;
    Button send_button, log_activity_button;
    private static final int MESSAGE_Connect = 101;
    BlueConnect blueConnect;
    DataFrame dataFrame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_search);

        context = this;

        //연결하는 동시에 데이터 갱신시킬 DataFrame 객체 생성
        dataFrame = new DataFrame();

        //OBD2, ECU 연결 현황
        PreferenceManager.setBoolean(context, "BlueTooth_Connect_OBD2" ,false);
        PreferenceManager.setBoolean(context, "BlueTooth_Connect_ECU" ,false);

        OBD2_connect_Text = findViewById(R.id.EML_connect_Text_boolean);
        //ECU_connect_Text = findViewById(R.id.ECU_connect_Text_boolean);

        // Get permission
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(BlueToothSearch.this, permission_list, 1);

        //Enable bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter_search = BluetoothAdapter.getDefaultAdapter();
        //Log.i("bluetoothAdapter_befor", "bluetoothAdapter");
        if (!bluetoothAdapter.isEnabled()&&!bluetoothAdapter_search.isEnabled()) {

            //Log.i("bluetoothAdapter", "bluetoothAdapter");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }

        title = findViewById(R.id.title);
        BlueToothSearch_list = findViewById(R.id.BlueToothSearch_list);
        BlueToothSearch_list2 = findViewById(R.id.BlueToothSearch_list2);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context);
        linearLayoutManager2.setOrientation(RecyclerView.VERTICAL);
        BlueToothSearch_list.setLayoutManager(linearLayoutManager);
        BlueToothSearch_list2.setLayoutManager(linearLayoutManager2);

        blueConnect = new BlueConnect(context);
        blueConnect.sendEmptyMessage(MESSAGE_Connect);

        //Show paired devices
        deviceAddressArray = new ArrayList<>();
        deviceNameArray = new ArrayList<>();
        blueToothSearch_adapter = new BlueToothSearch_Adapter(context, deviceNameArray, deviceAddressArray, bluetoothSocket, BT_UUID, bluetoothAdapter, send_commend, send_button);
        BlueToothSearch_list.setAdapter(blueToothSearch_adapter);

        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0){
            // 페어링된 장치가 있으면 페어링된 각 장치의 이름과 주소를 가져옵니다.
            for (BluetoothDevice device : pairedDevices){
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                deviceNameArray.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
                blueToothSearch_adapter.notifyDataSetChanged();
            }
        }

        //검색한 블루투스 디바이스 객체를 구한다.
        bluetoothAdapter_search.startDiscovery();
        Search_deviceNameArray = new ArrayList<>();
        Search_deviceAddressArray = new ArrayList<>();
        blueToothSearch_device_adapter = new BlueToothSearch_device_Adapter(context, BlueToothSearch_device_map, Search_deviceNameArray, Search_deviceAddressArray, bluetoothSocket, BT_UUID,
                bluetoothAdapter_search, send_commend, send_button);
        BlueToothSearch_list2.setAdapter(blueToothSearch_device_adapter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, intentFilter);

        log_activity_button = findViewById(R.id.log_activity_button);
        log_activity_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataFrame.Scaner_connect){
                    Intent intent = new Intent(BlueToothSearch.this, LogActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(context, "먼저 스캐너를 연결해 주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action){
                case BluetoothDevice.ACTION_FOUND:
                    //검색한 블루투스 디바이스 객체를 구한다.
                    BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //데이터 저장
                    String bluetoothDevice_name = bluetoothDevice.getName();
                    String bluetoothDevice_Address = bluetoothDevice.getAddress();
                    // 이미 추가한 기기는 추가 안하려고 함.
                        for (int i=0; i<deviceAddressArray.size(); i++){
                            if (BlueToothSearch_device_map.get(bluetoothDevice_Address) == null&&bluetoothDevice_name != null&&bluetoothDevice_Address != null){
                                Search_deviceNameArray.add(bluetoothDevice_name);
                                Search_deviceAddressArray.add(bluetoothDevice_Address);
                                BlueToothSearch_device_map.put(bluetoothDevice_name, bluetoothDevice_name);
                                BlueToothSearch_device_map.put(bluetoothDevice_Address, bluetoothDevice_Address);
                            }
                        }
                    blueToothSearch_device_adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    class BlueConnect extends Handler {
        Context context;

        public BlueConnect(Context context){
            this.context = context;
        }

        public void handleMessage(Message mgs) {
            //Log.i("Connect", "Run Connect");

            switch (mgs.what){
                case MESSAGE_Connect:
                    //OBD2 연결 여부
                    if (dataFrame.Scaner_connect) {
                        OBD2_connect_Text.setText("연결");
                        OBD2_connect_Text.setTextColor(Color.GREEN);
                        //Log.i("BlueTooth_Connect_OBD2", "true");
                    }

                    sendEmptyMessage(MESSAGE_Connect);
                    break;
            }

        }
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

}