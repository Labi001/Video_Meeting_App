package com.labinot.bajrami.video_meeting_app.activities;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.labinot.bajrami.video_meeting_app.R;
import com.labinot.bajrami.video_meeting_app.adapters.UserAdapter;
import com.labinot.bajrami.video_meeting_app.listeners.UsersListeners;
import com.labinot.bajrami.video_meeting_app.models.User;
import com.labinot.bajrami.video_meeting_app.utilities.Constants;
import com.labinot.bajrami.video_meeting_app.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsersListeners {

    private PreferenceManager preferenceManager;
    private TextView txt_title,txt_signOut;
    private RecyclerView userRecyclerView;
    private List<User> userList;
    private UserAdapter userAdapter;
    private TextView txtErrorMessage;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imgConference;
    private AlertDialog alertDialog;
    private int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();


        txt_title.setText(String.format(

                "%s %s",
                preferenceManager.getString(Constants.FIRST_NAME),
                preferenceManager.getString(Constants.LAST_NAME)

        ));

        txt_signOut.setOnClickListener(v -> signOut());


        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {

            if(task.isSuccessful() && task.getResult() != null)
                sentFCMTokenToDatabase(task.getResult());

        });

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList,this);
        userRecyclerView.setAdapter(userAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUsers();
            }
        });

         getUsers();
         checkForBatteryOptimizations();


    }

    private void initViews() {

        preferenceManager = new PreferenceManager(getApplicationContext());
        txt_title = findViewById(R.id.txt_title);
        txt_signOut = findViewById(R.id.txt_signOut);
        userRecyclerView = findViewById(R.id.usersRecyclerView);
        txtErrorMessage = findViewById(R.id.txtErrorMessage);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        imgConference = findViewById(R.id.imgConferences);
    }

    private void getUsers() {
           swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                swipeRefreshLayout.setRefreshing(false);
                String mUserId = preferenceManager.getString(Constants.ID);

                if(task.isSuccessful() && task.getResult() != null){
                    userList.clear();
                    for(QueryDocumentSnapshot documentSnapshot:task.getResult()){

                        if(mUserId.equals(documentSnapshot.getId()))
                            continue;


                        User user = new User();
                        user.first_name = documentSnapshot.getString(Constants.FIRST_NAME);
                        user.last_name = documentSnapshot.getString(Constants.LAST_NAME);
                        user.email = documentSnapshot.getString(Constants.EMAIL);
                        user.token = documentSnapshot.getString(Constants.FCM_TOKEN);
                        userList.add(user);

                    }

                    if(userList.size() > 0){
                        userAdapter.notifyDataSetChanged();
                    }else{
                        txtErrorMessage.setText(String.format("%s ","No users available"));
                        txtErrorMessage.setVisibility(View.VISIBLE);
                    }


                }else{

                    txtErrorMessage.setText(String.format("%s ","No users available"));
                    txtErrorMessage.setVisibility(View.VISIBLE);

                }

            }
        });

    }

    private void sentFCMTokenToDatabase(String token){

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(

                        preferenceManager.getString(Constants.ID)

                );

        documentReference.update(Constants.FCM_TOKEN,token)
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Unable to send token: " + e.getMessage(), Toast.LENGTH_SHORT).show());


    }

    private void signOut(){

        Toast.makeText(this, "Sign Out...", Toast.LENGTH_SHORT).show();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.ID)
        );
        HashMap<String,Object> updates = new HashMap<>();
        updates.put(Constants.FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {

            preferenceManager.clearPreference();
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();

        }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Unable To Sign Out", Toast.LENGTH_SHORT).show());

    }

    @Override
    public void initiateVideoMeeting(User user) {

        if(user.token == null || user.token.trim().isEmpty()){

            Toast.makeText(this, user.first_name + " "
                    + user.last_name + "is not available for meeting", Toast.LENGTH_SHORT).show();

        }else{

            Intent intent = new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
            intent.putExtra("user",user);
            intent.putExtra("type","video");
            startActivity(intent);

        }

    }

    @Override
    public void initiateAudioMeeting(User user) {

        if(user.token == null || user.token.trim().isEmpty()){

            Toast.makeText(this, user.first_name + " "
                    + user.last_name + "is not available for meeting", Toast.LENGTH_SHORT).show();

        }else{

            Intent intent = new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
            intent.putExtra("user",user);
            intent.putExtra("type","audio");
            startActivity(intent);
        }

    }

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {

        if(isMultipleUsersSelected){

            imgConference.setVisibility(View.VISIBLE);
            imgConference.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
                    intent.putExtra("selectedUser", new Gson().toJson(userAdapter.SelectedUsers()));
                    intent.putExtra("type","video");
                    intent.putExtra("isMultiple",true);
                    startActivity(intent);

                }
            });

        }else {

            imgConference.setVisibility(View.GONE);

        }


    }


    private void checkForBatteryOptimizations(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

            if(!powerManager.isIgnoringBatteryOptimizations(getPackageName())){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Warning");
                builder.setMessage("Battery optimization is enabled.It can interrupt running background service.");
                builder.setPositiveButton("Disable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                       // startActivityForResult(intent,REQUEST_CODE_BATTERY_OPTIMIZATIONS);
                       settingsLauncher.launch(intent);

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                          dialog.dismiss();
                    }
                });
                alertDialog = builder.create();
                alertDialog.setCancelable(false);
                alertDialog.show();

            }

        }

    }

    ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            if(result.getResultCode() == RESULT_OK)
                checkForBatteryOptimizations();

        }
    });



//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS)
//            checkForBatteryOptimizations();
//    }
}