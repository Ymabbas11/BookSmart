package com.example.booksmart;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class BookSmartApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Configure Firebase Database persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
} 