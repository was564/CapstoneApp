package com.example.logintest001;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        Button buttonGoToBack = findViewById(R.id.logout);
        buttonGoToBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        Button buttonmyinfo = findViewById(R.id.myinfo);
        buttonmyinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMakeDialog();
            }
            });
        }

        private void showMakeDialog() {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.activity_login_popup);
            dialog.setCancelable(true);

            Button closeButton = dialog.findViewById(R.id.backlogin);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }


    }
