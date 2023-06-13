package com.labinot.bajrami.video_meeting_app.utilities;

import java.util.HashMap;

public class Constants {

    public final static String FIRST_NAME = "first_name";
    public final static String LAST_NAME = "last_name";
    public final static String EMAIL = "email";
    public final static String PASSWORD = "password";
    public final static String ID = "user_id";
    public final static String FCM_TOKEN = "fcm_token";
    public final static String KEY_COLLECTION_USERS = "users";
    public final static String KEY_PREFERENCE_NAME = "videoMeetingPreference";
    public final static String KEY_IS_SIGNED_IN = "isSignedIn";
    public final static String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public final static String REMOTE_CONTENT_TYPE = "Content-Type";
    public final static String REMOTE_MSG_TYPE = "type";
    public final static String REMOTE_MSG_INVITATION = "invitation";
    public final static String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public final static String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public final static String REMOTE_MSG_DATA = "data";
    public final static String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public final static String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";
    public final static String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public final static String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public final static String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";
    public final static String REMOTE_MSG_МEETING_ROOM = "meetingRoom";



    public static HashMap<String,String> getRemoteMessageHeader(){

        HashMap<String,String> headers = new HashMap<>();
        headers.put(

                Constants.REMOTE_MSG_AUTHORIZATION,
                "key=AAAA4LWlqtw:APA91bFHWsHgVOCmapc-3HclWSBu2pXa-ypGYg1CAxIkfoYZgBAJzRNbbf4xeu0weF71Yf5Cc5Ey_EgLbOGZRqFTxYDUX0EAejfmEdU3ikWF8Xr7PU1ZQbqVe3cQ-WBwbEQH942U_Ixp"

        );

        headers.put(Constants.REMOTE_CONTENT_TYPE,"application/json");

        return headers;
    }

}
