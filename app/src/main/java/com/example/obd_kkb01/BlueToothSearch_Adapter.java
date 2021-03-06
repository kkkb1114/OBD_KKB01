package com.example.obd_kkb01;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import static java.lang.Thread.sleep;

public class BlueToothSearch_Adapter extends RecyclerView.Adapter<BlueToothSearch_Adapter.ViewHolder> {

    private static final int RESULT_OK = 1;
    Context context;
    ArrayList<String> Bluetooth_Name_List;
    ArrayList<String> Bluetooth_Address_List;
    BluetoothSocket bluetoothSocket;
    UUID BT_UUID;
    BluetoothDevice device;
    BluetoothAdapter bluetoothAdapter;
    BlueToothSearch_Thead blueToothSearch_thead;
    boolean flag, first_answer;
    TextView ECU, OBD2;

    public BlueToothSearch_Adapter(Context context, ArrayList<String> Bluetooth_Name_List, ArrayList<String> Bluetooth_Address_List, BluetoothSocket bluetoothSocket, UUID BT_UUID, BluetoothAdapter bluetoothAdapter
            , TextView OBD2, TextView ECU){
        this.context = context;
        this.Bluetooth_Name_List = Bluetooth_Name_List;
        this.Bluetooth_Address_List = Bluetooth_Address_List;
        this.bluetoothSocket = bluetoothSocket;;
        this.bluetoothAdapter = bluetoothAdapter;
        this.BT_UUID = BT_UUID;
        this.OBD2 = OBD2;
        this.ECU = ECU;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.bluetoothsearch_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // ??????????????? ?????? ?????? device????????? ??????????????? ????????? onBindViewHolder?????? ?????? ?????? ???????????? ??????
        holder.BlueToothSearch_list_item_Name.setText(Bluetooth_Name_List.get(position));
        holder.BlueToothSearch_list_item_Address.setText(Bluetooth_Address_List.get(position));
        holder.BlueToothSearch_list_item_Name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ?????? Toast??? ?????? ?????? ????????? ????????? ?????? ?????? ??????.
                Toast.makeText(context, "OBD2 ?????? ????????????.", Toast.LENGTH_SHORT).show();

                // create & connect socket
                try {
                    device = bluetoothAdapter.getRemoteDevice(Bluetooth_Address_List.get(position));
                    //Log.i("Bluetooth_Address", Bluetooth_Address_List.get(position));
                    //Log.i("item click_bluetoothSocketCreate", "nice");
                    bluetoothSocket = createBluetoothSocket(device);
                    /*try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    bluetoothSocket.connect();
                    flag = true;
                } catch (IOException e) {
                    Toast.makeText(context, "OBD2 ?????? ?????????????????????.", Toast.LENGTH_LONG).show();
                    flag = false;
                    //Log.i("item click_bluetoothSocketCreate", "fail");
                    e.printStackTrace();
                }

                // ????????? ?????? ???????????? ?????? ????????? ????????? ?????? ????????? ??????
                if(flag){
                    Toast.makeText(context, "OBD2 ?????? ???????????????.", Toast.LENGTH_LONG).show();
                    first_answer = true;
                    //Log.i("blueToothSearch_thead", "blueToothSearch_thead");
                    blueToothSearch_thead = new BlueToothSearch_Thead(context, bluetoothSocket);
                    blueToothSearch_thead.start();
                    //?????? ????????? ?????? ????????? ?????? ????????? ?????? ?????? ?????????
                    PreferenceManager.setBoolean(context, "BlueTooth_Connect", true);
                    PreferenceManager.setBoolean(context, "first_answer", false);
                    while (first_answer){
                        if (PreferenceManager.getBoolean(context, "first_answer")) {
                            blueToothSearch_thead.write("010D\r");
                            first_answer = false;
                        }
                    }

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return Bluetooth_Name_List.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView BlueToothSearch_list_item_Name;
        TextView BlueToothSearch_list_item_Address;

        public ViewHolder(View itemView) {
            super(itemView);

            BlueToothSearch_list_item_Name = itemView.findViewById(R.id.BlueToothSearch_list_item_Name);
            BlueToothSearch_list_item_Address = itemView.findViewById(R.id.BlueToothSearch_list_item_Address);
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        try {
            final Method method = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            //Log.i("createBluetoothSocket_UUID_1", BT_UUID.toString());
            return (BluetoothSocket) method.invoke(device, BT_UUID);
        } catch (Exception e) {
            Log.e("MainActivity", "Bluetooth create fail!!",e);
        }
        //Log.i("createBluetoothSocket_UUID_2", BT_UUID.toString());
        return device.createRfcommSocketToServiceRecord(BT_UUID);
    }

}
