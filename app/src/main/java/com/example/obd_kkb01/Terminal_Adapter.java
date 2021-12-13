package com.example.obd_kkb01;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Terminal_Adapter extends RecyclerView.Adapter<Terminal_Adapter.ViewHolder> {

    Context context;
    ArrayList<String> send_data_list;
    ArrayList<Integer> Type_list; // 0: sender, 1: receiver
    int Type;

    public Terminal_Adapter(Context context, ArrayList<String> send_data_list, ArrayList<Integer> Type_list){
        this.context = context;
        this.send_data_list = send_data_list;
        this.Type_list = Type_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.terminal_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(Terminal_Adapter.ViewHolder holder, int position) {

        if (Type_list.get(position) == 0){
            holder.data_who.setText("Sender: ");
            holder.Terminal_data_item.setText(send_data_list.get(position));
        }else if (Type_list.get(position) == 1){

            holder.data_who.setText("Receiver: ");
            holder.Terminal_data_item.setText(send_data_list.get(position));
        }

    }

    @Override
    public int getItemCount() {
        return send_data_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView data_who, Terminal_data_item;

        public ViewHolder(View itemView) {
            super(itemView);

            Terminal_data_item = itemView.findViewById(R.id.Terminal_data_item);
            data_who = itemView.findViewById(R.id.data_who);
        }
    }


}
