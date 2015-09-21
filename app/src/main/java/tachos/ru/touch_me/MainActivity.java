package tachos.ru.touch_me;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.backendless.Backendless;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBackendless();
        setContentView(R.layout.activity_main);
    }

    private void initBackendless() {
        String appVersion = "v1";
        Backendless.initApp(this, "365FC299-BD60-33E4-FF58-FC3CD4CF0100", "1C23155A-8B3C-0E24-FF1B-04ED6021E800", appVersion);
    }

}
