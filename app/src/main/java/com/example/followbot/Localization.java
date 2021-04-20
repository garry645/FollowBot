package com.example.followbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Localization extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);
    }
    public void onClick(View view) {
        Button button = (Button) view;
        Intent intent;

        switch (button.getId()) {
            case R.id.buttonLearn:
                intent = new Intent(Localization.this, Learn.class);
                startActivity(intent);
                break;
            case R.id.buttonLocate:
                intent = new Intent(Localization.this, Locate.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}