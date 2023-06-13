package com.labinot.bajrami.video_meeting_app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.labinot.bajrami.video_meeting_app.R;
import com.labinot.bajrami.video_meeting_app.utilities.Constants;
import com.labinot.bajrami.video_meeting_app.utilities.PreferenceManager;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ImageView img_back;
    private TextView txt_SignIn;
    private EditText inputFirstName, inputLastName, inputEmail, inputPassword, inputConfirmPassword;
    private MaterialButton signUp_btn;
    private ProgressBar  signUpProgressBar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initView();

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();

            }
        });

        txt_SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();

            }
        });

        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (inputFirstName.getText().toString().trim().isEmpty()) {

                    Toast.makeText(SignUpActivity.this, "Enter First Name", Toast.LENGTH_SHORT).show();

                } else if (inputLastName.getText().toString().trim().isEmpty()) {

                    Toast.makeText(SignUpActivity.this, "Enter Last Name", Toast.LENGTH_SHORT).show();

                } else if (inputEmail.getText().toString().trim().isEmpty()) {

                    Toast.makeText(SignUpActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();

                } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()) {

                    Toast.makeText(SignUpActivity.this, "Enter Valid Email Address", Toast.LENGTH_SHORT).show();

                } else if (inputPassword.getText().toString().trim().isEmpty()) {

                    Toast.makeText(SignUpActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();

                } else if (inputConfirmPassword.getText().toString().trim().isEmpty()) {

                    Toast.makeText(SignUpActivity.this, "Confirm your password", Toast.LENGTH_SHORT).show();

                } else if (!inputPassword.getText().toString().equals(inputConfirmPassword.getText().toString())) {

                    Toast.makeText(SignUpActivity.this, "Password & Confirm password must be the same", Toast.LENGTH_SHORT).show();

                }else{

                    signUp();
                }

            }
        });

    }

    private void initView() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        img_back = findViewById(R.id.imageBack);
        txt_SignIn = findViewById(R.id.textSignIn);
        inputFirstName = findViewById(R.id.inputFirstName);
        inputLastName = findViewById(R.id.inputLastName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        signUp_btn = findViewById(R.id.buttonSignUp);
        signUpProgressBar = findViewById(R.id.signUpProgress);

    }

    private void signUp() {

        signUp_btn.setVisibility(View.INVISIBLE);
        signUpProgressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String,Object> user = new HashMap<>();
        user.put(Constants.FIRST_NAME,inputFirstName.getText().toString());
        user.put(Constants.LAST_NAME,inputLastName.getText().toString());
        user.put(Constants.EMAIL,inputEmail.getText().toString());
        user.put(Constants.PASSWORD,inputPassword.getText().toString());

        database.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                preferenceManager.putString(Constants.ID,documentReference.getId());
                preferenceManager.putString(Constants.FIRST_NAME,inputFirstName.getText().toString());
                preferenceManager.putString(Constants.LAST_NAME,inputLastName.getText().toString());
                preferenceManager.putString(Constants.EMAIL,inputEmail.getText().toString());
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                signUpProgressBar.setVisibility(View.INVISIBLE);
                signUp_btn.setVisibility(View.VISIBLE);
                Toast.makeText(SignUpActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
}