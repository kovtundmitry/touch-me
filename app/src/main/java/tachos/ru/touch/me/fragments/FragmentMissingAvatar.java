package tachos.ru.touch.me.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import tachos.ru.touch.me.MainActivity;
import tachos.ru.touch.me.R;
import tachos.ru.touch.me.data.Avatar;

public class FragmentMissingAvatar extends Fragment {
    private static final int REQUEST_CODE_PICTURE_SELECT = 1717;
    private static final int REQUEST_CODE_PICTURE_CROP = 1718;
    private static final int REQUEST_CODE_CAMERA = 1719;
    private static FragmentMissingAvatar instance = null;

    public FragmentMissingAvatar() {
        instance = this;
    }

    public static FragmentMissingAvatar getInstance() {
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_missing_avatar, container, false);
        root.findViewById(R.id.bt_missing_avatar_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
            }
        });
        root.findViewById(R.id.bt_missing_avatar_take_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, REQUEST_CODE_PICTURE_SELECT);
            }
        });
        return root;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("test", "Activity result " + requestCode + " " + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                if (photo != null) {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), photo, "temp", null);
                    performCrop(Uri.parse(path));
                }
                break;
            case REQUEST_CODE_PICTURE_SELECT:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = data.getData();
                    Log.d("test", "Uri: " + selectedImage);
                    performCrop(selectedImage);
                    break;
                }
            case REQUEST_CODE_PICTURE_CROP:
                if (resultCode == Activity.RESULT_OK) {
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
                                    ((MainActivity) getActivity()).displayMissingAvatar(false);
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
            Toast toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
