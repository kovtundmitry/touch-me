package tachos.ru.touch.me.data;

import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataManager {
    private static final int usersPageSize = 20;
    private static final long timeBetweenUpdates = 10 * 1000;
    private static final long timeBetweenTouches = 5 * 1000;
    private static Thread lastActivityUpdater = null;
    private static long lastOnlineSend = 0;
    private static boolean isInForeground = false;
    private static long lastTouch = 0;
    private static long lastTouchSend = 0;
    private static List<String> likedUsersIds = new ArrayList<>();
    private static Users currUser;

    public static List<Users> getLikedUsers() {
        return currUser.getLikedUsers();
    }

    public static List<String> getLikedUsersIds() {
        return likedUsersIds;
    }

    public static void likeUser(Users user) {
        likedUsersIds.add(user.getObjectId());
        currUser.getLikedUsers().add(user);
        Backendless.Persistence.save(currUser, new AsyncCallback<Users>() {
            @Override
            public void handleResponse(Users response) {
                Log.d("test", "like ok");
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d("test", "like not ok!");
            }
        });
    }

    public static void unLikeUser(Users user) {
        likedUsersIds.remove(user.getObjectId());
        for (Users likedUser : currUser.getLikedUsers()) {
            if (likedUser.getObjectId().equals(user.getObjectId())) {
                Log.d("test", "deleted: " + currUser.getLikedUsers().remove(likedUser));
                break;
            }
        }
        Backendless.Persistence.save(currUser, new AsyncCallback<Users>() {
            @Override
            public void handleResponse(Users response) {
                Log.d("test", "unlike ok");
            }

            @Override
            public void handleFault(BackendlessFault fault) {

                Log.d("test", "unlike not ok!");
            }
        });
    }

    public static void startLastActivityUpdater() {
        if (lastActivityUpdater == null || lastActivityUpdater.isInterrupted()) {
            lastActivityUpdater = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (lastActivityUpdater != null && !lastActivityUpdater.isInterrupted()) {
                        if (Backendless.UserService.CurrentUser() == null) {
                            stopLastActivityUpdater();
                            return;
                        }
                        if (!isInForeground || (System.currentTimeMillis() - lastOnlineSend < timeBetweenUpdates && lastTouch - lastTouchSend < timeBetweenTouches)) {
                            try {
                                Thread.sleep(timeBetweenTouches);
                            } catch (InterruptedException e) {
                                return;
                            }
                        } else {
                            updateLastActivity();
                        }
                    }
                }
            });
            lastActivityUpdater.start();
        }
    }

    public static void setLastTouch(long touchTime) {
        lastTouch = touchTime;
    }

    public static void setIsUpdaterInForeground(boolean appInForeground) {
        isInForeground = appInForeground;
    }

    public static void stopLastActivityUpdater() {
        if (lastActivityUpdater != null) lastActivityUpdater.interrupt();
        lastActivityUpdater = null;
    }

    public static void getUsers(AsyncCallback<BackendlessCollection<Users>> asyncCallback) {
        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.addSortByOption("lastOnline DESC");
        queryOptions.setPageSize(usersPageSize);
        dataQuery.setQueryOptions(queryOptions);
        dataQuery.setWhereClause("objectId != \'" + Backendless.UserService.CurrentUser().getUserId() + "\'");
        Backendless.Persistence.of(Users.class).find(dataQuery, asyncCallback);
    }

    public static void updateLikedUsers(final AsyncCallback<List<Users>> asyncCallback) {
        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.addRelated("likedUsers");
        queryOptions.addRelated("likedUsers.objectId");
        dataQuery.setQueryOptions(queryOptions);
        dataQuery.setWhereClause("objectId = \'" + Backendless.UserService.CurrentUser().getUserId() + "\'");
        Backendless.Persistence.of(Users.class).find(dataQuery, new AsyncCallback<BackendlessCollection<Users>>() {
            @Override
            public void handleResponse(BackendlessCollection<Users> response) {
                currUser = response.getData().get(0);
                likedUsersIds.clear();
                for (Users us : currUser.getLikedUsers()) {
                    likedUsersIds.add(us.getObjectId());
                }
                Log.d("test", "liked users count: " + currUser.getLikedUsers().size());
                asyncCallback.handleResponse(currUser.getLikedUsers());
            }

            @Override
            public void handleFault(BackendlessFault fault) {

                Log.d("test", "unable to get liked users: " + fault.getMessage());
                asyncCallback.handleFault(fault);
            }
        });
    }

    private static void updateLastActivity() {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user != null) {
            try {
                if (lastTouch - lastTouchSend > timeBetweenTouches) {
                    user.setProperty("lastTouch", lastTouch);
                }
                user.setProperty("lastOnline", System.currentTimeMillis());
                Backendless.UserService.setCurrentUser(Backendless.UserService.update(user));
                user = Backendless.UserService.CurrentUser();
                lastOnlineSend = (user.getProperty("lastOnline") != null) ? ((Date) user.getProperty("lastOnline")).getTime() : 0;
                lastTouchSend = (user.getProperty("lastTouch") != null) ? ((Date) user.getProperty("lastTouch")).getTime() : 0;
            } catch (BackendlessException e) {
                Log.d("test", "updating last activity error: " + e.getMessage());
            }
        }
    }
}
