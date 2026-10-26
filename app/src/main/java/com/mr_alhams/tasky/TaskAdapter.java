package com.mr_alhams.tasky;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTicketClickListener {
        void onTicketClick(TaskTicket ticket, int position);
    }

    private final List<TaskTicket> ticketList;
    private final OnTicketClickListener listener;

    public TaskAdapter(List<TaskTicket> ticketList, OnTicketClickListener listener) {
        this.ticketList = ticketList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskTicket ticket = ticketList.get(position);
        holder.bind(ticket, listener);
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvLocation;
        private final TextView tvStatus;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(final TaskTicket ticket, final OnTicketClickListener listener) {
            tvTitle.setText(ticket.getTitle());
            tvLocation.setText("Site: " + ticket.getLocation());
            tvStatus.setText("Status: " + ticket.getStatus());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTicketClick(ticket, getBindingAdapterPosition());
                }
            });
        }
    }
}