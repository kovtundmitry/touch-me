package tachos.ru.touch_me;


import android.util.Log;

import com.backendless.Backendless;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.Message;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.backendless.messaging.SubscriptionOptions;

import java.util.ArrayList;
import java.util.List;

public class Messenger {
    private static final String CHANNEL_GAME_INVITE = "gameInviteChannel";
    private static final String COMMAND_INVITE = "join me";
    private static final String COMMAND_INVITE_ACCEPTED = "invite accepted";
    private static final String COMMAND_INVITE_DECLINED = "invite declined";
    private static final ArrayList<String> channels = new ArrayList<String>() {{
        add(CHANNEL_GAME_INVITE);
    }};
    private static final String DEBUG_TAG = "messenger";
    private static Subscription gameInviteSubscription;
    private static String partnerId = null;
    private static boolean needsToResponseInvitation = false;

    public static void unregisterDevice() {
        Backendless.Messaging.unregisterDeviceOnServer(new AsyncCallback<Boolean>() {
            @Override
            public void handleResponse(Boolean response) {
                Log.d(DEBUG_TAG, "unregistered " + response);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d(DEBUG_TAG, "error unregister device: " + fault.getMessage());
            }
        });
    }

    public static void sendInvite(String userId) {
        partnerId = userId;
        needsToResponseInvitation = true;
        PublishOptions publishOptions = new PublishOptions();
        publishOptions.setPublisherId(Backendless.UserService.CurrentUser().getUserId());
        publishOptions.setSubtopic(userId);

        Backendless.Messaging.publish(CHANNEL_GAME_INVITE, COMMAND_INVITE, publishOptions, new AsyncCallback<MessageStatus>() {
            @Override
            public void handleResponse(MessageStatus response) {
                Log.d(DEBUG_TAG, "send: " + response.getStatus());
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d(DEBUG_TAG, "unable to send: " + fault.getMessage());
            }
        });
    }

    public static void sendInviteResponse(String userId, boolean accepted) {
        PublishOptions publishOptions = new PublishOptions();
        publishOptions.setPublisherId(Backendless.UserService.CurrentUser().getUserId());
        publishOptions.setSubtopic(userId);

        Backendless.Messaging.publish(CHANNEL_GAME_INVITE, (accepted) ? COMMAND_INVITE_ACCEPTED : COMMAND_INVITE_DECLINED, publishOptions, new AsyncCallback<MessageStatus>() {
            @Override
            public void handleResponse(MessageStatus response) {
                Log.d(DEBUG_TAG, "send: " + response.getStatus());
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d(DEBUG_TAG, "unable to send: " + fault.getMessage());
            }
        });
    }

    public static void cancelInvitation() {
        partnerId = null;
    }

    public static void registerDevice() {
        Backendless.Messaging.registerDeviceOnServer(Backendless.UserService.CurrentUser().getUserId(),
                channels,
                System.currentTimeMillis() + 2 * 60 * 60 * 1000,
                new AsyncCallback<String>() {
                    @Override
                    public void handleResponse(String response) {
                        Log.d(DEBUG_TAG, "registering: " + response);
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.d(DEBUG_TAG, "registering error: " + fault.getMessage());
                    }
                });
        subscribeGameInvite();
    }

    private static void subscribeGameInvite() {
        if (gameInviteSubscription != null) {
            gameInviteSubscription.resumeSubscription();
            return;
        }
        AsyncCallback<List<Message>> messageCallback = new AsyncCallback<List<Message>>() {
            public void handleResponse(List<Message> response) {
                for (Message message : response) {
                    String publisherId = message.getPublisherId();
                    Object data = message.getData();

                    if (MainActivity.handlerMessages != null) {
                        android.os.Message msg = null;
                        if (data.toString().contains(COMMAND_INVITE) && partnerId == null) {
                            partnerId = publisherId;
                            needsToResponseInvitation = false;
                            msg = MainActivity.handlerMessages.obtainMessage(MainActivity.MESSAGE_GAME_INVITE, publisherId);
                        }
                        if (data.toString().contains(COMMAND_INVITE_ACCEPTED) && partnerId != null && partnerId.equals(publisherId)) {
                            msg = MainActivity.handlerMessages.obtainMessage(MainActivity.MESSAGE_GAME_ACCEPTED, publisherId);
                            if (needsToResponseInvitation) {
                                needsToResponseInvitation = false;
                                sendInviteResponse(partnerId, true);
                            }
                            partnerId = null;
                        }
                        if (data.toString().contains(COMMAND_INVITE_DECLINED) && partnerId != null && partnerId.equals(publisherId)) {
                            msg = MainActivity.handlerMessages.obtainMessage(MainActivity.MESSAGE_GAME_DECLINED, publisherId);
                            partnerId = null;
                        }
                        if (msg != null) MainActivity.handlerMessages.sendMessage(msg);
                    }
                    Log.d(DEBUG_TAG, "received: " + publisherId + " " + data);
                }
            }

            public void handleFault(BackendlessFault fault) {
                Log.d(DEBUG_TAG, fault.getMessage());
            }
        };
        AsyncCallback<Subscription> subscriptionCallback = new AsyncCallback<Subscription>() {
            public void handleResponse(Subscription response) {
                gameInviteSubscription = response;
            }

            public void handleFault(BackendlessFault fault) {
                Log.d(DEBUG_TAG, fault.getMessage());
            }
        };
        SubscriptionOptions subsOpt = new SubscriptionOptions();
        subsOpt.setSubtopic(Backendless.UserService.CurrentUser().getUserId());
        Backendless.Messaging.subscribe(CHANNEL_GAME_INVITE, messageCallback, subsOpt, subscriptionCallback);
    }
}
