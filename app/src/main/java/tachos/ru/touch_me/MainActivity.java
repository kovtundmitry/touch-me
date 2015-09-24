package tachos.ru.touch_me;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserTokenStorageFactory;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        initBackendless();
        setContentView(R.layout.activity_main);

        String userToken = UserTokenStorageFactory.instance().getStorage().get();
        if (userToken != null && !userToken.equals("")) {
            Backendless.UserService.findById(Backendless.UserService.loggedInUser(), new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser response) {
                    Backendless.UserService.setCurrentUser(response);
                    DataManager.startLastActivityUpdater();
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    Log.d("test", "Unable to login");
                }
            });
            startFragmentUsers();
        } else {
            startFragmentLogin();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
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
        DataManager.setIsUpdaterInForeground(true);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fl_main_container, fragment);
        transaction.commit();
    }

    public void startFragmentLogin() {
        replaceFragment(new FragmentLogin());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
            DataManager.setLastTouch(System.currentTimeMillis());
        return super.dispatchTouchEvent(ev);
    }

    public void startFragmentUsers() {
        replaceFragment(new FragmentUsers());
    }

    private void initBackendless() {
        String appVersion = "v1";
        Backendless.initApp(this, "365FC299-BD60-33E4-FF58-FC3CD4CF0100", "1C23155A-8B3C-0E24-FF1B-04ED6021E800", appVersion);
    }

}
