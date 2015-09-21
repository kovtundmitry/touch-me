package tachos.ru.touch_me;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;

public class FragmentLogin extends Fragment {
    EditText etLogin;
    EditText etName;
    EditText etPassword;

    public FragmentLogin() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);
        etLogin = (EditText) root.findViewById(R.id.et_login_login);
        etName = (EditText) root.findViewById(R.id.et_login_name);
        etPassword = (EditText) root.findViewById(R.id.et_login_password);
        root.findViewById(R.id.bt_login_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(etLogin.getText().toString(), etPassword.getText().toString());
            }
        });
        root.findViewById(R.id.bt_login_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(etLogin.getText().toString(), etName.getText().toString(), etPassword.getText().toString());
            }
        });
        return root;
    }

    private void login(String email, String password) {
        Backendless.UserService.login(email, password, new BackendlessCallback<BackendlessUser>() {
            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getActivity(), "Error logging in: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Registration", "Error logging in: " + fault.getMessage());
            }

            @Override
            public void handleResponse(BackendlessUser backendlessUser) {
                Toast.makeText(getActivity(), backendlessUser.getEmail() + " successfully logged in", Toast.LENGTH_SHORT).show();
                Log.d("Registration", backendlessUser.getEmail() + " successfully logged in");
            }
        });
    }

    private void register(String email, String name, String password) {
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
            }
        });
    }
}
