package com.mr_alhams.tasky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TicketDetailActivity extends AppCompatActivity {

    private TaskTicket currentTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvLocation = findViewById(R.id.tvDetailLocation);
        TextView tvStatus = findViewById(R.id.tvDetailStatus);
        Button btnInProgress = findViewById(R.id.btnMarkInProgress);
        Button btnCompleted = findViewById(R.id.btnMarkCompleted);

        currentTicket = (TaskTicket) getIntent().getSerializableExtra("EXTRA_TICKET");

        if (currentTicket != null) {
            tvTitle.setText(currentTicket.getTitle());
            tvLocation.setText("Site Location: " + currentTicket.getLocation());
            tvStatus.setText("Current Status: " + currentTicket.getStatus());
        }

        btnInProgress.setOnClickListener(v -> updateStatus("In Progress"));
        btnCompleted.setOnClickListener(v -> updateStatus("Completed"));
    }

    private void updateStatus(String newStatus) {
        if (currentTicket != null) {
            currentTicket.setStatus(newStatus);
            Intent returnIntent = new Intent();
            returnIntent.putExtra("UPDATED_TICKET", currentTicket);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }
}
