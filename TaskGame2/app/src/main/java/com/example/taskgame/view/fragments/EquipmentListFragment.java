package com.example.taskgame.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.taskgame.databinding.FragmentEquipmentListBinding;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.view.adapters.EquipmentListAdapter;

import java.util.ArrayList;
import java.util.List;

public class EquipmentListFragment extends ListFragment {
    private EquipmentListAdapter adapter;
    private static final String ARG_PARAM = "param";
    private ArrayList<Equipment> mEquipment;
    private FragmentEquipmentListBinding binding;
    public interface OnBuyClickListener {
        void onBuyClicked(Equipment equipment);
    }
    private OnBuyClickListener buyClickListener;
    public static EquipmentListFragment newInstance(ArrayList<Equipment> equipment){
        EquipmentListFragment fragment = new EquipmentListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM, equipment);
        fragment.setArguments(args);
        return fragment;
    }
    public void setOnBuyClickListener(OnBuyClickListener listener) {
        this.buyClickListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEquipmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEquipment = getArguments().getParcelableArrayList(ARG_PARAM);
            adapter = new EquipmentListAdapter(getActivity(), mEquipment);
            setListAdapter(adapter);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (adapter != null) {
            adapter.setOnBuyClickListener(item -> {
                Log.d("EquipmentListFragment", "Forwarding buy click for: " + item.getName());
                if (buyClickListener != null) {
                    buyClickListener.onBuyClicked(item);
                } else {
                    Log.e("EquipmentListFragment", "buyClickListener is NULL!");
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    public void updateEquipment(List<Equipment> newEquipment) {
        if (adapter != null) {
            adapter.updateData(newEquipment);
        }
    }

}