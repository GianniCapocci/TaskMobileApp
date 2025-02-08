package com.example.taskapplication.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.taskapplication.database.AppDatabase;
import com.example.taskapplication.database.TaskEntity;

public class TaskContentProvider extends ContentProvider {

    private static final String AUTHORITY = "com.example.taskapplication.provider";
    private static final String TABLE_NAME = "tasks";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

    private AppDatabase database;

    @Override
    public boolean onCreate() {
        database = AppDatabase.getDatabase(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return database.taskDao().getAllTasksCursor();
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values == null) return null;

        TaskEntity task = new TaskEntity();
        task.shortName = values.getAsString("shortName");
        task.description = values.getAsString("description");
        task.startTime = values.getAsString("startTime");
        task.duration = values.getAsInteger("duration");
        task.status = values.getAsString("status");
        task.location = values.getAsString("location");

        long id = database.taskDao().insertTask(task);
        if (id > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int id = (int) ContentUris.parseId(uri);

        int rowsDeleted = database.taskDao().deleteTaskById(id);
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

@Override
public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
    if (values == null) return 0;

    int rowsUpdated = 0;
    int id = (int) ContentUris.parseId(uri);

    TaskEntity task = database.taskDao().findById(id);
    if (task != null) {
        if (values.containsKey("shortName")) task.shortName = values.getAsString("shortName");
        if (values.containsKey("description")) task.description = values.getAsString("description");
        if (values.containsKey("startTime")) task.startTime = values.getAsString("startTime");
        if (values.containsKey("duration")) task.duration = values.getAsInteger("duration");
        if (values.containsKey("status")) task.status = values.getAsString("status");
        if (values.containsKey("location")) task.location = values.getAsString("location");

        database.taskDao().updateTask(task);
        rowsUpdated = 1;
    }

    getContext().getContentResolver().notifyChange(uri, null);
    return rowsUpdated;
}

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".tasks";
    }
}
