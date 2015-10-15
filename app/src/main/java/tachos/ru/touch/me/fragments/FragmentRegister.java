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
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;

import tachos.ru.touch.me.MainActivity;
import tachos.ru.touch.me.Messenger;
import tachos.ru.touch.me.R;
import tachos.ru.touch.me.data.DataManager;

public class FragmentRegister extends Fragment {
    EditText etLogin;
    EditText etName;
    EditText etPassword;
    CheckBox cbStayLoggedIn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_register, container, false);
        etLogin = (EditText) root.findViewById(R.id.et_login_login);
        etName = (EditText) root.findViewById(R.id.et_login_name);
        etPassword = (EditText) root.findViewById(R.id.et_login_password);
        cbStayLoggedIn = (CheckBox) root.findViewById(R.id.cb_fragment_register_stay);
        root.findViewById(R.id.bt_login_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
                register(etLogin.getText().toString(), etName.getText().toString(), etPassword.getText().toString(), true);
            }
        });
        root.findViewById(R.id.bt_fragment_register_allready_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
                ((MainActivity) getActivity()).startFragmentLogin();
            }
        });
        return root;
    }

    private void register(final String email, String name, final String password, final boolean stayLoggedIn) {
        BackendlessUser user = new BackendlessUser();
        user.setEmail(email);
        user.setProperty("name", name);
        user.setPassword(password);
        Backendless.UserService.register(user, new BackendlessCallback<BackendlessUser>() {
            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getActivity(), "Error registering: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Registration", "Error registering: " + fault.getMessage());
            }

            @Override
            public void handleResponse(BackendlessUser backendlessUser) {
                Toast.makeText(getActivity(), backendlessUser.getEmail() + " successfully registered", Toast.LENGTH_SHORT).show();
                Log.d("Registration", backendlessUser.getEmail() + " successfully registered");
                FragmentLogin.login(email, password, cbStayLoggedIn.isChecked(), new BackendlessCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser response) {
                        Toast.makeText(getActivity(), response.getEmail() + " successfully logged in", Toast.LENGTH_SHORT).show();
                        DataManager.startLastActivityUpdater();
                        Messenger.registerDevice();
                        ((MainActivity) getActivity()).startFragmentUsers();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getActivity(), "Error logging in: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).startFragmentLogin();
                    }
                });
            }
        });
    }
}
