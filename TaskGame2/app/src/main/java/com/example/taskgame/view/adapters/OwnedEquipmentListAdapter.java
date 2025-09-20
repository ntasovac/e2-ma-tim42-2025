package com.example.taskgame.view.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskgame.R;
import com.example.taskgame.domain.models.Equipment;

import java.util.ArrayList;
import java.util.List;

public class OwnedEquipmentListAdapter extends RecyclerView.Adapter<OwnedEquipmentListAdapter.EquipmentViewHolder> {

    private ArrayList<Equipment> aEquipment;
    private OnActivateClickListener activateListener;

    public interface OnActivateClickListener {
        void onActivateClicked(int position, Equipment equipment);
    }

    public OwnedEquipmentListAdapter(ArrayList<Equipment> equipment) {
        this.aEquipment = equipment;
    }
    public OwnedEquipmentListAdapter(ArrayList<Equipment> equipment, OnActivateClickListener listener) {
        this.aEquipment = equipment;
        this.activateListener = listener;
    }

    public void setOnActivateClickListener(OnActivateClickListener listener) {
        this.activateListener = listener;
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.owned_equipment_card, parent, false);
        return new EquipmentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        Equipment equipment = aEquipment.get(position);

        holder.equipmentName.setText(equipment.getName());
        holder.equipmentDescription.setText(equipment.getDescription());
        holder.equipmentType.setText(equipment.getType().toString());
        switch (equipment.getName()) {
            case "One-time potion I":
                holder.imageView.setImageResource(R.drawable.blue_potion);
                break;
            case "One-time potion II":
                holder.imageView.setImageResource(R.drawable.green_potion);
                break;
            case "Permanent potion I":
                holder.imageView.setImageResource(R.drawable.yellow_potion);
                break;
            case "Permanent potion II":
                holder.imageView.setImageResource(R.drawable.red_potion);
                break;
            case "Gloves":
                holder.imageView.setImageResource(R.drawable.gloves);
                break;
            case "Shield":
                holder.imageView.setImageResource(R.drawable.shield);
                break;
            case "Boots":
                holder.imageView.setImageResource(R.drawable.boots);
                break;
            default:
                throw new IllegalStateException("Invalid equipment: " + equipment.getName());
        }

        if (equipment.isActivated()) {
            holder.activateButton.setEnabled(false);
            holder.activateButton.setText("Activated");
        } else {
            holder.activateButton.setEnabled(true);
            holder.activateButton.setText("Activate");
        }

        holder.itemView.setOnClickListener(v -> {
            Log.i("TaskGame", "Clicked: " + equipment.getName());
            Toast.makeText(v.getContext(), "Clicked: " + equipment.getName(), Toast.LENGTH_SHORT).show();
        });

        holder.activateButton.setOnClickListener(v -> {
            Log.d("EquipmentAdapter", "Activate button clicked for: " + equipment.getName());
            if (activateListener != null) {
                activateListener.onActivateClicked(holder.getAdapterPosition(), equipment);
                holder.activateButton.setEnabled(false);
                holder.activateButton.setText("Activated");
            } else {
                Log.e("EquipmentAdapter", "activateListener is NULL!");
            }
        });
    }

    @Override
    public int getItemCount() {
        return aEquipment != null ? aEquipment.size() : 0;
    }

    public void updateData(List<Equipment> newEquipment) {
        aEquipment.clear();
        aEquipment.addAll(newEquipment);
        notifyDataSetChanged();
    }
    public void updateActivated(int index) {
        if (index >= 0 && index < aEquipment.size()) {
            aEquipment.get(index).setActivated(true);
            notifyItemChanged(index);
        }
    }

    static class EquipmentViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView equipmentName;
        TextView equipmentType;
        TextView equipmentDescription;
        Button activateButton;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.owned_equipment_image);
            equipmentName = itemView.findViewById(R.id.owned_equipment_name);
            equipmentType = itemView.findViewById(R.id.owned_equipment_type);
            equipmentDescription = itemView.findViewById(R.id.owned_equipment_description);
            activateButton = itemView.findViewById(R.id.activate_button);
        }
    }
}
