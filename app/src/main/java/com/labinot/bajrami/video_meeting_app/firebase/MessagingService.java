package com.labinot.bajrami.video_meeting_app.firebase;



import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.labinot.bajrami.video_meeting_app.activities.IncomingInvitationActivity;
import com.labinot.bajrami.video_meeting_app.utilities.Constants;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String type = message.getData().get(Constants.REMOTE_MSG_TYPE);

        if(type != null){

            if(type.equals(Constants.REMOTE_MSG_INVITATION)){

                Intent intent = new Intent(getApplicationContext(), IncomingInvitationActivity.class);
                intent.putExtra(
                        Constants.REMOTE_MSG_MEETING_TYPE,
                        message.getData().get(Constants.REMOTE_MSG_MEETING_TYPE)
                );

                intent.putExtra(
                        Constants.FIRST_NAME,
                        message.getData().get(Constants.FIRST_NAME)
                );

                intent.putExtra(
                        Constants.LAST_NAME,
                        message.getData().get(Constants.LAST_NAME)
                );

                intent.putExtra(
                        Constants.EMAIL,
                        message.getData().get(Constants.EMAIL)
                );

                intent.putExtra(
                        Constants.REMOTE_MSG_INVITER_TOKEN,
                        message.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN)
                );

                intent.putExtra(
                     Constants.REMOTE_MSG_МEETING_ROOM,
                        message.getData().get(Constants.REMOTE_MSG_МEETING_ROOM)

                );

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


            }else if(type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){

                Intent intent = new Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE);
                intent.putExtra(
                        Constants.REMOTE_MSG_INVITATION_RESPONSE,
                        message.getData().get(Constants.REMOTE_MSG_INVITATION_RESPONSE)

                );
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            }

        }

    }
}
