package com.example.killer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.example.killer.R;
import com.example.killer.dialogs.AddTaskDialog;
import com.example.killer.dialogs.AddRewardDialog;

/**
 * Фрагмент с вкладками для заданий и наград
 * Использует ViewPager для переключения между TasksListFragment и RewardsFragment
 */
public class TasksFragment extends Fragment {

    // UI элементы
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddTask;
    private FloatingActionButton fabAddReward;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // Инициализация UI элементов
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        fabAddTask = view.findViewById(R.id.fab_add_task);
        fabAddReward = view.findViewById(R.id.fab_add_reward);

        // Создаем адаптер для ViewPager
        TasksPagerAdapter adapter = new TasksPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new TasksListFragment(), "Задания"); // Первая вкладка
        adapter.addFragment(new RewardsFragment(), "Награды"); // Вторая вкладка
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager); // Привязываем TabLayout к ViewPager

        // Показываем соответствующую FAB кнопку для текущей вкладки
        updateFabVisibility(0);

        // Слушатель смены вкладок
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateFabVisibility(position); // Обновляем видимость FAB
            }
        });

        // Обработчики для FAB кнопок
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());
        fabAddReward.setOnClickListener(v -> showAddRewardDialog());

        return view;
    }

    /**
     * Обновление видимости FAB кнопок в зависимости от выбранной вкладки
     */
    private void updateFabVisibility(int position) {
        if (position == 0) { // Вкладка "Задания"
            fabAddTask.setVisibility(View.VISIBLE);
            fabAddReward.setVisibility(View.GONE);
        } else { // Вкладка "Награды"
            fabAddTask.setVisibility(View.GONE);
            fabAddReward.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Показ диалога добавления задания
     */
    private void showAddTaskDialog() {
        AddTaskDialog dialog = new AddTaskDialog();
        dialog.setOnTaskAddedListener(() -> {
            // Обновляем список заданий после добавления
            TasksListFragment tasksListFragment = (TasksListFragment)
                    ((TasksPagerAdapter) viewPager.getAdapter()).getItem(0);
            if (tasksListFragment != null) {
                tasksListFragment.loadTasks(); // Перезагружаем задания
            }
        });
        dialog.show(getParentFragmentManager(), "AddTaskDialog");
    }

    /**
     * Показ диалога добавления награды
     */
    private void showAddRewardDialog() {
        AddRewardDialog dialog = new AddRewardDialog();
        dialog.setOnRewardAddedListener(() -> {
            // Обновляем список наград после добавления
            RewardsFragment rewardsFragment = (RewardsFragment)
                    ((TasksPagerAdapter) viewPager.getAdapter()).getItem(1);
            if (rewardsFragment != null) {
                rewardsFragment.loadRewards(); // Перезагружаем награды
            }
        });
        dialog.show(getParentFragmentManager(), "AddRewardDialog");
    }

    /**
     * Адаптер для ViewPager с фрагментами заданий и наград
     */
    public static class TasksPagerAdapter extends FragmentPagerAdapter {
        private final java.util.List<Fragment> fragmentList = new java.util.ArrayList<>();
        private final java.util.List<String> fragmentTitleList = new java.util.ArrayList<>();

        public TasksPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT); // Оптимизация памяти
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position); // Получаем фрагмент по позиции
        }

        @Override
        public int getCount() {
            return fragmentList.size(); // Количество вкладок
        }

        /**
         * Добавление фрагмента и его заголовка
         */
        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position); // Заголовок для TabLayout
        }
    }
}