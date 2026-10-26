package com.mr_alhams.tasky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final List<TaskTicket> ticketList = new ArrayList<>();
    private TaskAdapter adapter;
    private int selectedPosition = -1;

    private final ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    TaskTicket updatedTicket = (TaskTicket) result.getData().getSerializableExtra("UPDATED_TICKET");
                    if (updatedTicket != null && selectedPosition != -1) {
                        ticketList.set(selectedPosition, updatedTicket);
                        adapter.notifyItemChanged(selectedPosition);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadInitialData();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter(ticketList, (ticket, position) -> {
            selectedPosition = position;
            Intent intent = new Intent(MainActivity.this, TicketDetailActivity.class);
            intent.putExtra("EXTRA_TICKET", ticket);
            detailLauncher.launch(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadInitialData() {
        ticketList.add(new TaskTicket(1, "Fiber Node Failure", "Zone 4, Street 10", "Pending"));
        ticketList.add(new TaskTicket(2, "Substation Backup Offline", "Main Substation B", "In Progress"));
        ticketList.add(new TaskTicket(3, "Optical Splitter Re-patch", "Industrial Sector 9", "Pending"));
    }
}