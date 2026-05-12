package com.example.killer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.killer.R;
import com.example.killer.database.AppDatabase;
import com.example.killer.database.CalendarDao;
import com.example.killer.models.Calendar;
// УДАЛИТЬ: import com.google.firebase.auth.FirebaseAuth;
import com.example.killer.auth.AuthManager; // ДОБАВИТЬ
import java.util.ArrayList;
import java.util.List;

public class CreateCalendarDialog extends DialogFragment {

    private OnCalendarCreatedListener listener;
    private EditText etCalendarName;
    private RecyclerView recyclerColors;
    private ColorAdapter colorAdapter;
    private String selectedColor = "#6200EE";
    private AuthManager authManager; // ДОБАВИТЬ

    private List<String> colors = new ArrayList<>();

    public interface OnCalendarCreatedListener {
        void onCalendarCreated();
    }

    public void setOnCalendarCreatedListener(OnCalendarCreatedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_create_calendar, null);

        authManager = AuthManager.getInstance(requireContext()); // ДОБАВИТЬ
        initColors();
        initViews(view);

        builder.setView(view);
        return builder.create();
    }
    private void initColors() {
        colors.clear();
        colors.add("#FF5252");
        colors.add("#FF9800");
        colors.add("#FFEB3B");
        colors.add("#4CAF50");
        colors.add("#2196F3");
        colors.add("#9C27B0");
        colors.add("#795548");
        colors.add("#607D8B");
        colors.add("#FF4081");
        colors.add("#00BCD4");
        colors.add("#8BC34A");
        colors.add("#FF5722");
    }

    private void initViews(View view) {
        etCalendarName = view.findViewById(R.id.et_calendar_name);
        recyclerColors = view.findViewById(R.id.recycler_colors);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnCreate = view.findViewById(R.id.btn_create);

        colorAdapter = new ColorAdapter(colors, selectedColor, color -> {
            selectedColor = color;
        });

        recyclerColors.setLayoutManager(new GridLayoutManager(getContext(), 6));
        recyclerColors.setAdapter(colorAdapter);

        btnCancel.setOnClickListener(v -> dismiss());

        btnCreate.setOnClickListener(v -> {
            String calendarName = etCalendarName.getText().toString().trim();

            if (calendarName.isEmpty()) {
                etCalendarName.setError("Введите название календаря");
                return;
            }

            createCalendar(calendarName, selectedColor);
        });
    }

    private void createCalendar(String name, String color) {
        // ЗАМЕНИТЬ FirebaseAuth на AuthManager
        String userId = String.valueOf(authManager.getCurrentUser().getId());
        Calendar calendar = new Calendar(name, color, userId);

        new CreateCalendarTask().execute(calendar);
    }

    private class CreateCalendarTask extends AsyncTask<Calendar, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Calendar... calendars) {
            try {
                CalendarDao calendarDao = AppDatabase.getDatabase(requireContext()).calendarDao();
                List<Calendar> existingCalendars = calendarDao.getUserCalendars(calendars[0].getOwnerId());

                for (Calendar existing : existingCalendars) {
                    if (existing.getName().equalsIgnoreCase(calendars[0].getName())) {
                        return null;
                    }
                }

                if (existingCalendars.isEmpty()) {
                    calendars[0].setDefault(true);
                }

                calendarDao.insert(calendars[0]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result == null) {
                Toast.makeText(getContext(), "Календарь с таким названием уже существует", Toast.LENGTH_SHORT).show();
                return;
            }

            if (result) {
                Toast.makeText(getContext(), "Календарь создан успешно", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onCalendarCreated();
                }
                dismiss();
            } else {
                Toast.makeText(getContext(), "Ошибка создания календаря", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {
        private List<String> colors;
        private String selectedColor;
        private OnColorSelectedListener listener;

        interface OnColorSelectedListener {
            void onColorSelected(String color);
        }

        ColorAdapter(List<String> colors, String selectedColor, OnColorSelectedListener listener) {
            this.colors = colors;
            this.selectedColor = selectedColor;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_color_picker, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String color = colors.get(position);
            holder.viewColor.setBackgroundColor(Color.parseColor(color));

            if (color.equals(selectedColor)) {
                holder.viewSelected.setVisibility(View.VISIBLE);
            } else {
                holder.viewSelected.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                selectedColor = color;
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onColorSelected(color);
                }
            });
        }

        @Override
        public int getItemCount() {
            return colors.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View viewColor;
            View viewSelected;

            ViewHolder(View itemView) {
                super(itemView);
                viewColor = itemView.findViewById(R.id.view_color);
                viewSelected = itemView.findViewById(R.id.view_selected);
            }
        }
    }
}