package com.example.myassignment5;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

public class AuditLogManager extends SQLiteOpenHelper {
    private static final String DB_NAME = "AuditLogs.db";
    private static final int DB_VERSION = 1;

    public AuditLogManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE logs (_id INTEGER PRIMARY KEY AUTOINCREMENT, prompt TEXT, response TEXT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS logs");
        onCreate(db);
    }

    public void saveEntry(String prompt, String response) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("prompt", prompt);
        values.put("response", response);
        db.insert("logs", null, values);
        db.close();
    }
}
