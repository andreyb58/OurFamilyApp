package com.example.killer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;

import com.example.killer.R;
import com.example.killer.database.AppDatabase;
import com.example.killer.models.CalendarEvent;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Адаптер для отображения списка событий в календаре
 * Показывает события на выбранный день с возможностью удаления
 */
public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.ViewHolder> {

    private List<CalendarEvent> events; // Список событий для отображения
    private Context context; // Контекст для доступа к ресурсам

    /**
     * Конструктор адаптера
     * @param events Список событий календаря
     * @param context Контекст приложения
     */
    public CalendarEventAdapter(List<CalendarEvent> events, Context context) {
        this.events = events;
        this.context = context;
    }

    /**
     * Создание нового ViewHolder для элемента списка событий
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Загружаем макет одного события
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view); // Возвращаем новый ViewHolder
    }

    /**
     * Привязка данных события к элементу списка
     * Настраивает отображение информации о событии
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CalendarEvent event = events.get(position); // Получаем событие по позиции

        // Устанавливаем данные события в элементы интерфейса
        holder.tvTitle.setText(event.getTitle()); // Название события
        holder.tvDescription.setText(event.getDescription()); // Описание
        holder.tvTime.setText(event.getTime()); // Время события
        holder.tvChild.setText("Для: " + event.getAssignedTo()); // Для кого событие

        // Форматируем и отображаем дату события
        if (event.getEventDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(event.getEventDate())); // Форматированная дата
            holder.tvDate.setVisibility(View.VISIBLE); // Показываем дату
        } else {
            holder.tvDate.setVisibility(View.GONE); // Скрываем если даты нет
        }

        // Обработчик кнопки удаления события
        holder.btnDelete.setOnClickListener(v -> {
            // Удаляем событие из списка
            events.remove(position);
            notifyItemRemoved(position); // Уведомляем адаптер об удалении
            notifyItemRangeChanged(position, events.size()); // Обновляем позиции

            // Удаляем событие из базы данных в фоновом потоке
            new Thread(() -> {
                AppDatabase.getDatabase(context).calendarEventDao().delete(event);
            }).start(); // Запускаем поток

            // Показываем сообщение об успешном удалении
            Toast.makeText(context, "Событие удалено", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Возвращает количество событий в списке
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder для элемента списка событий
     * Хранит ссылки на все View элементы макета события
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Элементы интерфейса одного события
        TextView tvTitle; // Заголовок события
        TextView tvDescription; // Описание события
        TextView tvTime; // Время события
        TextView tvChild; // Для кого событие
        TextView tvDate; // Дата события
        ImageButton btnDelete; // Кнопка удаления

        public ViewHolder(View itemView) {
            super(itemView);
            // Находим все View элементы по их ID
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvChild = itemView.findViewById(R.id.tv_child);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}