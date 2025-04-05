package com.somnium.app;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Handles touch events to hide keyboard when user taps outside an input field
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText || v instanceof TextInputEditText) {
                float rawX = event.getRawX();
                float rawY = event.getRawY();
                
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                
                // Check if touch was outside the EditText's bounds
                if (rawX < location[0] || rawX > location[0] + v.getWidth() || 
                    rawY < location[1] || rawY > location[1] + v.getHeight()) {
                    
                    v.clearFocus();
                    hideKeyboard(v);
                    return true;
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
    
    /**
     * Hide the keyboard
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
} 