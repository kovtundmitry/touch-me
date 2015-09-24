package tachos.ru.touch_me;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.ArrayList;

public class FragmentUsers extends Fragment {
    ListView lvUsers;
    AdapterListViewUsers adapterUsers;
    ArrayList<Users> users = new ArrayList<>();

    private void startUsersUpdater() {
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
                        Log.d("test", "users acquired");
                        Log.d("test", "users acquired " + response.getData());
                        startUsersUpdater();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.d("test", "error acquiring users: " + fault.getMessage());
                        startUsersUpdater();
                    }
                });
            }
        }, 10000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_users, container, false);
        lvUsers = ((ListView) root.findViewById(R.id.lv_users_list));
        adapterUsers = new AdapterListViewUsers(users, inflater);
        lvUsers.setAdapter(adapterUsers);
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
        startUsersUpdater();
        return root;
    }
}
