package tachos.ru.touch.me.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import tachos.ru.touch.me.R;
import tachos.ru.touch.me.utils.VibrationSettings;

public class DialogVibroSettings extends DialogFragment {
    SeekBar sbVibrationMin;
    SeekBar sbVibrationRatio;
    SeekBar sbPauseMin;
    SeekBar sbPauseRatio;
    TextView tvVibrationMin;
    TextView tvVibrationRatio;
    TextView tvPauseMin;
    TextView tvPauseRatio;
    CheckBox cbIsVibroEnabled;
    VibrationSettings vibrationSettings;
    private InterfaceDialogVibroSettings listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        vibrationSettings = new VibrationSettings(getActivity());

        View root = inflater.inflate(R.layout.dialog_vibro_settings, null);
        SeekBar.OnSeekBarChangeListener sbChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (seekBar.getId()) {
                    case R.id.sb_dialog_vibro_settings_vibration_min: {
                        tvVibrationMin.setText(getResources().getString(R.string.dialog_vibro_length_min, progress - 150));
                        vibrationSettings.setLengthMinValue(progress - 150);
                        break;
                    }
                    case R.id.sb_dialog_vibro_settings_vibration_ratio: {
                        tvVibrationRatio.setText(getResources().getString(R.string.dialog_vibro_vibration_ratio, ((float) progress / 100) - 1.0));
                        vibrationSettings.setLengthMultiplier((float) progress / 100 - 1);
                        break;
                    }
                    case R.id.sb_dialog_vibro_settings_pause_min: {
                        tvPauseMin.setText(getResources().getString(R.string.dialog_vibro_pause_min, progress - 150));
                        vibrationSettings.setPauseMinValue(progress - 150);
                        break;
                    }
                    case R.id.sb_dialog_vibro_settings_pause_ratio: {
                        tvPauseRatio.setText(getResources().getString(R.string.dialog_vibro_pause_ratio, ((float) progress / 100) - 1.0));
                        vibrationSettings.setPauseMultiplier((float) progress / 100 - 1);
                        break;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
        sbVibrationMin = (SeekBar) root.findViewById(R.id.sb_dialog_vibro_settings_vibration_min);
        sbVibrationMin.setProgress((int) vibrationSettings.getLengthMinValue() + 150);
        sbVibrationMin.setOnSeekBarChangeListener(sbChangeListener);
        sbVibrationRatio = (SeekBar) root.findViewById(R.id.sb_dialog_vibro_settings_vibration_ratio);
        sbVibrationRatio.setProgress((int) (vibrationSettings.getLengthMultiplier() * 100) + 100);
        sbVibrationRatio.setOnSeekBarChangeListener(sbChangeListener);
        sbPauseMin = (SeekBar) root.findViewById(R.id.sb_dialog_vibro_settings_pause_min);
        sbPauseMin.setProgress((int) vibrationSettings.getPauseMinValue() + 150);
        sbPauseMin.setOnSeekBarChangeListener(sbChangeListener);
        sbPauseRatio = (SeekBar) root.findViewById(R.id.sb_dialog_vibro_settings_pause_ratio);
        sbPauseRatio.setProgress((int) (vibrationSettings.getPauseMultiplier() * 100) + 100);
        sbPauseRatio.setOnSeekBarChangeListener(sbChangeListener);

        cbIsVibroEnabled = (CheckBox) root.findViewById(R.id.cb_dialog_vibro_settings_vibro_enabled);
        cbIsVibroEnabled.setChecked(vibrationSettings.isVibrationEnabled());
        cbIsVibroEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                vibrationSettings.setVibrationEnabled(isChecked);
            }
        });

        tvVibrationMin = (TextView) root.findViewById(R.id.tv_dialog_vibro_settings_vibration_min);
        tvVibrationMin.setText(getResources().getString(R.string.dialog_vibro_length_min, vibrationSettings.getLengthMinValue()));
        tvVibrationRatio = (TextView) root.findViewById(R.id.tv_dialog_vibro_settings_vibration_ratio);
        tvVibrationRatio.setText(getResources().getString(R.string.dialog_vibro_vibration_ratio, vibrationSettings.getLengthMultiplier()));
        tvPauseMin = (TextView) root.findViewById(R.id.tv_dialog_vibro_settings_pause_min);
        tvPauseMin.setText(getResources().getString(R.string.dialog_vibro_pause_min, vibrationSettings.getPauseMinValue()));
        tvPauseRatio = (TextView) root.findViewById(R.id.tv_dialog_vibro_settings_pause_ratio);
        tvPauseRatio.setText(getResources().getString(R.string.dialog_vibro_pause_ratio, vibrationSettings.getPauseMultiplier()));

        builder.setView(root)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        vibrationSettings.saveSettings(getActivity());
                        if (listener != null) listener.onConfigChanged();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    public void setConfigChangedListener(InterfaceDialogVibroSettings listener) {
        this.listener = listener;
    }

    public interface InterfaceDialogVibroSettings {
        void onConfigChanged();
    }
}