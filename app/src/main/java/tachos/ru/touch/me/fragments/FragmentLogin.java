package tachos.ru.touch.me.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.List;

import tachos.ru.touch.me.MainActivity;
import tachos.ru.touch.me.Messenger;
import tachos.ru.touch.me.R;
import tachos.ru.touch.me.data.DataManager;
import tachos.ru.touch.me.data.Users;

public class FragmentLogin extends Fragment {
    EditText etLogin;
    EditText etPassword;
    CheckBox cbStayLoggedIn;

    public static void login(String email, String password, boolean stayLoggedIn, final BackendlessCallback<BackendlessUser> callback) {
        Backendless.UserService.login(email,
                password,
                new BackendlessCallback<BackendlessUser>() {
                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.d("Login", "Error logging in: " + fault.getMessage());
                        callback.handleFault(fault);
                    }

                    @Override
                    public void handleResponse(final BackendlessUser backendlessUser) {
                        Log.d("Login", backendlessUser.getEmail() + " successfully logged in");
                        DataManager.updateLikedUsers(new AsyncCallback<List<Users>>() {
                            @Override
                            public void handleResponse(List<Users> response) {
                                callback.handleResponse(backendlessUser);
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Log.d("Login", "Error logging in: " + fault.getMessage());
                                callback.handleFault(fault);
                            }
                        });
                    }
                },
                stayLoggedIn);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);
        etLogin = (EditText) root.findViewById(R.id.et_login_login);
        etPassword = (EditText) root.findViewById(R.id.et_login_password);
        cbStayLoggedIn = (CheckBox) root.findViewById(R.id.cb_fragment_login_stay);
        root.findViewById(R.id.bt_login_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).startLoading();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
                login(etLogin.getText().toString(), etPassword.getText().toString(), cbStayLoggedIn.isChecked(), new BackendlessCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser response) {
                        if (getActivity()==null)return;
                        Toast.makeText(getActivity(), response.getEmail() + " successfully logged in", Toast.LENGTH_SHORT).show();
                        DataManager.startLastActivityUpdater();
                        Messenger.registerDevice();
                        ((MainActivity) getActivity()).startFragmentUsers();
                        ((MainActivity) getActivity()).stopLoading();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        if (getActivity()==null)return;
                        Toast.makeText(getActivity(), "Error logging in: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).stopLoading();
                    }
                });
            }
        });
        root.findViewById(R.id.bt_fragment_login_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).startFragmentRegister();
            }
        });
        return root;
    }
}
