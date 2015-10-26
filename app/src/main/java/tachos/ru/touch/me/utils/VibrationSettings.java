package tachos.ru.touch.me.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class VibrationSettings {

    private long lengthMinValue;
    private float lengthMultiplier;
    private long pauseMinValue;
    private float pauseMultiplier;
    private boolean strongerWhenFarther;
    private boolean vibrationEnabled;

    public VibrationSettings(Context context) {
        SharedPreferences sharedPref = getPrefs(context);
        lengthMinValue = sharedPref.getLong("lengthMinValue", 20);
        lengthMultiplier = sharedPref.getFloat("lengthMultiplier", 0.0f);
        pauseMinValue = sharedPref.getLong("pauseMinValue", 10);
        pauseMultiplier = sharedPref.getFloat("pauseMultiplier", 1.0f);
        strongerWhenFarther = sharedPref.getBoolean("strongerWhenFarther", true);
        vibrationEnabled = sharedPref.getBoolean("vibrationEnabled", true);
    }

    public void saveSettings(Context context) {
        SharedPreferences.Editor edit = getPrefs(context).edit();
        edit.putLong("lengthMinValue", lengthMinValue);
        edit.putFloat("lengthMultiplier", lengthMultiplier);
        edit.putLong("pauseMinValue", pauseMinValue);
        edit.putFloat("pauseMultiplier", pauseMultiplier);
        edit.putBoolean("strongerWhenFarther", strongerWhenFarther);
        edit.putBoolean("vibrationEnabled", vibrationEnabled);
        edit.commit();
    }

    private SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(context.getPackageName() + ".vibrationSettings", Context.MODE_PRIVATE);
    }

    public float getPauseMultiplier() {
        return pauseMultiplier;
    }

    public void setPauseMultiplier(float pauseMultiplier) {
        this.pauseMultiplier = pauseMultiplier;
    }

    public float getLengthMultiplier() {
        return lengthMultiplier;
    }

    public void setLengthMultiplier(float lengthMultiplier) {
        this.lengthMultiplier = lengthMultiplier;
    }

    public long getLengthMinValue() {
        return lengthMinValue;
    }

    public void setLengthMinValue(long lengthMinValue) {
        this.lengthMinValue = lengthMinValue;
    }

    public long getPauseMinValue() {
        return pauseMinValue;
    }

    public void setPauseMinValue(long pauseMinValue) {
        this.pauseMinValue = pauseMinValue;
    }

    public boolean isStrongerWhenFarther() {
        return strongerWhenFarther;
    }

    public void setStrongerWhenFarther(boolean strongerWhenFarther) {
        this.strongerWhenFarther = strongerWhenFarther;
    }

    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }

    public void setVibrationEnabled(boolean vibrationEnabled) {
        this.vibrationEnabled = vibrationEnabled;
    }
}
