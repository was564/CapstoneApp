package com.example.logintest001;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class MemberActivity1 extends AppCompatActivity {

    private DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_member1);

        EditText nameText = (EditText)findViewById(R.id.name);
        EditText birthdayText = (EditText)findViewById(R.id.birthday) ;
        Button buttonDatePicker = findViewById(R.id.btn_date_pick);
        Button buttonGoN = findViewById(R.id.faceshot);

        birthdayText.setEnabled(false);
        buttonGoN.setEnabled(true);

        buttonGoN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonGoN.setEnabled(false);
                if(nameText.getText().length() == 0 || birthdayText.getText().length() == 0){
                    Toast.makeText(MemberActivity1.this, "개인 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    buttonGoN.setEnabled(true);
                    return;
                }

                Intent intent = new Intent(MemberActivity1.this, FaceCameraActivity.class);
                String name = nameText.getText().toString();
                String birthday = birthdayText.getText().toString();
                intent.putExtra("name", name);
                intent.putExtra("birth", birthday);
                intent.putExtra("mode", 1);
                startActivity(intent);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //오늘 날짜(년,월,일) 변수에 담기
                Calendar calendar = Calendar.getInstance();
                int pYear = calendar.get(Calendar.YEAR); //년
                int pMonth = calendar.get(Calendar.MONTH);//월
                int pDay = calendar.get(Calendar.DAY_OF_MONTH);//일
                datePickerDialog = new DatePickerDialog(MemberActivity1.this, AlertDialog.THEME_HOLO_LIGHT,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                //1월은 0부터 시작하기 때문에 +1을 해준다.
                                month = month + 1;
                                String date =
                                        String.format("%04d", year) + "-" +
                                        String.format("%02d", month) + "-" +
                                        String.format("%02d", day);
                                birthdayText.setText(date);
                            }
                        }, pYear, pMonth, pDay);
                datePickerDialog.show();
            } //onClick
        });
    }
}