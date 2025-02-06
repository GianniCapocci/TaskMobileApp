package com.example.taskapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskapplication.R;
import com.example.taskapplication.database.TaskEntity;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskEntity> taskList = new ArrayList<>();
    private final OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(TaskEntity task);
        void onTaskDelete(TaskEntity task);
    }

    public TaskAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setTaskList(List<TaskEntity> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskEntity task = taskList.get(position);
        holder.textShortName.setText(task.shortName);
        holder.textStatus.setText(task.status);

        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
        holder.btnDelete.setOnClickListener(v -> listener.onTaskDelete(task));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textShortName, textStatus;
        ImageButton btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textShortName = itemView.findViewById(R.id.textShortName);
            textStatus = itemView.findViewById(R.id.textStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
