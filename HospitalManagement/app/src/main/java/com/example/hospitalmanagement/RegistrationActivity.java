package com.example.hospitalmanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RegistrationActivity extends AppCompatActivity {
    private TextView back;
    Button patientReg,doctorReg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(RegistrationActivity.this,LogInActivity.class);
                startActivity(intent);
            }
        });

        patientReg=findViewById(R.id.patientReg);
        patientReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(RegistrationActivity.this,PatientRegistrationActivity.class);
                startActivity(intent);
            }
        });

        doctorReg=findViewById(R.id.doctorReg);
        doctorReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(RegistrationActivity.this,DoctorRegistrationActivity.class);
                startActivity(intent);
            }
        });
    }
}