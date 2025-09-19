package com.example.taskgame.view.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.taskgame.R;
import com.example.taskgame.domain.models.Equipment;

import java.util.ArrayList;
import java.util.List;

public class EquipmentListAdapter extends ArrayAdapter<Equipment> {
    private ArrayList<Equipment> aEquipment;

    public interface OnBuyClickListener {
        void onBuyClicked(Equipment equipment);
    }
    private OnBuyClickListener buyListener;
    public EquipmentListAdapter(Context context, ArrayList<Equipment> equipment){
        super(context, R.layout.equipment_card, equipment);
        aEquipment = equipment;

    }

    public void setOnBuyClickListener(OnBuyClickListener listener) {
        this.buyListener = listener;
    }

    @Override
    public int getCount(){
        return aEquipment.size();
    }

    @Nullable
    @Override
    public Equipment getItem(int position) {
        return aEquipment.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Equipment equipment = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.equipment_card,
                    parent, false);
        }
        LinearLayout equipmentCard = convertView.findViewById(R.id.equipment_card_item);
        ImageView imageView = convertView.findViewById(R.id.equipment_image);
        TextView equipmentName = convertView.findViewById(R.id.equipment_name);
        TextView equipmentType = convertView.findViewById(R.id.equipment_type);
        TextView equipmentDescription = convertView.findViewById(R.id.equipment_description);
        TextView equipmentPrice = convertView.findViewById(R.id.equipment_price);
        Button buyButton = convertView.findViewById(R.id.buy_button);

        if(equipment != null){
            imageView.setImageResource(equipment.getImage());
            equipmentName.setText(equipment.getName());
            equipmentDescription.setText(equipment.getDescription());
            equipmentPrice.setText(String.valueOf(equipment.getPrice()));
            equipmentType.setText(equipment.getType().toString());
            equipmentCard.setOnClickListener(v -> {
                Log.i("TaskGame", "Clicked: " + equipment.getName());
                Toast.makeText(getContext(), "Clicked: " + equipment.getName(), Toast.LENGTH_SHORT).show();
            });
        }

        buyButton.setOnClickListener(v -> {
            Log.d("EquipmentAdapter", "Buy button clicked for: " + equipment.getName());
            if (buyListener != null) {
                buyListener.onBuyClicked(equipment);
            } else {
                Log.e("EquipmentAdapter", "buyListener is NULL!");
            }
        });


        return convertView;
    }

    public void updateData(List<Equipment> newEquipment) {
        aEquipment.clear();
        aEquipment.addAll(newEquipment);
        notifyDataSetChanged();
    }
}
