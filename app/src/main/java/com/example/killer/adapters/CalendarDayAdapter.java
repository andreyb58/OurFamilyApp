package com.example.killer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.killer.R;
import com.example.killer.fragments.CalendarFragment;
import java.util.List;

/**
 * Адаптер для отображения дней календаря в RecyclerView
 * Отвечает за отображение сетки дней месяца и обработку кликов
 */
public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.ViewHolder> {

    private List<CalendarFragment.CalendarDay> days; // Список дней для отображения
    private OnDayClickListener listener; // Слушатель кликов по дням


    public interface OnDayClickListener {
        void onDayClick(CalendarFragment.CalendarDay day);
    }

    /**
     * Конструктор адаптера
     * @param days Список дней для отображения
     * @param listener Слушатель кликов по дням
     */
    public CalendarDayAdapter(List<CalendarFragment.CalendarDay> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    /**
     * Создание нового ViewHolder при создании элемента списка
     * Вызывается системой для каждого нового элемента
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Загружаем макет одного дня календаря
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view); // Возвращаем новый ViewHolder
    }

    /**
     * Привязка данных дня к элементу списка
     * Вызывается для каждого видимого элемента при прокрутке
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CalendarFragment.CalendarDay day = days.get(position); // Получаем день по позиции

        // Если дата равна null - это пустая ячейка (день из другого месяца)
        if (day.getDate() == null) {
            holder.tvDay.setText(""); // Пустой текст
            holder.itemView.setBackgroundResource(android.R.color.transparent); // Прозрачный фон
        } else {
            holder.tvDay.setText(String.valueOf(day.getDayNumber()));

            if (day.isToday()) {
                holder.itemView.setBackgroundResource(R.drawable.bg_today);
                holder.tvDay.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
            } else if (day.isSelected()) {
                holder.itemView.setBackgroundResource(R.drawable.bg_selected_day);
                holder.tvDay.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
            } else {
                holder.itemView.setBackgroundResource(android.R.color.transparent);
                holder.tvDay.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
            }
        }

        // Обработчик клика по дню
        holder.itemView.setOnClickListener(v -> {
            // Вызываем слушатель только если день существует
            if (listener != null && day.getDate() != null) {
                listener.onDayClick(day);
            }
        });
    }


    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_day); // Находим TextView по ID
        }
    }
}