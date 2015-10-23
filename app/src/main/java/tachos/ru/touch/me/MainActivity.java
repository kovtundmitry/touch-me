package tachos.ru.touch.me;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserTokenStorageFactory;
import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.L;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import tachos.ru.touch.me.data.DataManager;
import tachos.ru.touch.me.data.Users;
import tachos.ru.touch.me.fragments.FragmentGame;
import tachos.ru.touch.me.fragments.FragmentLogin;
import tachos.ru.touch.me.fragments.FragmentMissingAvatar;
import tachos.ru.touch.me.fragments.FragmentRegister;
import tachos.ru.touch.me.fragments.FragmentUsers;

public class MainActivity extends Activity {
    static final int MESSAGE_GAME_INVITE = 1;
    static final int MESSAGE_GAME_ACCEPTED = 2;
    static final int MESSAGE_GAME_DECLINED = 3;
    public static AlertDialog currDialog = null;
    public static String partnerId = "";
    static Handler handlerMessages;
    RelativeLayout rlLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        initBackendless();
        initHandler();

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                        //.cacheOnDisk(true)
                .showImageOnLoading(android.R.drawable.ic_lock_lock) // resource or drawable
                .showImageForEmptyUri(android.R.drawable.ic_lock_lock) // resource or drawable
                .showImageOnFail(android.R.drawable.ic_lock_lock)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);
        L.writeLogs(false);

        setContentView(R.layout.activity_main);

        rlLoading = (RelativeLayout) findViewById(R.id.rl_activity_main_loading);
        rlLoading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        String userToken = UserTokenStorageFactory.instance().getStorage().get();
        if (userToken != null && !userToken.equals("")) {
            if (Backendless.UserService.CurrentUser() == null) {
                startLoading();
                Backendless.UserService.findById(Backendless.UserService.loggedInUser(), new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser response) {
                        Backendless.UserService.setCurrentUser(response);
                        DataManager.updateLikedUsers(new AsyncCallback<List<Users>>() {
                            @Override
                            public void handleResponse(List<Users> response) {
                                Messenger.registerDevice();
                                DataManager.startLastActivityUpdater();
                                stopLoading();
                                startFragmentUsers();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Log.d("test", "Unable to login");
                                stopLoading();
                                startFragmentLogin();
                            }
                        });
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.d("test", "Unable to login");
                        stopLoading();
                        startFragmentLogin();
                    }
                });
            }
        } else {
            startFragmentRegister();
        }
    }

    public void startLoading() {
        rlLoading.setVisibility(View.VISIBLE);
    }

    public void stopLoading() {
        rlLoading.setVisibility(View.GONE);
    }

    public void displayMissingAvatar(boolean visibility) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (visibility)
            transaction.replace(R.id.fl_main_missing_avatar_container, new FragmentMissingAvatar());
        else if (FragmentMissingAvatar.getInstance() != null)
            transaction.remove(FragmentMissingAvatar.getInstance());
        transaction.commit();
    }

    @SuppressLint("HandlerLeak")
    private void initHandler() {
        handlerMessages = new Handler() {
            public void handleMessage(final android.os.Message msg) {
                final String pId = msg.obj.toString();
                switch (msg.what) {
                    case MESSAGE_GAME_INVITE:
                        Backendless.UserService.findById(pId, new AsyncCallback<BackendlessUser>() {
                            @Override
                            public void handleResponse(BackendlessUser response) {
                                currDialog = new AlertDialog.Builder(MainActivity.this).create();
                                currDialog.setTitle("You have been invited");
                                currDialog.setMessage(response.getProperty("name").toString() + " want to play with you!");

                                currDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Agree", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(MainActivity.this, "Trying to connect", Toast.LENGTH_SHORT).show();
                                        Messenger.sendInviteResponse(pId, true);
                                        startLoading();
                                        dialog.dismiss();
                                    }
                                });
                                currDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Nope", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Messenger.sendInviteResponse(pId, false);
                                        dialog.dismiss();
                                    }
                                });
                                currDialog.show();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                            }
                        });
                        break;
                    case MESSAGE_GAME_ACCEPTED:
                        currDialog.dismiss();
                        partnerId = pId;
                        startFragmentGame();
                        stopLoading();
                        break;
                    case MESSAGE_GAME_DECLINED:
                        currDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Your partner don\'t want to play", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().findFragmentByTag(FragmentGame.class.getName()) != null) {
            startFragmentUsers();
            if (ServerConnection.getInstance() != null) {
                ServerConnection.getInstance().disconnect();
            }
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Messenger.unregisterDevice();
        DataManager.setIsUpdaterInForeground(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataManager.stopLastActivityUpdater();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Backendless.UserService.CurrentUser() != null) Messenger.registerDevice();
        DataManager.setIsUpdaterInForeground(true);
    }

    public void startFragmentLogin() {
        replaceFragment(new FragmentLogin());
    }

    public void startFragmentGame() {
        replaceFragment(new FragmentGame());
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
            DataManager.setLastTouch(System.currentTimeMillis());
        return super.dispatchTouchEvent(ev);
    }

    public void startFragmentUsers() {
        replaceFragment(new FragmentUsers());
    }

    public void startFragmentRegister() {
        replaceFragment(new FragmentRegister());
    }

    private void initBackendless() {
        String appVersion = "v1";
        Backendless.initApp(this, "365FC299-BD60-33E4-FF58-FC3CD4CF0100", BuildConfig.backendlessSk, appVersion);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fl_main_container, fragment, fragment.getClass().getName());
        transaction.commit();
    }
}
