package tachos.ru.touch.me.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import tachos.ru.touch.me.IServer;
import tachos.ru.touch.me.MainActivity;
import tachos.ru.touch.me.R;
import tachos.ru.touch.me.ServerConnection;
import tachos.ru.touch.me.data.Avatar;
import tachos.ru.touch.me.dialogs.DialogVibroSettings;
import tachos.ru.touch.me.utils.VibrationSettings;

public class FragmentGame extends Fragment implements IServer {
    private static final int xMaskSize = 200;
    private static final int yMaskSize = 200;
    final Paint paintTrans = new Paint();
    final Paint paintPartner = new Paint();
    View root;
    ServerConnection serverConnection;
    ImageView enemyFinger;
    ImageView canvasImageView;
    ImageView linesImageView;
    boolean trans = false;
    int maxDistance = 90;
    float selfLastX;
    float selfLastY;
    float partnerX = -1;
    float partnerY = -1;
    Drawable circleDrawable;
    Handler fingerPainter;
    long lastTimeTouchReceived = 0;
    VibrationSettings vibrationSettings;
    int screenDim;
    private Canvas canvas;
    private Canvas canvasLines;
    private boolean isTouched = false;
    private long nextVibrate = 0;

    static public float percentTransparent(Bitmap bm, int scale) {
        final int width = bm.getWidth();
        final int height = bm.getHeight();
        final int xStep = width / scale;
        final int yStep = height / scale;
        final int xInit = xStep / 2;
        final int yInit = yStep / 2;
        final int xEnd = width - xStep / 2;
        final int yEnd = height - yStep / 2;
        int totalTransparent = 0;
        for (int x = xInit; x < xEnd; x += xStep) {
            for (int y = yInit; y < yEnd; y += yStep) {
                if (bm.getPixel(x, y) == Color.TRANSPARENT) {
                    totalTransparent++;
                }
            }
        }
        return ((float) totalTransparent) / (scale * scale);
    }

    void updateFingerColor(long delay) {
        if (fingerPainter == null) return;
        fingerPainter.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (lastTimeTouchReceived == 0) {
                    updateFingerColor(500);
                    return;
                }
                long timeBetweenTouches = System.currentTimeMillis() - lastTimeTouchReceived;
                if (timeBetweenTouches <= 500) {
                    updateFingerColor(15);
                    circleDrawable.setColorFilter(Color.parseColor("#00FF00"), PorterDuff.Mode.SRC_ATOP);
                    return;
                }
                if (timeBetweenTouches <= 2000) {
                    circleDrawable.setColorFilter(Color.parseColor("#" + Integer.toHexString(((int) timeBetweenTouches * 255 / 2000)) + "FF00"), PorterDuff.Mode.SRC_ATOP);
                    updateFingerColor(15);
                    return;
                }
                if (timeBetweenTouches <= 5000) {
                    circleDrawable.setColorFilter(Color.parseColor("#FF" + Integer.toHexString(Math.max(255 - ((int) (timeBetweenTouches - 2000) * 255 / 3000), 16)) + "00"), PorterDuff.Mode.SRC_ATOP);
                    updateFingerColor(15);
                    return;
                }
                circleDrawable.setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.SRC_ATOP);
            }
        }, delay);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        circleDrawable = getActivity().getResources().getDrawable(R.drawable.circle);
        fingerPainter = new Handler();
        root = inflater.inflate(R.layout.fragment_game, container, false);
        root.findViewById(R.id.bt_fragment_game_vibro_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogVibroSettings dialog = new DialogVibroSettings();
                dialog.setCancelable(false);
                dialog.show(getActivity().getFragmentManager(), "DialogVibroSettings");
                dialog.setConfigChangedListener(new DialogVibroSettings.InterfaceDialogVibroSettings() {
                    @Override
                    public void onConfigChanged() {
                        vibrationSettings = new VibrationSettings(getActivity());
                    }
                });
            }
        });
        ImageLoader.getInstance().displayImage(Avatar.generateFullPathToAva(MainActivity.partnerId), (ImageView) root.findViewById(R.id.imageViewBack));

        enemyFinger = (ImageView) root.findViewById(R.id.enemyFinger);
        enemyFinger.setBackgroundDrawable(circleDrawable);
        canvasImageView = (ImageView) root.findViewById(R.id.imageViewCover);
        linesImageView = (ImageView) root.findViewById(R.id.imageViewCanvas);
        paintTrans.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        serverConnection = new ServerConnection(this, ((MainActivity) getActivity()).partnerId);
        final Paint paintSelf = new Paint();
        paintSelf.setColor(Color.rgb(0, 255, 0));
        float mmSize = 0.5f;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mmSize, dm);
        paintSelf.setStrokeWidth(strokeWidth);
        paintPartner.setColor(Color.rgb(0, 0, 255));
        paintPartner.setStrokeWidth(strokeWidth);

        canvasImageView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getVisibility() != View.VISIBLE) return false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        isTouched = false;
                        serverConnection.sendCoords(-20, -20);
                        stopVibrate();
                        break;
                    case MotionEvent.ACTION_DOWN:
                        isTouched = true;
                        selfLastX = event.getX();
                        selfLastY = event.getY();
                        //canvas.drawColor(Color.WHITE);
                        //canvasImageView.invalidate();
                        serverConnection.sendCoords((int) ((event.getX() >= 0) ? event.getX() : 0) * xMaskSize / canvasImageView.getWidth(),
                                (int) ((event.getY() >= 0) ? event.getY() : 0) * yMaskSize / canvasImageView.getHeight());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // if (enemyFinger.getVisibility() != View.VISIBLE) break;
                        double xOffset = Math.abs(enemyFinger.getX() + enemyFinger.getWidth() / 2 - event.getX());
                        double yOffset = Math.abs(enemyFinger.getY() + enemyFinger.getHeight() / 2 - event.getY());
                        vibrate(Math.sqrt(Math.pow(yOffset, 2) + Math.pow(xOffset, 2)));
                        /*if (xOffset > maxDistance)
                            break;
                        if (yOffset > maxDistance)
                            break;*/
                        canvasLines.drawLine(selfLastX, selfLastY, event.getX(), event.getY(), paintSelf);
                        selfLastX = event.getX();
                        selfLastY = event.getY();
                        linesImageView.invalidate();

                        if (serverConnection.isPaired()) {
                            serverConnection.sendCoords((int) ((event.getX() >= 0) ? event.getX() : 0) * xMaskSize / canvasImageView.getWidth(),
                                    (int) ((event.getY() >= 0) ? event.getY() : 0) * yMaskSize / canvasImageView.getHeight());
                            if (enemyFinger.getVisibility() == View.VISIBLE && xOffset <= maxDistance && yOffset <= maxDistance)
                                scratchCover((int) event.getX(), (int) event.getY(), canvas, canvasImageView);
                            //canvas.drawLine(selfLastX, selfLastY, event.getX(), event.getY(), paintSelf);
                            canvasImageView.invalidate();
                        }
                        break;
                }
                return true;
            }
        });

        root.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(canvasImageView.getWidth(), canvasImageView.getHeight(), Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                canvasImageView.setImageBitmap(bitmap);
                canvas.drawColor(Color.BLACK);
                canvasImageView.invalidate();

                Bitmap bitmapLines = Bitmap.createBitmap(linesImageView.getWidth(), linesImageView.getHeight(), Bitmap.Config.ARGB_8888);
                canvasLines = new Canvas(bitmapLines);
                linesImageView.setImageBitmap(bitmapLines);
                linesImageView.invalidate();
                screenDim = Math.max(root.getWidth(), root.getHeight());
            }
        });
        return root;
    }

    @Override
    public void connectionSuccessful() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void connectionFailed() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void disconnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void paired() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                canvasImageView.setVisibility(View.VISIBLE);
                root.findViewById(R.id.imageViewBack).setVisibility(View.VISIBLE);
            }
        });
    }

    private void scratchCover(int x, int y, Canvas canvas, ImageView view) {
        canvas.drawCircle(x, y, canvasImageView.getWidth() / 8, paintTrans);
        view.invalidate();
        if (trans) return;
        int transparentPercent = (int) (percentTransparent(((BitmapDrawable) view.getDrawable()).getBitmap(), 10) * 100);
        if (transparentPercent > 50) {
            serverConnection.sendCoords(-121, 0);
            hideCover();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        vibrationSettings = new VibrationSettings(getActivity());
    }

    @Override
    public void onPause() {
        stopVibrate();
        fingerPainter = null;
        vibrationSettings.saveSettings(getActivity());
        super.onPause();
        Log.d("test", "pause");
    }

    private void hideCover() {
        if (trans) return;
        trans = true;
        new Thread(new Runnable() {
            int alph = 255;

            @Override
            public void run() {
                while (alph >= 0) {
                    if (getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                alph--;
                                if (alph >= 0) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                        canvasImageView.setImageAlpha(alph);
                                    else canvasImageView.setAlpha(alph);
                                    canvasImageView.invalidate();
                                } else {
                                    canvasImageView.setVisibility(View.GONE);
                                    enemyFinger.setVisibility(View.GONE);
                                }
                            }
                        });
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ((Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE)).cancel();
                canvasImageView.setVisibility(View.INVISIBLE);
            }
        }).start();

    }

    @Override
    public void coordsReceived(final int x, final int y) {
        enemyFinger.post(new Runnable() {
            @Override
            public void run() {
                if (x == -121) {
                    hideCover();
                    return;
                }
                int localX = x * canvasImageView.getWidth() / xMaskSize;
                int localY = y * canvasImageView.getHeight() / yMaskSize;

                if (x != -20 && y != -20) {
                    if (isTouched && Math.abs(selfLastX - localX) <= maxDistance && Math.abs(selfLastY - localY) <= maxDistance)
                        scratchCover(localX, localY, canvas, canvasImageView);
                    circleDrawable.setColorFilter(Color.parseColor("#00FF00"), PorterDuff.Mode.SRC_ATOP);
                    lastTimeTouchReceived = System.currentTimeMillis();
                    updateFingerColor(0);
                    enemyFinger.setVisibility(View.VISIBLE);
                    enemyFinger.setX(localX - enemyFinger.getWidth() / 2);
                    enemyFinger.setY(localY - enemyFinger.getHeight() / 2);

                    if (partnerX >= 0)
                        canvasLines.drawLine(partnerX, partnerY, localX, localY, paintPartner);
                    partnerX = localX;
                    partnerY = localY;
                    linesImageView.invalidate();
                    //!!
                    /*GradientDrawable myGrad = (GradientDrawable)enemyFinger.getBackground();
                    myGrad.setStroke(2, Color.GREEN);*/
                    //!!
                } else {
                    //Partner finger's up
                    stopVibrate();
                    partnerX = -1;
                    partnerY = -1;
                    enemyFinger.setVisibility(View.INVISIBLE);
                    lastTimeTouchReceived = 0;
                }
            }
        });
    }

    private void vibrate(double distanceBetweenFingers) {
        if (!vibrationSettings.isVibrationEnabled()) return;
        if (nextVibrate > System.currentTimeMillis()) return;
        distanceBetweenFingers = 100 * distanceBetweenFingers / screenDim;
        long vibrationLength = (long) (vibrationSettings.getLengthMinValue() + distanceBetweenFingers * vibrationSettings.getLengthMultiplier());
        long pauseLength = (long) (vibrationSettings.getPauseMinValue() + distanceBetweenFingers * vibrationSettings.getPauseMultiplier());
        if (vibrationLength < 0) vibrationLength = 0;
        if (pauseLength < 0) pauseLength = 0;
        long[] pattern = {0, vibrationLength, pauseLength};
        ((Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(pattern, 0);
        nextVibrate = System.currentTimeMillis() + pauseLength + vibrationLength;
    }

    private void stopVibrate() {
        ((Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE)).cancel();
    }
}
