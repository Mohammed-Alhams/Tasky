# Advanced Android Development (Java)
## Complete Lecture Course Pack: Lectures 1 to 4 & Intro to Room Database

---

### Architectural Arc of this Module
To ensure students do not learn concepts in isolation, this module uses a continuous, evolving case study: the **Field Task Dispatcher App** (an enterprise application used by field supervisors to assign, track, and update technical tickets).

* **Lecture 1:** Professional mindset, grading, rules, and architecture roadmap.
* **Lecture 2:** The app runs **in-memory** using a clean `RecyclerView` and modern `ActivityResultLauncher`.
* **Lecture 3:** The app is refactored to persist data to disk using raw **SQLite** (`SQLiteOpenHelper`, `Cursor`), exposing boilerplate and runtime risks.
* **Lecture 4:** The app is version-controlled via **Git/GitHub**, setting up branch protection and an Android `.gitignore`.
* **Intro to Room (Lec 5 Prep):** The SQLite engine is refactored to **Android Jetpack Room**, demonstrating compile-time SQL validation, object mapping, and threading rules.

---

# Lecture 1: Course Kickoff, Rules & Professional Mindset

### 1. Lecture Objectives
* Clarify course expectations, grading policies, and submission standards.
* Establish development rules: transitioning from "student scripts" to clean, industry-standard engineering.
* Set up the modern development environment.

### 2. Syllabus, Rules & Environment
* **Platform Baseline:**
    * Language: **Java 8+** (utilizing Lambdas, Streams, and modern standard libraries).
    * Minimum SDK: **API 26 (Android 8.0 Oreo)** — covers virtually all active devices while supporting modern background execution policies.
    * Target SDK: **Current API** (Android 14+).
* **Course Submission Rules:**
    * No `.zip` archives or email attachments. All assignments must be submitted via **GitHub Pull Requests**.
    * Code that fails to compile receives a grade of zero.
    * Separation of concerns: Activities and Fragments must contain **no direct database queries, network calls, or heavy business logic**.
* **Grading Distribution:**
    * Lab Quizzes & Continuous Code Reviews: 20%
    * Midterm Exam (Lectures 19–20): 25%
    * Final Project & Defense (Lectures 29–31): 35%
    * Class Participation & Code Quality: 20%

### 3. Mindset Shift: From Android 1 to Advanced Topics
Show students the evolution of Android development practices:

$$\text{Monolithic Activities} \longrightarrow \text{SQLite Helper} \longrightarrow \text{Room DB + DAOs} \longrightarrow \text{Fragments \& Jetpack Nav} \longrightarrow \text{MVVM / DI (Hilt)}$$

Explain that by the end of this course, they will build applications capable of handling offline data synchronization, background workers, and modular component design.

---

# Lecture 2: Android Core Revision & RecyclerView Masterclass

### 1. Lecture Objectives
* Revisit the Activity Lifecycle and view-binding patterns.
* Understand memory recycling: `onCreateViewHolder` vs. `onBindViewHolder`.
* Build a decoupled item interaction system using a custom interface callback.
* Implement the modern `ActivityResultLauncher` contract.

---

### 2. The Domain Model (`TaskTicket.java`)
Create package: `com.example.taskdispatcher.model`

```java
package com.example.taskdispatcher.model;

import java.io.Serializable;

public class TaskTicket implements Serializable {
    private int id;
    private String title;
    private String location;
    private String status; // "Pending", "In Progress", "Completed"

    public TaskTicket(int id, String title, String location, String status) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

---

### 3. XML Layouts

#### Item Layout: `res/layout/item_ticket.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="12dp"
    android:layout_marginVertical="6dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="2dp"
    app:strokeWidth="1dp"
    app:strokeColor="#DDDDDD">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Task Title"
            android:textColor="#212121"
            android:textSize="18sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvLocation"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:text="Location: Site Sector"
            android:textColor="#666666"
            android:textSize="14sp" />

        <TextView
            android:id="@+id/tvStatus"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="Status: Pending"
            android:textColor="#1976D2"
            android:textStyle="italic" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

#### Main Activity Layout: `res/layout/activity_main.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F5F5F5">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerViewTasks"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

#### Detail Activity Layout: `res/layout/activity_ticket_detail.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:id="@+id/tvDetailTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="22sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/tvDetailLocation"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:textSize="16sp" />

    <TextView
        android:id="@+id/tvDetailStatus"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:textSize="16sp"
        android:textStyle="italic" />

    <Button
        android:id="@+id/btnMarkInProgress"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:text="Mark as In Progress" />

    <Button
        android:id="@+id/btnMarkCompleted"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Mark as Completed" />

</LinearLayout>
```

---

### 4. Custom Adapter (`TaskAdapter.java`)
Create package: `com.example.taskdispatcher.adapter`

Emphasize to students:
* `onCreateViewHolder`: Inflates XML and allocates view pointers **only** for visible rows + buffers.
* `onBindViewHolder`: Only maps data. Never instantiate heavy objects or bind complex logic here.
* The `OnTicketClickListener` pattern decouples the Adapter from Android navigation frameworks.

```java
package com.example.taskdispatcher.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskdispatcher.R;
import com.example.taskdispatcher.model.TaskTicket;
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
```

---

### 5. Controller Activities

#### `MainActivity.java`
Uses the modern `ActivityResultLauncher` instead of the deprecated `startActivityForResult`.

```java
package com.example.taskdispatcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskdispatcher.adapter.TaskAdapter;
import com.example.taskdispatcher.model.TaskTicket;
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
```

#### `TicketDetailActivity.java`
```java
package com.example.taskdispatcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.taskdispatcher.model.TaskTicket;

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
```

---

# Lecture 3: Persistent Storage via SQLite (The Foundation & The Pain)

### 1. Lecture Objectives
* Understand how relational databases work directly inside Android.
* Implement the Android `SQLiteOpenHelper` interface: `onCreate` and `onUpgrade`.
* Learn the mechanics of `ContentValues` and `Cursor`.
* Identify architectural flaws and security/maintainability risks in raw SQLite.

---

### 2. Implementation: `DatabaseHelper.java`
Create package: `com.example.taskdispatcher.database`

```java
package com.example.taskdispatcher.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.taskdispatcher.model.TaskTicket;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dispatcher_tasks.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "tasks";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_LOCATION = "location";
    public static final String COL_STATUS = "status";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_LOCATION + " TEXT, "
                + COL_STATUS + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert Record
    public long insertTicket(TaskTicket ticket) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, ticket.getTitle());
        values.put(COL_LOCATION, ticket.getLocation());
        values.put(COL_STATUS, ticket.getStatus());
        
        long insertedId = db.insert(TABLE_NAME, null, values);
        db.close();
        return insertedId;
    }

    // Fetch All Records
    public List<TaskTicket> getAllTickets() {
        List<TaskTicket> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " DESC", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS));

                list.add(new TaskTicket(id, title, location, status));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }

    // Update Status
    public int updateTicketStatus(int id, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, newStatus);

        int rowsAffected = db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected;
    }
}
```

---

### 3. Integrating SQLite into `MainActivity.java`
Replace the static `ArrayList` with queries to the database:

```java
// Inside MainActivity.java:
private DatabaseHelper dbHelper;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    dbHelper = new DatabaseHelper(this);

    // Seed dummy data if database is empty
    if (dbHelper.getAllTickets().isEmpty()) {
        dbHelper.insertTicket(new TaskTicket(0, "Fiber Node Failure", "Zone 4, Street 10", "Pending"));
        dbHelper.insertTicket(new TaskTicket(0, "Substation Backup Offline", "Main Substation B", "In Progress"));
    }

    ticketList.clear();
    ticketList.addAll(dbHelper.getAllTickets());

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
```

Inside the `detailLauncher` callback:
```java
if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
    TaskTicket updatedTicket = (TaskTicket) result.getData().getSerializableExtra("UPDATED_TICKET");
    if (updatedTicket != null && selectedPosition != -1) {
        // Persist change to SQLite
        dbHelper.updateTicketStatus(updatedTicket.getId(), updatedTicket.getStatus());
        
        // Update local UI list
        ticketList.set(selectedPosition, updatedTicket);
        adapter.notifyItemChanged(selectedPosition);
    }
}
```

---

### 4. Instructor's Critical Evaluation: The Pitfalls of Raw SQLite
Guide a classroom critique of the code written above:

1. **No Compile-Time SQL Verification:** If you write `"SELEC * FORM " + TABLE_NAME`, the compiler won't flag it. The app compiles fine, but crashes in production at runtime.
2. **Boilerplate Mapping:** Writing `cursor.getString(cursor.getColumnIndexOrThrow(...))` hundreds of times across 15 models introduces human error and clutter.
3. **Threading Risk:** `getWritableDatabase()` and raw queries run synchronously on the UI thread by default. If the table grows to 10,000 records, the app drops frames and triggers an **ANR (Application Not Responding)** dialog.
4. **Complex Schema Migrations:** Writing raw string alterations in `onUpgrade` without automated checks is a frequent source of database corruption errors.

---

# Lecture 4: Professional Git & GitHub for Android Engineers

### 1. Lecture Objectives
* Configure a standardized Android `.gitignore` file.
* Prevent build artifacts and secrets from being committed.
* Master branch isolation: `main`, `develop`, and `feature/*`.
* Prepare a branch for the upcoming Room database refactoring.

---

### 2. Android-Specific `.gitignore` Configuration
Explain that committing compiled outputs (`.apk`, `.aar`, `.class`) or machine-specific paths creates merge conflicts and bloats repository size.

Create `.gitignore` in the project root:
```gitignore
# Built application files
*.apk
*.aar
*.ap_
*.aab

# Files for the ART/Dalvik VM
*.dex

# Java class files
*.class

# Generated files
bin/
gen/
out/
build/
app/build/

# Gradle files
.gradle/
buildOutputCleanup/

# Android Studio and IntelliJ files
.idea/
*.iml
*.ipr
*.iws

# OS-specific files
.DS_Store
Thumbs.db

# Sensitive local paths and API keys (CRITICAL)
local.properties
keystore.jks
*.keystore
google-services.json
```

---

### 3. Git Command Line Terminal Walkthrough
Run the following commands in the Android Studio integrated terminal:

```bash
# 1. Initialize Git repository
git init

# 2. Stage verified files
git add .

# 3. Create initial baseline commit
git commit -m "feat: complete SQLite task dispatcher app"

# 4. Link to GitHub remote (e.g., student assignment repo)
git remote add origin https://github.com/your-username/TaskDispatcher-Java.git

# 5. Set main branch and push
git branch -M main
git push -u origin main

# 6. BRANCHING STRATEGY: Create a feature branch for Lecture 5
git checkout -b feature/room-database-migration
```

Instruct students: **All modifications for Room will be made on this branch.** We will not merge into `main` until our tests compile and run properly.

---

# Introduction to Room Database (Through Official Documentation)

According to the official [Android Jetpack Room Documentation](https://developer.android.com/training/data-storage/room):

> *"The Room persistence library provides an abstraction layer over SQLite to allow fluent database access while harnessing the full power of SQLite. In particular, Room provides compile-time verification of SQL queries, convenience annotations that minimize repetitive boilerplate code, and streamlined database migration paths."*

```
                 +-------------------------------------------------------+
                 |                     AppDatabase                       |
                 |  - Extends RoomDatabase                               |
                 |  - Holds singleton instance                           |
                 |  - Serves as the main access point to persisted data  |
                 +---------------------------+---------------------------+
                                             |
                                             v
                 +-------------------------------------------------------+
                 |                      TaskDao                          |
                 |  - Annotated with @Dao                                |
                 |  - Contains methods for Insert, Update, Delete, Query |
                 +---------------------------+---------------------------+
                                             |
                                             v
                 +-------------------------------------------------------+
                 |                     TaskEntity                        |
                 |  - Annotated with @Entity(tableName = "tasks")       |
                 |  - Represents a table in the database                 |
                 +-------------------------------------------------------+
```

---

### 1. Project Dependencies (`app/build.gradle`)
Ensure Java annotation processing is configured:

```groovy
dependencies {
    def room_version = "2.6.1"

    implementation "androidx.room:room-runtime:$room_version"
    annotationProcessor "androidx.room:room-compiler:$room_version"
    
    // Testing helpers (for later lectures)
    testImplementation "androidx.room:room-testing:$room_version"
}
```

---

### 2. Refactoring SQLite into Room Components

#### Component 1: The Entity (`TaskEntity.java`)
Create package: `com.example.taskdispatcher.data.local.entity`

```java
package com.example.taskdispatcher.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "tasks")
public class TaskEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "location")
    private String location;

    @ColumnInfo(name = "status")
    private String status;

    public TaskEntity(String title, String location, String status) {
        this.title = title;
        this.location = location;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

---

#### Component 2: The Data Access Object (`TaskDao.java`)
Create package: `com.example.taskdispatcher.data.local.dao`

```java
package com.example.taskdispatcher.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.taskdispatcher.data.local.entity.TaskEntity;
import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insert(TaskEntity task);

    @Update
    int update(TaskEntity task);

    @Delete
    void delete(TaskEntity task);

    // Room validates this SQL statement at COMPILE TIME!
    // Try misspelling 'tasks' or column names to show students the compiler error.
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    List<TaskEntity> getAllTasks();

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    TaskEntity getTaskById(int taskId);

    @Query("UPDATE tasks SET status = :newStatus WHERE id = :taskId")
    int updateStatus(int taskId, String newStatus);
}
```

---

#### Component 3: The Room Database (`AppDatabase.java`)
Create package: `com.example.taskdispatcher.data.local`

Emphasize:
1. `abstract class` extending `RoomDatabase`.
2. **Volatile Singleton:** Prevents multiple database instances from opening simultaneously, which would cause lockups.

```java
package com.example.taskdispatcher.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.taskdispatcher.data.local.dao.TaskDao;
import com.example.taskdispatcher.data.local.entity.TaskEntity;

@Database(entities = {TaskEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TaskDao taskDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "task_dispatcher_room.db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
```

---

### 3. The Threading Rule & Integration Demo

Show students what happens if we attempt to run this on the main thread:
```java
// CODE CAUSES A CRASH:
TaskEntity task = AppDatabase.getInstance(this).taskDao().getTaskById(1);
```

**The Runtime Crash:**
`java.lang.IllegalStateException: Cannot access database on the main thread since it may potentially lock the UI for a long period of time.`

#### Safe Execution via `ExecutorService`:

```java
package com.example.taskdispatcher;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.taskdispatcher.data.local.AppDatabase;
import com.example.taskdispatcher.data.local.entity.TaskEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoomDemoActivity extends AppCompatActivity {

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        // Run query asynchronously off the UI thread
        databaseExecutor.execute(() -> {
            // 1. Check if database is empty; if so, populate dummy tasks
            List<TaskEntity> existing = db.taskDao().getAllTasks();
            if (existing.isEmpty()) {
                db.taskDao().insert(new TaskEntity("Generator Maintenance", "Sector 3", "Pending"));
                db.taskDao().insert(new TaskEntity("Solar Array Inverter Fault", "Sector 8", "In Progress"));
            }

            // 2. Fetch records in background
            List<TaskEntity> tasks = db.taskDao().getAllTasks();

            // 3. Post results safely back to the Main (UI) Thread
            mainThreadHandler.post(() -> {
                Toast.makeText(RoomDemoActivity.this, 
                        "Fetched " + tasks.size() + " tasks via Room safely!", 
                        Toast.LENGTH_SHORT).show();
                // Pass tasks list to RecyclerView adapter here
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown(); // Prevent memory leaks
    }
}
```

---

# Comparison Matrix: SQLite vs. Room Database

| Feature | Raw SQLite (`SQLiteOpenHelper`) | Android Jetpack Room |
| :--- | :--- | :--- |
| **SQL Syntax Verification** | **Runtime only.** Errors cause crashes in production. | **Compile-time.** Invalid column names or syntax errors fail the project build. |
| **Object Mapping** | Manual using `Cursor` & index getters. | **Automated.** Directly returns Java objects and lists. |
| **Main-Thread Safety** | Allows synchronous UI blocking by default. | **Strictly enforced.** Crashes automatically if queried on the UI thread. |
| **Architecture Integration** | Zero native support for reactive components. | Natively integrates with **LiveData**, **RxJava**, and **Paging 3**. |
| **Schema Migrations** | Manual `ALTER TABLE` SQL strings; high risk of data corruption. | Structured migrations using `Migration` classes with automated verification. |

---

# Lab Assignment & Review Questions

### Student Review Questions
1. In `RecyclerView`, why does `onCreateViewHolder` execute far fewer times than `onBindViewHolder` when scrolling through 1,000 items?
2. Why is passing an anonymous inner-class listener to every row inside `onBindViewHolder` considered an anti-pattern?
3. What is the fundamental role of `ContentValues` in SQLite, and which Room component makes it unnecessary?
4. Why does Room strictly throw an `IllegalStateException` if a database query is executed on Android's main thread?

### Lab Homework: Room Migration Challenge
1. Check out the `feature/room-database-migration` branch created in Lecture 4.
2. Completely replace `DatabaseHelper.java` in the Task Dispatcher app with the `AppDatabase`, `TaskDao`, and `TaskEntity` classes.
3. Update `TicketDetailActivity` to execute status updates via `db.taskDao().updateStatus(...)` inside an `ExecutorService`.
4. Commit your changes with the message:
   ```bash
   git commit -m "refactor: replace SQLiteOpenHelper with Room database persistence"
   ```
5. Push the branch to your GitHub repository and open a **Pull Request** targeting `main` for grading.