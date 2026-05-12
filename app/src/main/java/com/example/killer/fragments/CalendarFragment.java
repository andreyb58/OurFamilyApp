package com.example.killer.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.killer.R;
import com.example.killer.adapters.CalendarDayAdapter;
import com.example.killer.adapters.CalendarEventAdapter;
import com.example.killer.database.AppDatabase;
import com.example.killer.database.CalendarDao;
import com.example.killer.database.CalendarEventDao;
import com.example.killer.dialogs.AddEventDialog;
import com.example.killer.models.Calendar;
import com.example.killer.models.CalendarEvent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.killer.auth.AuthManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private TextView tvMonthYear;
    private Button btnPrevMonth, btnNextMonth, btnToday;
    private Spinner spinnerCalendars;
    private RecyclerView calendarGrid;
    private RecyclerView eventsList;
    private View noCalendarView;
    private Button btnCreateCalendar;

    private CalendarDayAdapter dayAdapter;
    private CalendarEventAdapter eventAdapter;

    private List<CalendarDay> days = new ArrayList<>();
    private List<CalendarEvent> events = new ArrayList<>();
    private List<Calendar> userCalendars = new ArrayList<>();

    private java.util.Calendar currentDate;
    private String selectedCalendarId;
    private CalendarDao calendarDao;
    private CalendarEventDao eventDao;
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

        calendarDao = AppDatabase.getDatabase(requireContext()).calendarDao();
        eventDao = AppDatabase.getDatabase(requireContext()).calendarEventDao();

        currentDate = java.util.Calendar.getInstance();

        initViews(view);
        setupCalendarGrid();
        loadUserCalendars();

        return view;
    }

    private void initViews(View view) {
        tvMonthYear = view.findViewById(R.id.tv_month_year);
        btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        btnNextMonth = view.findViewById(R.id.btn_next_month);
        btnToday = view.findViewById(R.id.btn_today);
        spinnerCalendars = view.findViewById(R.id.spinner_calendars);
        calendarGrid = view.findViewById(R.id.calendar_grid);
        eventsList = view.findViewById(R.id.events_list);
        noCalendarView = view.findViewById(R.id.no_calendar_view);
        btnCreateCalendar = view.findViewById(R.id.btn_create_calendar);

        FloatingActionButton fabAddEvent = view.findViewById(R.id.fab_add_event);

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

        btnCreateCalendar.setOnClickListener(v -> {
            showCreateCalendarDialog();
        });

        fabAddEvent.setOnClickListener(v -> {
            if (authManager != null && authManager.isLoggedIn() && selectedCalendarId != null) {
                showAddEventDialog();
            } else {
                Toast.makeText(getContext(), "Сначала создайте календарь", Toast.LENGTH_SHORT).show();
            }
        });

        spinnerCalendars.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position - 1 < userCalendars.size()) {
                    selectedCalendarId = String.valueOf(userCalendars.get(position - 1).getId());
                    loadEventsForSelectedDay();
                    showCalendarView();
                } else {
                    selectedCalendarId = null;
                    events.clear();
                    if (eventAdapter != null) {
                        eventAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCalendarId = null;
            }
        });
    }

    private void setupCalendarGrid() {
        calendarGrid.setLayoutManager(new GridLayoutManager(getContext(), 7));
        dayAdapter = new CalendarDayAdapter(days, new CalendarDayAdapter.OnDayClickListener() {
            @Override
            public void onDayClick(CalendarDay day) {
                if (day.getDate() != null) {
                    currentDate.setTime(day.getDate());
                    updateCalendarView();
                    loadEventsForSelectedDay();
                }
            }
        });
        calendarGrid.setAdapter(dayAdapter);

        eventsList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        eventAdapter = new CalendarEventAdapter(events, requireContext());
        eventsList.setAdapter(eventAdapter);
    }

    private void loadUserCalendars() {
        new AsyncTask<Void, Void, List<Calendar>>() {
            @Override
            protected List<Calendar> doInBackground(Void... voids) {
                try {
                    if (authManager == null) {
                        authManager = AuthManager.getInstance(requireContext());
                    }

                    if (!authManager.isLoggedIn()) {
                        return new ArrayList<>();
                    }

                    String userId = String.valueOf(authManager.getCurrentUser().getId());
                    return calendarDao.getUserCalendars(userId);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<Calendar> calendars) {
                userCalendars.clear();
                if (calendars != null) {
                    userCalendars.addAll(calendars);
                }

                List<String> calendarNames = new ArrayList<>();
                calendarNames.add("Выберите календарь");
                for (Calendar calendar : userCalendars) {
                    calendarNames.add(calendar.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        calendarNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCalendars.setAdapter(adapter);

                if (!userCalendars.isEmpty()) {
                    spinnerCalendars.setSelection(1);
                    selectedCalendarId = String.valueOf(userCalendars.get(0).getId());
                    showCalendarView();
                    updateCalendarView();
                } else {
                    showNoCalendarView();
                }
            }
        }.execute();
    }

    private void showCalendarView() {
        noCalendarView.setVisibility(View.GONE);
        calendarGrid.setVisibility(View.VISIBLE);
        eventsList.setVisibility(View.VISIBLE);
        tvMonthYear.setVisibility(View.VISIBLE);
        btnPrevMonth.setVisibility(View.VISIBLE);
        btnNextMonth.setVisibility(View.VISIBLE);
        btnToday.setVisibility(View.VISIBLE);
        spinnerCalendars.setVisibility(View.VISIBLE);
    }

    private void showNoCalendarView() {
        noCalendarView.setVisibility(View.VISIBLE);
        calendarGrid.setVisibility(View.GONE);
        eventsList.setVisibility(View.GONE);
        tvMonthYear.setVisibility(View.GONE);
        btnPrevMonth.setVisibility(View.GONE);
        btnNextMonth.setVisibility(View.GONE);
        btnToday.setVisibility(View.GONE);
        spinnerCalendars.setVisibility(View.GONE);
    }

    private void showCreateCalendarDialog() {
        com.example.killer.dialogs.CreateCalendarDialog dialog = new com.example.killer.dialogs.CreateCalendarDialog();
        dialog.setOnCalendarCreatedListener(() -> {
            loadUserCalendars();
            Toast.makeText(getContext(), "Календарь создан", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getParentFragmentManager(), "CreateCalendarDialog");
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
        if (firstDayOfWeek == java.util.Calendar.SUNDAY) {
            firstDayOfWeek = 7;
        } else {
            firstDayOfWeek -= 1;
        }

        for (int i = 1; i < firstDayOfWeek; i++) {
            days.add(new CalendarDay(null, false, false));
        }

        int daysInMonth = currentDate.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

        java.util.Calendar today = java.util.Calendar.getInstance();
        boolean isTodaySameMonth = today.get(java.util.Calendar.MONTH) == currentDate.get(java.util.Calendar.MONTH) &&
                today.get(java.util.Calendar.YEAR) == currentDate.get(java.util.Calendar.YEAR);

        for (int day = 1; day <= daysInMonth; day++) {
            java.util.Calendar dayCalendar = (java.util.Calendar) currentDate.clone();
            dayCalendar.set(java.util.Calendar.DAY_OF_MONTH, day);

            boolean isToday = isTodaySameMonth && day == today.get(java.util.Calendar.DAY_OF_MONTH);
            boolean isSelected = day == currentDate.get(java.util.Calendar.DAY_OF_MONTH);

            days.add(new CalendarDay(dayCalendar.getTime(), isToday, isSelected, day));
        }
    }

    private void loadEventsForSelectedDay() {
        if (selectedCalendarId == null || eventAdapter == null) {
            if (events != null && eventAdapter != null) {
                events.clear();
                eventAdapter.notifyDataSetChanged();
            }
            return;
        }

        new AsyncTask<Void, Void, List<CalendarEvent>>() {
            @Override
            protected List<CalendarEvent> doInBackground(Void... voids) {
                try {
                    return eventDao.getEventsByDate(selectedCalendarId, currentDate.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<CalendarEvent> calendarEvents) {
                if (events != null && eventAdapter != null) {
                    events.clear();
                    if (calendarEvents != null) {
                        events.addAll(calendarEvents);
                    }
                    eventAdapter.notifyDataSetChanged();

                    if (calendarEvents == null || calendarEvents.isEmpty()) {
                        Toast.makeText(getContext(), "На этот день нет событий", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }.execute();
    }

    private void showAddEventDialog() {
        if (userCalendars.isEmpty()) {
            Toast.makeText(getContext(), "Сначала создайте календарь", Toast.LENGTH_SHORT).show();
            return;
        }

        AddEventDialog dialog = new AddEventDialog();
        dialog.setListener(new AddEventDialog.OnEventAddedListener() {
            @Override
            public void onEventAdded(CalendarEvent event) {
                event.setEventDate(currentDate.getTime());
                event.setCalendarId(selectedCalendarId);

                new AsyncTask<CalendarEvent, Void, Void>() {
                    @Override
                    protected Void doInBackground(CalendarEvent... events) {
                        eventDao.insert(events[0]);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Toast.makeText(getContext(), "Событие добавлено", Toast.LENGTH_SHORT).show();
                        loadEventsForSelectedDay();
                    }
                }.execute(event);
            }
        });
        dialog.show(getParentFragmentManager(), "AddEventDialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        events.clear();
        userCalendars.clear();
        days.clear();
    }

    public static class CalendarDay {
        private Date date;
        private boolean isToday;
        private boolean isSelected;
        private int dayNumber;

        public CalendarDay(Date date, boolean isToday, boolean isSelected) {
            this.date = date;
            this.isToday = isToday;
            this.isSelected = isSelected;
        }

        public CalendarDay(Date date, boolean isToday, boolean isSelected, int dayNumber) {
            this.date = date;
            this.isToday = isToday;
            this.isSelected = isSelected;
            this.dayNumber = dayNumber;
        }

        public Date getDate() { return date; }
        public boolean isToday() { return isToday; }
        public boolean isSelected() { return isSelected; }
        public int getDayNumber() { return dayNumber; }
    }
}