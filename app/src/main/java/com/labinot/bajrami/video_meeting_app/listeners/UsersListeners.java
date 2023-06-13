package com.labinot.bajrami.video_meeting_app.listeners;

import com.labinot.bajrami.video_meeting_app.models.User;

public interface UsersListeners {

    void initiateVideoMeeting(User user);

    void initiateAudioMeeting(User user);

    void onMultipleUsersAction(Boolean isMultipleUsersSelected);

}
