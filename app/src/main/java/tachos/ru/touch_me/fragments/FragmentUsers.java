package tachos.ru.touch_me.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.ArrayList;

import tachos.ru.touch_me.MainActivity;
import tachos.ru.touch_me.Messenger;
import tachos.ru.touch_me.R;
import tachos.ru.touch_me.adapters.AdapterListViewUsers;
import tachos.ru.touch_me.data.DataManager;
import tachos.ru.touch_me.data.Users;

public class FragmentUsers extends Fragment {
    ListView lvUsers;
    AdapterListViewUsers adapterUsers;
    ArrayList<Users> users = new ArrayList<>();

    private void startUsersUpdater(long time) {
        if (getActivity() == null) return;
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                DataManager.getUsers(new AsyncCallback<BackendlessCollection<Users>>() {
                    @Override
                    public void handleResponse(BackendlessCollection<Users> response) {
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_users, container, false);
        lvUsers = ((ListView) root.findViewById(R.id.lv_users_list));
        adapterUsers = new AdapterListViewUsers(users, inflater);
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
        startUsersUpdater(0);
        return root;
    }
}
