package com.example.taskgame.view.adapters;

import static android.provider.Settings.System.getString;

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
import com.example.taskgame.data.repositories.BossRepository;
import com.example.taskgame.domain.enums.EquipmentType;
import com.example.taskgame.domain.models.Boss;
import com.example.taskgame.domain.models.Equipment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OwnedEquipmentListAdapter extends RecyclerView.Adapter<OwnedEquipmentListAdapter.EquipmentViewHolder> {

    public enum Mode { USER, FRIEND }

    private final Mode mode;
    private ArrayList<Equipment> aEquipment;
    private OnActivateClickListener activateListener;
    private OnUpgradeClickListener upgradeListener;
    private final boolean showUpgrade;
    private final BossRepository bossRepository;

    public interface OnActivateClickListener {
        void onActivateClicked(int position, Equipment equipment);
    }
    public interface OnUpgradeClickListener {
        void onUpgradeClicked(int position, Equipment equipment);
    }
    public OwnedEquipmentListAdapter(Mode mode, ArrayList<Equipment> equipment, boolean showUpgrade) {
        this.aEquipment = equipment;
        this.showUpgrade = showUpgrade;
        bossRepository = new BossRepository();
        this.mode = mode;
    }
    public OwnedEquipmentListAdapter(Mode mode, ArrayList<Equipment> equipment, boolean showUpgrade, OnActivateClickListener activateListener, OnUpgradeClickListener upgradeListener) {
        this.aEquipment = equipment;
        this.activateListener = activateListener;
        this.upgradeListener = upgradeListener;
        this.showUpgrade = showUpgrade;
        this.mode = mode;
        bossRepository = new BossRepository();
    }

    public void setOnActivateClickListener(OnActivateClickListener listener) {
        this.activateListener = listener;
    }
    public void setOnUpgradeClickListener(OnUpgradeClickListener listener) {
        this.upgradeListener = listener;
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
            case "Sword":
                holder.imageView.setImageResource(R.drawable.sword);
                break;
            case "Bow and Arrow":
                holder.imageView.setImageResource(R.drawable.bow_and_arrow);
                break;
            default:
                throw new IllegalStateException("Invalid equipment: " + equipment.getName());
        }

        if(mode == Mode.USER) {
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
            if (showUpgrade && equipment.getType() == EquipmentType.WEAPON) {
                Log.d("Upgrade: ", "visible");
                holder.upgradeButton.setVisibility(View.VISIBLE);
                if (upgradeListener != null) {
                    holder.upgradeButton.setOnClickListener(v ->
                            upgradeListener.onUpgradeClicked(position, equipment)
                    );
                }
                holder.upgradeCost.setVisibility(View.VISIBLE);
                getUpgradeCost(cost -> holder.upgradeCost.setText("Upgrade cost: " + cost));
            } else {
                Log.d("Upgrade: ", "invisible");
                holder.upgradeButton.setVisibility(View.GONE);
                holder.upgradeCost.setVisibility(View.GONE);
            }
        }else{
            holder.activateButton.setVisibility(View.GONE);
            holder.upgradeButton.setVisibility(View.GONE);
            holder.upgradeCost.setVisibility(View.GONE);
        }
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

    private void getUpgradeCost(Consumer<Integer> callback){
        bossRepository.getBoss().observeForever(boss -> {
            if (boss != null) {
                int cost = (int) Math.ceil(Math.pow(6.0 / 5.0, boss.getLevel() - 2) * 200);
                cost = (int)Math.round(0.6 * cost);
                callback.accept(cost);
            }
        });
    }

    static class EquipmentViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView equipmentName;
        TextView equipmentType;
        TextView equipmentDescription;
        Button activateButton;
        Button upgradeButton;
        TextView upgradeCost;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.owned_equipment_image);
            equipmentName = itemView.findViewById(R.id.owned_equipment_name);
            equipmentType = itemView.findViewById(R.id.owned_equipment_type);
            equipmentDescription = itemView.findViewById(R.id.owned_equipment_description);
            activateButton = itemView.findViewById(R.id.activate_button);
            upgradeButton = itemView.findViewById(R.id.upgrade_button);
            upgradeCost = itemView.findViewById(R.id.upgrade_cost);
        }
    }
}
