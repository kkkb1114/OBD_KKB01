package com.example.obd_kkb01;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class Self_Diagnosis_Adater extends RecyclerView.Adapter<Self_Diagnosis_Adater.ViewHolder> {

    Context context;
    ArrayList<String> explanation_code_list, error_code_list;
    Self_Diagnosis_data self_diagnosis_data;

    public Self_Diagnosis_Adater(Context context, ArrayList<String> explanation_code_list, ArrayList<String> error_code_list){
        this.context = context;
        this.explanation_code_list = explanation_code_list;
        this.error_code_list = error_code_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.self_diagnosis_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       holder.explanation.setText(explanation_code_list.get(position));
       holder.error_code.setText(error_code_list.get(position));
    }

    @Override
    public int getItemCount() {
        return explanation_code_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView error_code, explanation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }
}
