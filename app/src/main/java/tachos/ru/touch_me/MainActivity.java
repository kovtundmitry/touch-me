package tachos.ru.touch_me;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.persistence.local.UserTokenStorageFactory;
import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.L;

import java.io.File;
import java.io.IOException;

import io.fabric.sdk.android.Fabric;
import tachos.ru.touch_me.data.Avatar;
import tachos.ru.touch_me.data.DataManager;
import tachos.ru.touch_me.fragments.FragmentLogin;
import tachos.ru.touch_me.fragments.FragmentUsers;
import tachos.ru.touch_me.fragments.GameFragment;

public class MainActivity extends Activity {
    public static final int REQUEST_CODE_PICTURE_SELECT = 1717;
    public static final int REQUEST_CODE_PICTURE_CROP = 1718;
    static final int MESSAGE_GAME_INVITE = 1;
    static final int MESSAGE_GAME_ACCEPTED = 2;
    static final int MESSAGE_GAME_DECLINED = 3;
    public static AlertDialog currDialog = null;
    static Handler handlerMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        initBackendless();
        initHandler();

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);
        L.writeLogs(false);

        setContentView(R.layout.activity_main);

        String userToken = UserTokenStorageFactory.instance().getStorage().get();
        if (userToken != null && !userToken.equals("")) {
            if (Backendless.UserService.CurrentUser() == null)
                Backendless.UserService.findById(Backendless.UserService.loggedInUser(), new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser response) {
                        Backendless.UserService.setCurrentUser(response);
                        Messenger.registerDevice();
                        DataManager.startLastActivityUpdater();
                        startFragmentUsers();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.d("test", "Unable to login");
                        startFragmentLogin();
                    }
                });

        } else {
            startFragmentLogin();
        }
    }

    private void initHandler() {
        handlerMessages = new Handler() {
            public void handleMessage(final android.os.Message msg) {
                final String partnerId = msg.obj.toString();
                switch (msg.what) {
                    case MESSAGE_GAME_INVITE:
                        Backendless.UserService.findById(partnerId, new AsyncCallback<BackendlessUser>() {
                            @Override
                            public void handleResponse(BackendlessUser response) {
                                currDialog = new AlertDialog.Builder(MainActivity.this).create();
                                currDialog.setTitle("You have been invited");
                                currDialog.setMessage(response.getProperty("name").toString() + " want to play with you!");

                                currDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Agree", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(MainActivity.this, "Trying to connect", Toast.LENGTH_SHORT).show();
                                        Messenger.sendInviteResponse(partnerId, true);
                                        dialog.dismiss();
                                    }
                                });
                                currDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Nope", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Messenger.sendInviteResponse(partnerId, false);
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
                        startFragmentGame();
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
        replaceFragment(new GameFragment());
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

    private void initBackendless() {
        String appVersion = "v1";
        Backendless.initApp(this, "365FC299-BD60-33E4-FF58-FC3CD4CF0100", "1C23155A-8B3C-0E24-FF1B-04ED6021E800", appVersion);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fl_main_container, fragment);
        transaction.commit();
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        Log.d("test", "Activity result " + requestCode + " " + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_PICTURE_SELECT:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    performCrop(selectedImage);
                }
                break;
            case REQUEST_CODE_PICTURE_CROP:
                if (resultCode == RESULT_OK) {
                    Bitmap selectedBitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/tempAva.jpg");
                    Backendless.Files.Android.upload(
                            selectedBitmap,
                            Bitmap.CompressFormat.JPEG, 100,
                            Backendless.UserService.CurrentUser().getUserId() + ".jpg",
                            Avatar.generatePathToAva(Backendless.UserService.CurrentUser().getUserId()),
                            new AsyncCallback<BackendlessFile>() {
                                @Override
                                public void handleResponse(final BackendlessFile backendlessFile) {
                                    Log.d("test", "Uploaded successfully");

                                }

                                @Override
                                public void handleFault(BackendlessFault backendlessFault) {
                                    Log.d("test", "Failed to upload " + backendlessFault.getMessage());
                                }
                            });
                }
                break;
        }
    }


    private void performCrop(Uri picUri) {
        try {

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 3);
            cropIntent.putExtra("aspectY", 4);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 768);
            cropIntent.putExtra("outputY", 1024);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            cropIntent.putExtra("scale", true);
            File f = new File(Environment.getExternalStorageDirectory(), "tempAva.jpg");
            try {
                f.createNewFile();
            } catch (IOException e) {
            }
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, REQUEST_CODE_PICTURE_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
