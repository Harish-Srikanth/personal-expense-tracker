package com.example.expense;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class ExpenseTrackerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}