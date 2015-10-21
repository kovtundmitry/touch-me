package tachos.ru.touch.me.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

import tachos.ru.touch.me.MainActivity;
import tachos.ru.touch.me.Messenger;
import tachos.ru.touch.me.R;
import tachos.ru.touch.me.adapters.AdapterListViewLikedUsers;
import tachos.ru.touch.me.adapters.AdapterListViewUsers;
import tachos.ru.touch.me.data.Avatar;
import tachos.ru.touch.me.data.DataManager;
import tachos.ru.touch.me.data.Users;

public class FragmentUsers extends Fragment {
    ListView lvUsers;
    AdapterListViewUsers adapterUsers;
    ArrayList<Users> users = new ArrayList<>();
    boolean isPaused = false;


    //--Drawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private AdapterListViewLikedUsers adapterListViewLikedUsers;

    //--
    private void startUsersUpdater(long time) {
        if (getActivity() == null || isPaused) return;
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null && !isPaused)
                    DataManager.getUsers(new AsyncCallback<BackendlessCollection<Users>>() {
                        @Override
                        public void handleResponse(BackendlessCollection<Users> response) {
                            if (getActivity() == null && isPaused) return;
                            users.clear();
                            users.addAll(response.getData());
                            adapterUsers.notifyDataSetChanged();
                            startUsersUpdater(10000);
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.d("test", "error acquiring users: " + fault.getMessage());
                            startUsersUpdater(10000);
                        }
                    });
            }
        }, time);
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
        startUsersUpdater(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_users, container, false);
        lvUsers = ((ListView) root.findViewById(R.id.lv_users_list));
        adapterUsers = new AdapterListViewUsers(users, getActivity());
        lvUsers.setAdapter(adapterUsers);
        lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Messenger.sendInvite(users.get(position).getObjectId());
                MainActivity.currDialog = new AlertDialog.Builder(getActivity()).create();
                MainActivity.currDialog.setTitle("Invite send");
                MainActivity.currDialog.setCancelable(false);
                MainActivity.currDialog.setMessage("Waiting for response");
                MainActivity.currDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NOOOO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Messenger.cancelInvitation();
                        dialog.dismiss();
                    }
                });
                MainActivity.currDialog.show();
            }
        });
        root.findViewById(R.id.bt_users_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Backendless.UserService.logout(new AsyncCallback<Void>() {
                    @Override
                    public void handleResponse(Void response) {
                        Log.d("test", "Logout successful");
                        ((MainActivity) getActivity()).startFragmentLogin();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.d("test", "Error while logout: " + fault.getMessage());
                    }
                });
            }
        });

        ImageLoader.getInstance().loadImage(
                Avatar.generateFullPathToAva(Backendless.UserService.CurrentUser().getUserId()),
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//Ava загружена
                        Log.d("test", "Image loaded");
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        ((MainActivity) getActivity()).displayMissingAvatar(true);
                        //No avatar
                        Log.d("test", "Image failed to load " + failReason.toString());
                    }
                });

        //--Drawer
        mDrawerLayout = (DrawerLayout) root.findViewById(R.id.dl_fragment_user_liked_users);
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_DRAGGING) {
                    adapterListViewLikedUsers.notifyDataSetChanged();
                }
            }
        });
        mDrawerList = (ListView) root.findViewById(R.id.lv_fragment_user_liked_users);
        adapterListViewLikedUsers = new AdapterListViewLikedUsers(DataManager.getLikedUsers(), getActivity());
        mDrawerList.setAdapter(adapterListViewLikedUsers);
        //
        return root;
    }
}
