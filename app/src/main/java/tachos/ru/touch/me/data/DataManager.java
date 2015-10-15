package tachos.ru.touch.me.data;

import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import java.util.Date;

public class DataManager {
    private static final int usersPageSize = 20;
    private static final long timeBetweenUpdates = 10 * 1000;
    private static final long timeBetweenTouches = 5 * 1000;
    private static Thread lastActivityUpdater = null;
    private static long lastOnlineSend = 0;
    private static boolean isInForeground = false;
    private static long lastTouch = 0;
    private static long lastTouchSend = 0;

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
