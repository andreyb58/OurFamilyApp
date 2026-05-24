package com.example.killer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.killer.R;
import com.example.killer.adapters.CalendarDayAdapter;
import com.example.killer.adapters.CalendarEventAdapter;
import com.example.killer.api.ApiClient;
import com.example.killer.api.ApiService;
import com.example.killer.api.ApiUtils;
import com.example.killer.auth.AuthManager;
import com.example.killer.dialogs.AddEventDialog;
import com.example.killer.models.CalendarEvent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Единый семейный календарь.
 * События хранятся на сервере и видны всем участникам семьи.
 */
public class CalendarFragment extends Fragment {

    private TextView tvMonthYear;
    private TextView tvError;
    private Button btnPrevMonth, btnNextMonth, btnToday;
    private RecyclerView calendarGrid;
    private RecyclerView eventsList;
    private View noCalendarView;
    private View calendarControls;

    private CalendarDayAdapter dayAdapter;
    private CalendarEventAdapter eventAdapter;

    private final List<CalendarDay> days = new ArrayList<>();
    private final List<CalendarEvent> events = new ArrayList<>();

    private java.util.Calendar currentDate;
    private AuthManager authManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authManager = AuthManager.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        if (currentDate == null) {
            currentDate = java.util.Calendar.getInstance();
        }

        initViews(view);
        setupCalendarGrid();
        checkFamilyAndLoad();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем события при возврате на вкладку
        if (authManager.isLoggedIn()) {
            loadEventsForSelectedDay();
        }
    }

    private void initViews(View view) {
        tvMonthYear      = view.findViewById(R.id.tv_month_year);
        tvError          = view.findViewById(R.id.tv_error);
        btnPrevMonth     = view.findViewById(R.id.btn_prev_month);
        btnNextMonth     = view.findViewById(R.id.btn_next_month);
        btnToday         = view.findViewById(R.id.btn_today);
        calendarGrid     = view.findViewById(R.id.calendar_grid);
        eventsList       = view.findViewById(R.id.events_list);
        noCalendarView   = view.findViewById(R.id.no_calendar_view);
        calendarControls = view.findViewById(R.id.calendar_controls);

        btnPrevMonth.setOnClickListener(v -> {
            currentDate.add(java.util.Calendar.MONTH, -1);
            updateCalendarView();
        });
        btnNextMonth.setOnClickListener(v -> {
            currentDate.add(java.util.Calendar.MONTH, 1);
            updateCalendarView();
        });
        btnToday.setOnClickListener(v -> {
            currentDate = java.util.Calendar.getInstance();
            updateCalendarView();
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_add_event);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                if (!authManager.isLoggedIn()) {
                    showError("Войдите в систему, чтобы добавлять события");
                    return;
                }
                int fid = authManager.getCurrentUser() != null
                        ? authManager.getCurrentUser().getFamilyId() : 0;
                if (fid == 0) {
                    showError("Чтобы добавлять события, сначала вступите в семью");
                    return;
                }
                showAddEventDialog();
            });
        }
    }

    private void setupCalendarGrid() {
        calendarGrid.setLayoutManager(new GridLayoutManager(getContext(), 7));
        dayAdapter = new CalendarDayAdapter(days, day -> {
            if (day.getDate() != null) {
                // Обновляем только дату, не трогая время
                currentDate.set(java.util.Calendar.YEAR,
                        day.getDate().getYear() + 1900);
                currentDate.set(java.util.Calendar.MONTH,
                        day.getDate().getMonth());
                currentDate.set(java.util.Calendar.DAY_OF_MONTH,
                        day.getDate().getDate());
                generateCalendarDays();
                dayAdapter.notifyDataSetChanged();
                loadEventsForSelectedDay();
            }
        });
        calendarGrid.setAdapter(dayAdapter);

        eventsList.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new CalendarEventAdapter(events, requireContext());
        eventsList.setAdapter(eventAdapter);
    }

    /** Проверяем наличие семьи и загружаем или показываем заглушку */
    private void checkFamilyAndLoad() {
        if (!authManager.isLoggedIn()) {
            showNoCalendarView();
            return;
        }
        int fid = authManager.getCurrentUser() != null
                ? authManager.getCurrentUser().getFamilyId() : 0;
        if (fid == 0) {
            showNoCalendarView();
        } else {
            showCalendarView();
            updateCalendarView();
        }
    }

    private void showCalendarView() {
        if (noCalendarView != null)   noCalendarView.setVisibility(View.GONE);
        if (calendarControls != null) calendarControls.setVisibility(View.VISIBLE);
        if (calendarGrid != null)     calendarGrid.setVisibility(View.VISIBLE);
        if (eventsList != null)       eventsList.setVisibility(View.VISIBLE);
        if (tvMonthYear != null)      tvMonthYear.setVisibility(View.VISIBLE);
        if (btnPrevMonth != null)     btnPrevMonth.setVisibility(View.VISIBLE);
        if (btnNextMonth != null)     btnNextMonth.setVisibility(View.VISIBLE);
        if (btnToday != null)         btnToday.setVisibility(View.VISIBLE);
    }

    private void showNoCalendarView() {
        if (noCalendarView != null)   noCalendarView.setVisibility(View.VISIBLE);
        if (calendarControls != null) calendarControls.setVisibility(View.GONE);
        if (calendarGrid != null)     calendarGrid.setVisibility(View.GONE);
        if (eventsList != null)       eventsList.setVisibility(View.GONE);
        if (tvMonthYear != null)      tvMonthYear.setVisibility(View.GONE);
        if (btnPrevMonth != null)     btnPrevMonth.setVisibility(View.GONE);
        if (btnNextMonth != null)     btnNextMonth.setVisibility(View.GONE);
        if (btnToday != null)         btnToday.setVisibility(View.GONE);
    }

    private void showError(String message) {
        if (tvError != null) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(message);
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void hideError() {
        if (tvError != null) tvError.setVisibility(View.GONE);
    }

    private void updateCalendarView() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("ru"));
        tvMonthYear.setText(sdf.format(currentDate.getTime()));
        generateCalendarDays();
        dayAdapter.notifyDataSetChanged();
        loadEventsForSelectedDay();
    }

    private void generateCalendarDays() {
        days.clear();
        java.util.Calendar firstDay = (java.util.Calendar) currentDate.clone();
        firstDay.set(java.util.Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = firstDay.get(java.util.Calendar.DAY_OF_WEEK);
        if (firstDayOfWeek == java.util.Calendar.SUNDAY) firstDayOfWeek = 7;
        else firstDayOfWeek -= 1;

        for (int i = 1; i < firstDayOfWeek; i++) {
            days.add(new CalendarDay(null, false, false));
        }

        int daysInMonth = currentDate.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        java.util.Calendar today = java.util.Calendar.getInstance();
        boolean sameMonth = today.get(java.util.Calendar.MONTH) == currentDate.get(java.util.Calendar.MONTH)
                && today.get(java.util.Calendar.YEAR) == currentDate.get(java.util.Calendar.YEAR);

        for (int day = 1; day <= daysInMonth; day++) {
            java.util.Calendar dc = (java.util.Calendar) currentDate.clone();
            dc.set(java.util.Calendar.DAY_OF_MONTH, day);
            boolean isToday    = sameMonth && day == today.get(java.util.Calendar.DAY_OF_MONTH);
            boolean isSelected = day == currentDate.get(java.util.Calendar.DAY_OF_MONTH);
            days.add(new CalendarDay(dc.getTime(), isToday, isSelected, day));
        }
    }

    /** Загружает события для выбранного дня с сервера */
    private void loadEventsForSelectedDay() {
        if (!isAdded() || getContext() == null) return;
        if (!authManager.isLoggedIn()) return;
        int fid = authManager.getCurrentUser() != null
                ? authManager.getCurrentUser().getFamilyId() : 0;
        if (fid == 0) return;

        // Формируем диапазон: начало и конец выбранного дня
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

        java.util.Calendar start = (java.util.Calendar) currentDate.clone();
        start.set(java.util.Calendar.HOUR_OF_DAY, 0);
        start.set(java.util.Calendar.MINUTE, 0);
        start.set(java.util.Calendar.SECOND, 0);

        java.util.Calendar end = (java.util.Calendar) currentDate.clone();
        end.set(java.util.Calendar.HOUR_OF_DAY, 23);
        end.set(java.util.Calendar.MINUTE, 59);
        end.set(java.util.Calendar.SECOND, 59);

        String from = sdf.format(start.getTime());
        String to   = sdf.format(end.getTime());

        ApiService apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        ApiUtils.makeApiCall(
                apiService.getCalendarEvents(from, to),
                new ApiUtils.ApiCallback<Map<String, Object>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onSuccess(Map<String, Object> data) {
                        if (!isAdded()) return;
                        hideError();
                        List<Map<String, Object>> eventMaps =
                                (List<Map<String, Object>>) data.get("list");
                        events.clear();
                        if (eventMaps != null) {
                            for (Map<String, Object> m : eventMaps) {
                                CalendarEvent e = new CalendarEvent();
                                e.setTitle(ApiUtils.extractString(m.get("title")));
                                e.setDescription(ApiUtils.extractString(m.get("description")));
                                // startAt приходит как "yyyy-MM-dd HH:mm:ss.S"
                                String startAt = ApiUtils.extractString(m.get("startAt"));
                                // Берём только время HH:mm
                                if (startAt.contains(" ")) {
                                    String timePart = startAt.split(" ")[1];
                                    if (timePart.length() >= 5) {
                                        e.setTime(timePart.substring(0, 5));
                                    }
                                }
                                e.setEventDate(currentDate.getTime());
                                events.add(e);
                            }
                        }
                        if (eventAdapter != null) eventAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded()) return;
                        showError("Ошибка загрузки событий: " + error);
                    }
                });
    }

    private void showAddEventDialog() {
        AddEventDialog dialog = new AddEventDialog();
        dialog.setListener(event -> {
            // Формируем startAt из текущей даты + времени из диалога
            SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String dateStr  = dateFmt.format(currentDate.getTime());
            String timeStr  = (event.getTime() != null && !event.getTime().isEmpty())
                    ? event.getTime() : "00:00";
            // Добавляем секунды если их нет
            if (timeStr.matches("\\d{1,2}:\\d{2}")) timeStr += ":00";
            String startAt = dateStr + "T" + timeStr;

            Map<String, Object> request = new HashMap<>();
            request.put("title",       event.getTitle());
            request.put("description", event.getDescription() != null ? event.getDescription() : "");
            request.put("startAt",     startAt);

            ApiService apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
            ApiUtils.makeApiCall(
                    apiService.createCalendarEvent(request),
                    new ApiUtils.ApiCallback<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> data) {
                            if (!isAdded()) return;
                            hideError();
                            Toast.makeText(getContext(), "Событие добавлено", Toast.LENGTH_SHORT).show();
                            loadEventsForSelectedDay();
                        }

                        @Override
                        public void onError(String error) {
                            if (!isAdded()) return;
                            showError("Не удалось добавить событие: " + error);
                        }
                    });
        });
        dialog.show(getParentFragmentManager(), "AddEventDialog");
    }

    // ── CalendarDay inner class ──────────────────────────────────────────────
    public static class CalendarDay {
        private final Date date;
        private final boolean isToday;
        private final boolean isSelected;
        private final int dayNumber;

        public CalendarDay(Date date, boolean isToday, boolean isSelected) {
            this(date, isToday, isSelected, 0);
        }

        public CalendarDay(Date date, boolean isToday, boolean isSelected, int dayNumber) {
            this.date       = date;
            this.isToday    = isToday;
            this.isSelected = isSelected;
            this.dayNumber  = dayNumber;
        }

        public Date getDate()       { return date; }
        public boolean isToday()    { return isToday; }
        public boolean isSelected() { return isSelected; }
        public int getDayNumber()   { return dayNumber; }
    }
}
