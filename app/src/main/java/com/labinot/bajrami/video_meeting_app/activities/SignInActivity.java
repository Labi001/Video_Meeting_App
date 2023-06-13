package com.labinot.bajrami.video_meeting_app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.labinot.bajrami.video_meeting_app.utilities.Constants;
import com.labinot.bajrami.video_meeting_app.R;
import com.labinot.bajrami.video_meeting_app.utilities.PreferenceManager;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private TextView txtSignUp;
    private FirebaseFirestore database;
    private EditText inputEmail,inputPassword;
    private MaterialButton btn_SignIn;
    private ProgressBar signInProgressBar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initViews();

        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();

        }

        findViewById(R.id.textSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class));

            }
        });

        btn_SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if(inputEmail.getText().toString().trim().isEmpty()){

                    Toast.makeText(SignInActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){

                    Toast.makeText(SignInActivity.this, "Your Email is Invalid", Toast.LENGTH_SHORT).show();
                }else if(inputPassword.getText().toString().trim().isEmpty()){

                    Toast.makeText(SignInActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                }else{
                    
                    signIn();
                }

            }
        });



    }

    private void initViews() {

        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        txtSignUp = findViewById(R.id.textSignUp);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        signInProgressBar = findViewById(R.id.signInnProgressBar);
        btn_SignIn = findViewById(R.id.buttonSignIn);
    }

    private void signIn() {

        btn_SignIn.setVisibility(View.INVISIBLE);
        signInProgressBar.setVisibility(View.VISIBLE);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.EMAIL,inputEmail.getText().toString())
                .whereEqualTo(Constants.PASSWORD,inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){

                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                            preferenceManager.putString(Constants.ID,documentSnapshot.getId());
                            preferenceManager.putString(Constants.FIRST_NAME,documentSnapshot.getString(Constants.FIRST_NAME));
                            preferenceManager.putString(Constants.LAST_NAME,documentSnapshot.getString(Constants.LAST_NAME));
                            preferenceManager.putString(Constants.EMAIL,documentSnapshot.getString(Constants.EMAIL));
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        }else{

                            signInProgressBar.setVisibility(View.INVISIBLE);
                            btn_SignIn.setVisibility(View.VISIBLE);
                            Toast.makeText(SignInActivity.this, "Unable to Sign In", Toast.LENGTH_SHORT).show();

                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        signInProgressBar.setVisibility(View.INVISIBLE);
                        btn_SignIn.setVisibility(View.VISIBLE);
                        Toast.makeText(SignInActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}