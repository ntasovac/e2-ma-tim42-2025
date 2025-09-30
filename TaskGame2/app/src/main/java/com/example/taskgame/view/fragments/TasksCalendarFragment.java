package com.example.taskgame.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taskgame.data.repositories.BossRepository;
import com.example.taskgame.data.repositories.TaskRepository;
import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.databinding.FragmentTasksCalendarBinding;
import com.example.taskgame.domain.models.Boss;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.domain.models.Task;
import com.example.taskgame.domain.models.User;
import com.example.taskgame.view.adapters.TaskRowAdapter;
import com.example.taskgame.view.viewmodels.TaskViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TasksCalendarFragment extends Fragment {

    public interface TaskActionListener {
        void openTaskUpdate(Task task);
    }

    private TaskActionListener listener;

    private FragmentTasksCalendarBinding binding;
    private TaskViewModel vm;

    private final TaskRowAdapter adapter = new TaskRowAdapter(new TaskRowAdapter.Listener() {
        @Override
        public void onTaskClicked(Task t) {
            if (listener != null) listener.openTaskUpdate(t);
        }

        @Override public void onChangeStatus(Task t, String newStatus) {
            t.setStatus(newStatus);

            if(newStatus == "DONE"){
                UserRepository userRepo = new UserRepository();
                String loggedUserId = SessionManager.getInstance().getUserId();

                userRepo.getUserById(loggedUserId, new UserRepository.GetUserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        if (user != null) {
                            SessionManager.getInstance().setUserData(user);
                            SessionManager.getInstance().increaseUserXP(t.getTotalXp());
                            if(SessionManager.getInstance().CheckLvLUp()){
                                SessionManager.getInstance().setUserXP(0);
                                SessionManager.getInstance().increaseUserLevelandPP();

                                BossRepository bossRepo = new BossRepository();
                                Boss newBoss = new Boss(SessionManager.getInstance().getUserId(), SessionManager.getInstance().getUserLevel());


                                vm.calculateSuccessRatio(new TaskViewModel.RatioResult() {
                                    @Override
                                    public void ok(double ratio) {
                                        System.out.println("✅ Success ratio: " + ratio);
                                        newBoss.setAttackChance(ratio);

                                        // ⬇️ Boss se sada kreira tek kada imamo ratio
                                        bossRepo.create(newBoss, new BossRepository.CreateCallback() {
                                            @Override
                                            public void onSuccess(String id) {
                                                System.out.println("✅ Boss created: " + newBoss.getName() +
                                                        " with ratio: " + newBoss.getAttackChance());
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                System.err.println("❌ Failed to create boss: " + e.getMessage());
                                            }
                                        });
                                    }

                                    @Override
                                    public void error(Exception e) {
                                        System.err.println("❌ Failed to calculate success ratio: " + e.getMessage());
                                    }
                                });

                            }
                            // update user
                            SessionManager session = SessionManager.getInstance();
                            userRepo.updateUserFields(
                                    session.getUserId(),
                                    session.getUserLevel(),
                                    session.getUserXP(),
                                    session.getUserPP(),
                                    new UserRepository.RegisterCallback() {
                                        @Override
                                        public void onSuccess() {
                                            System.out.println("✅ User XP/PP/Level updated!");
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            System.err.println("❌ Failed to update user: " + e.getMessage());
                                        }
                                    }
                            );
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
            }

            vm.updateTask(t, new TaskViewModel.VoidResult() {
                @Override public void ok() { }
                @Override public void error(Exception e) { }
            });
        }

        @Override public void onDelete(Task t) {
            vm.deleteTask(t.getId(), new TaskViewModel.VoidResult() {
                @Override public void ok() { }
                @Override public void error(Exception e) { }
            });
        }
    });



    private final Calendar selected = Calendar.getInstance();
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof TaskActionListener) {
            listener = (TaskActionListener) getParentFragment();
        }
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle b) {
        binding = FragmentTasksCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        vm = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        vm.startObserving();

        binding.rvCalendar.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCalendar.setAdapter(adapter);

        binding.tvPicked.setText(fmt.format(selected.getTime()));
        binding.btnPickDate.setOnClickListener(view -> openDatePicker());

        vm.getTasks().observe(getViewLifecycleOwner(), this::applyForSelectedDay);
    }

    private void openDatePicker() {
        MaterialDatePicker<Long> dp = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Izaberi datum").build();
        dp.addOnPositiveButtonClickListener(ms -> {
            selected.setTimeInMillis(ms);
            zeroTime(selected);
            binding.tvPicked.setText(fmt.format(selected.getTime()));
            List<Task> all = vm.getTasks().getValue();
            if (all != null) applyForSelectedDay(all);
        });
        dp.show(getParentFragmentManager(), "cal_pick");
    }

    private void applyForSelectedDay(List<Task> all) {
        Calendar start = (Calendar) selected.clone(); zeroTime(start);
        Calendar end = (Calendar) start.clone(); end.add(Calendar.DAY_OF_YEAR, 1);

        List<Task> day = new ArrayList<>();
        long startMs = start.getTimeInMillis();
        long endMs = end.getTimeInMillis();

        String userId = SessionManager.getInstance().getUserId();
        long userLongId = Long.parseLong(userId);
        for (Task t : all) {
            if(t.getUserId() != userLongId) {
                continue;
            }
            if ("ONE_TIME".equals(t.getFrequency())) {
                if (t.getStartDateUtc() >= startMs && t.getStartDateUtc() < endMs) {
                    day.add(t);
                }
            } else {
                if (t.getStartDateUtc() <= startMs &&
                        (t.getEndDateUtc() == null || t.getEndDateUtc() >= startMs)) {
                    day.add(t);
                }
            }
        }
        Collections.sort(day, Comparator.comparingInt(Task::getTimeOfDayMin));
        adapter.submitList(day);
    }

    private static void zeroTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
