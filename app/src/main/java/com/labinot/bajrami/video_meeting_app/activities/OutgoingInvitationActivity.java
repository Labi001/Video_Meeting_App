package com.labinot.bajrami.video_meeting_app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.labinot.bajrami.video_meeting_app.R;
import com.labinot.bajrami.video_meeting_app.models.User;
import com.labinot.bajrami.video_meeting_app.network.ApiClient;
import com.labinot.bajrami.video_meeting_app.network.ApiService;
import com.labinot.bajrami.video_meeting_app.utilities.Constants;
import com.labinot.bajrami.video_meeting_app.utilities.PreferenceManager;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {

    private ImageView imgMeetingType,imgStopInvitation;
    private TextView txtFirstChar,txtUserName,txtEmail;
    private PreferenceManager preferenceManager;
    private String inviterToken = null;
    private String meetingType = null;
   private String meetingRoom = null;
   private int rejectionCount = 0;
   private int totalReceivers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);

        initViews();



        User user = (User) getIntent().getSerializableExtra("user");
        meetingType = getIntent().getStringExtra("type");


        if(user != null){

            txtFirstChar.setText(user.first_name.substring(0,1));
            txtUserName.setText(String.format("%s %s",user.first_name,user.last_name));
            txtEmail.setText(user.email);

        }

        if(meetingType != null){

            if(meetingType.equals("video"))
                imgMeetingType.setImageResource(R.drawable.ic_video);
            else
                imgMeetingType.setImageResource(R.drawable.ic_audio);


        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                if(task.isSuccessful() && task.getResult() != null){

                    inviterToken = task.getResult();

                    if(meetingType != null){

                        if(getIntent().getBooleanExtra("isMultiple",false)){

                            Type type = new TypeToken<ArrayList<User>>(){}.getType();
                            ArrayList<User> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUser"),type);

                            if(receivers != null){

                                totalReceivers = receivers.size();

                            }

                            initiateMeeting(meetingType,null,receivers);

                        }else{

                            if( user != null)
                                totalReceivers = 1;
                                initiateMeeting(meetingType, user.token,null);

                        }

                    }


                }


            }
        });



        imgStopInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getIntent().getBooleanExtra("isMultiple",false)){

                    Type type = new TypeToken<ArrayList<User>>(){}.getType();
                    ArrayList<User> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUser"),type);
                    cancelInvitation(null,receivers);

                }else{
                    if(user !=null)
                        cancelInvitation(user.token,null);

                }



            }
        });
    }

    private void initViews() {

        preferenceManager = new PreferenceManager(getApplicationContext());
        imgMeetingType = findViewById(R.id.img_meetingType);
        txtFirstChar = findViewById(R.id.txtFirstChar);
        txtUserName = findViewById(R.id.txtUserName);
        txtEmail = findViewById(R.id.txtEmail);
        imgStopInvitation = findViewById(R.id.img_stop_invitation);

    }

    @SuppressLint("SuspiciousIndentation")
    public void initiateMeeting(String meetingType, String receiverToken, ArrayList<User> receivers){

        try {

            JSONArray tokens = new JSONArray();

            if(receiverToken != null)
            tokens.put(receiverToken);

            if(receivers != null && receivers.size() > 0){

                StringBuilder userName = new StringBuilder();
                for(int i = 0; i < receivers.size();i++){

                    tokens.put(receivers.get(i).token);
                    userName.append(receivers.get(i).first_name).append(" ").append(receivers.get(i).last_name).append("\n");

                }
                txtFirstChar.setVisibility(View.GONE);
                txtEmail.setVisibility(View.GONE);
                txtUserName.setText(userName.toString());


            }

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE,meetingType);
            data.put(Constants.FIRST_NAME,preferenceManager.getString(Constants.FIRST_NAME));
            data.put(Constants.LAST_NAME,preferenceManager.getString(Constants.LAST_NAME));
            data.put(Constants.EMAIL,preferenceManager.getString(Constants.EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN,inviterToken);

            meetingRoom = preferenceManager.getString(Constants.ID) + "_" + UUID.randomUUID().toString().substring(0,5);

            data.put(Constants.REMOTE_MSG_МEETING_ROOM,meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

            sendRemoteMessage(body.toString(),Constants.REMOTE_MSG_INVITATION);

        }catch (Exception e){

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
        
    }

    public void sendRemoteMessage(String remoteMessageBody,String type){

        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(

                Constants.getRemoteMessageHeader(),remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {

                if(response.isSuccessful()){
                    
                    if(type.equals(Constants.REMOTE_MSG_INVITATION)){

                        Toast.makeText(OutgoingInvitationActivity.this, "Invitation send successfully", Toast.LENGTH_SHORT).show();
                    }else if(type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){

                        Toast.makeText(OutgoingInvitationActivity.this, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                    }


                }else{

                    Toast.makeText(OutgoingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }

            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                Toast.makeText(OutgoingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    @SuppressLint("SuspiciousIndentation")
    public void cancelInvitation(String receiverToken, ArrayList<User>receivers){

        try{

            JSONArray tokens = new JSONArray();

            if(receiverToken != null)
            tokens.put(receiverToken);

            if(receivers != null && receivers.size() > 0){

                for(User user: receivers){
                    tokens.put(user.token);

                }

            }

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE,Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

            sendRemoteMessage(body.toString(),Constants.REMOTE_MSG_INVITATION_RESPONSE);


        }catch (Exception e){

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }


    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);

            if(type != null){

                if(type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {

                    try {

                        URL serverUrl = new URL("https://meet.jit.si");

                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverUrl);
                        builder.setRoom(meetingRoom);

                        if(meetingType.equals("audio"))
                            builder.setVideoMuted(true);


                        JitsiMeetActivity.launch(OutgoingInvitationActivity.this,builder.build());
                        finish();

                    }catch (Exception e){

                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }


                }else if(type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)) {
                    rejectionCount += 1;

                    if(rejectionCount == totalReceivers){

                        Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }


            }

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(

                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)

        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver

        );

    }
}