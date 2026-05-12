package com.example.killer.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;

import com.example.killer.R;
import com.example.killer.database.AppDatabase;
import com.example.killer.database.CalendarDao;
import com.example.killer.models.Calendar;

import java.util.List;

public class CalendarProfileAdapter extends RecyclerView.Adapter<CalendarProfileAdapter.ViewHolder> {

    private List<Calendar> calendars;
    private Context context;
    private String currentUserId;

    public CalendarProfileAdapter(List<Calendar> calendars, Context context) {
        this.calendars = calendars;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Calendar calendar = calendars.get(position);

        holder.tvCalendarName.setText(calendar.getName());
        holder.viewCalendarColor.setBackgroundColor(android.graphics.Color.parseColor(calendar.getColor()));

        // Загружаем количество событий
        loadEventCount(calendar.getId(), holder.tvEventCount);

        // Показываем кнопку "По умолчанию" если это не текущий календарь по умолчанию
        holder.btnSetDefault.setVisibility(calendar.isDefault() ? View.GONE : View.VISIBLE);

        holder.btnSetDefault.setOnClickListener(v -> {
            setAsDefaultCalendar(calendar, position);
        });

        holder.btnDeleteCalendar.setOnClickListener(v -> {
            deleteCalendar(calendar, position);
        });
    }

    private void loadEventCount(int calendarId, TextView tvEventCount) {
        new AsyncTask<Integer, Void, Integer>() {
            @Override
            protected Integer doInBackground(Integer... ids) {
                return AppDatabase.getDatabase(context).calendarEventDao()
                        .getCalendarEvents(String.valueOf(ids[0])).size();
            }

            @Override
            protected void onPostExecute(Integer count) {
                tvEventCount.setText("Событий: " + count);
            }
        }.execute(calendarId);
    }

    private void setAsDefaultCalendar(Calendar calendar, int position) {
        new AsyncTask<Calendar, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Calendar... calendars) {
                try {
                    CalendarDao dao = AppDatabase.getDatabase(context).calendarDao();
                    dao.clearDefault(currentUserId);
                    dao.setAsDefault(calendars[0].getId());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    // Обновляем список
                    notifyDataSetChanged();
                    Toast.makeText(context, "Календарь установлен по умолчанию", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(calendar);
    }

    private void deleteCalendar(Calendar calendar, int position) {
        new AsyncTask<Calendar, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Calendar... calendars) {
                try {
                    AppDatabase.getDatabase(context).calendarDao().delete(calendars[0]);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    calendars.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, calendars.size());
                    Toast.makeText(context, "Календарь удален", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(calendar);
    }

    @Override
    public int getItemCount() {
        return calendars.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View viewCalendarColor;
        TextView tvCalendarName;
        TextView tvEventCount;
        Button btnSetDefault;
        ImageButton btnDeleteCalendar;

        public ViewHolder(View itemView) {
            super(itemView);
            viewCalendarColor = itemView.findViewById(R.id.view_calendar_color);
            tvCalendarName = itemView.findViewById(R.id.tv_calendar_name);
            tvEventCount = itemView.findViewById(R.id.tv_event_count);
            btnSetDefault = itemView.findViewById(R.id.btn_set_default);
            btnDeleteCalendar = itemView.findViewById(R.id.btn_delete_calendar);
        }
    }
}