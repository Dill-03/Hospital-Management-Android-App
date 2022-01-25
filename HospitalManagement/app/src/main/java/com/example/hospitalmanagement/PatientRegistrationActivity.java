package com.example.hospitalmanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PatientRegistrationActivity extends AppCompatActivity {
    private TextView regPageQuestion;
    private TextInputEditText RegistrationFullname,RegistrationIdNumber,RegistrationPhoneNumber,RegistrationEmail,RegistrationPassword;
    private Button RegistrationButton;
    private CircleImageView profileImage;
    private Uri resultUri;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;
    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_registration);

        regPageQuestion=findViewById(R.id.regPageQuestion);
        regPageQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(PatientRegistrationActivity.this,LogInActivity.class);
                startActivity(intent);
            }
        });



        RegistrationFullname=findViewById(R.id.RegistrationFullname);
        RegistrationIdNumber=findViewById(R.id.RegistrationIdNumber);
        RegistrationPhoneNumber=findViewById(R.id.RegistrationPhoneNumber);
        RegistrationEmail=findViewById(R.id.RegistrationEmail);
        RegistrationPassword=findViewById(R.id.RegistrationPassword);
        RegistrationButton=findViewById(R.id.RegistrationButton);
        profileImage=findViewById(R.id.profileImage);

        loader=new ProgressDialog(this);
        mAuth= FirebaseAuth.getInstance();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });


        RegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email= RegistrationEmail.getText().toString().trim();
                final String password= RegistrationPassword.getText().toString().trim();
                final String fullName= RegistrationFullname.getText().toString().trim();
                final String idNumber= RegistrationIdNumber.getText().toString().trim();
                final String phoneNumber= RegistrationPhoneNumber.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    RegistrationEmail.setText("Email is required");
                    return;
                }
                //------------------//
                if (TextUtils.isEmpty(password)){
                    RegistrationPassword.setText("Password is required");
                    return;
                }
                //---------------------//
                if (TextUtils.isEmpty(fullName)){
                    RegistrationFullname.setText("Full name is required");
                    return;
                }
                //---------------------------------//
                if (TextUtils.isEmpty(idNumber)){
                    RegistrationIdNumber.setText("ID number is required");
                    return;
                }
                //-------------------------//
                if (TextUtils.isEmpty(phoneNumber)){
                    RegistrationPhoneNumber.setText("Phone number is required");
                    return;
                }
                if (resultUri==null){
                    Toast.makeText(PatientRegistrationActivity.this, "Image required", Toast.LENGTH_SHORT).show();
                }
                else {
                    loader.setMessage("Registration on process....");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                String error=task.getException().toString();
                                Toast.makeText(PatientRegistrationActivity.this, "Error occurred" + error, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String currentUserId=mAuth.getCurrentUser().getUid();
                                userDatabaseRef= FirebaseDatabase.getInstance().getReference()
                                        .child("users").child(currentUserId);
                                HashMap userInfo= new HashMap();
                                userInfo.put("name",fullName);
                                userInfo.put("email",email);
                                userInfo.put("idnumber",idNumber);
                                userInfo.put("phonenumber",phoneNumber);
                                userInfo.put("type","patient");

                                userDatabaseRef.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(PatientRegistrationActivity.this, "Details save successfully..", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(PatientRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                        finish();
                                        loader.dismiss();
                                    }
                                });
                                if (resultUri !=null){
                                    final StorageReference filepath=
                                            FirebaseStorage.getInstance().getReference()
                                            .child("profile pictures")
                                                    .child(currentUserId);
                                    Bitmap bitmap=null;
                                    try {
                                        bitmap= MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG,20,byteArrayOutputStream);
                                    byte[] data=byteArrayOutputStream.toByteArray();
                                    UploadTask uploadTask= filepath.putBytes(data);
                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            finish();
                                            return;
                                        }
                                    });
                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            if (taskSnapshot.getMetadata()!=null){
                                                Task<Uri> result=taskSnapshot.getStorage().getDownloadUrl();
                                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String imageUrl=uri.toString();
                                                        Map newImageMap= new HashMap();
                                                        newImageMap.put("profilepictureurl",imageUrl);

                                                        userDatabaseRef.updateChildren(newImageMap).addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {
                                                               if (task.isSuccessful()){
                                                                   Toast.makeText(PatientRegistrationActivity.this, "Registration is successful...", Toast.LENGTH_SHORT).show();
                                                               }else {
                                                                   Toast.makeText(PatientRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                               }
                                                            }
                                                        });
                                                        finish();
                                                    }
                                                });
                                            }

                                        }
                                    });
                                    Intent intent= new Intent(PatientRegistrationActivity.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    loader.dismiss();
                                }
                            }

                        }
                    });

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data!=null){
            resultUri=data.getData();
            profileImage.setImageURI(resultUri);
        }
    }
}