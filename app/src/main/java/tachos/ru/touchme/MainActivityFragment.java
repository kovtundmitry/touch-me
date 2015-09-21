package tachos.ru.touchme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivityFragment extends Fragment implements IServer {
    private static final int xMaskSize = 200;
    private static final int yMaskSize = 200;
    final Paint paintTrans = new Paint();
    final Paint paintPartner = new Paint();
    View root;
    ServerConnection serverConnection;
    ListView lv;
    ArrayList<String> namesList = new ArrayList<>();
    ArrayAdapter<String> adapter;
    Button buttonConnect;
    Button refreshList;
    Button enterQueue;
    EditText editTextName;
    ImageView enemyFinger;
    ImageView canvasImageView;
    ImageView linesImageView;
    boolean trans = false;
    int maxDistance = 90;
    float selfLastX;
    float selfLastY;
    float partnerX = -1;
    float partnerY = -1;
    private Canvas canvas;
    private Canvas canvasLines;
    private boolean isTouched = false;

    static public float percentTransparent(Bitmap bm, int scale) {

        final int width = bm.getWidth();
        final int height = bm.getHeight();

        // size of sample rectangles
        final int xStep = width / scale;
        final int yStep = height / scale;

        // center of the first rectangle
        final int xInit = xStep / 2;
        final int yInit = yStep / 2;

        // center of the last rectangle
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_main, container, false);
        enemyFinger = (ImageView) root.findViewById(R.id.enemyFinger);
        canvasImageView = (ImageView) root.findViewById(R.id.imageViewCover);
        linesImageView = (ImageView) root.findViewById(R.id.imageViewCanvas);
        paintTrans.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        buttonConnect = (Button) root.findViewById(R.id.button_connect);
        editTextName = (EditText) root.findViewById(R.id.editText_name);
        refreshList = (Button) root.findViewById(R.id.button_refresh);
        enterQueue = (Button) root.findViewById(R.id.button_enter_queue);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

// Check if no view has focus:
                View view = getActivity().getCurrentFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                buttonConnect.setVisibility(View.GONE);
                editTextName.setVisibility(View.GONE);
                serverConnection = new ServerConnection(editTextName.getText().toString(), MainActivityFragment.this);
            }
        });
        refreshList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                namesList.clear();
                adapter.notifyDataSetChanged();
                serverConnection.getQueue();
            }
        });
        enterQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterQueue.setVisibility(View.GONE);
                serverConnection.enterQueue();
            }
        });
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, namesList);
        lv = (ListView) root.findViewById(R.id.lv_queue);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) parent.getItemAtPosition(position);
                serverConnection.pairTo(name);
            }
        });
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
                        if (enemyFinger.getVisibility() != View.VISIBLE) break;
                        if (Math.abs(enemyFinger.getX() + enemyFinger.getWidth() / 2 - event.getX()) > maxDistance)
                            break;
                        if (Math.abs(enemyFinger.getY() + enemyFinger.getHeight() / 2 - event.getY()) > maxDistance)
                            break;
                        canvasLines.drawLine(selfLastX, selfLastY, event.getX(), event.getY(), paintSelf);
                        selfLastX = event.getX();
                        selfLastY = event.getY();
                        linesImageView.invalidate();

                        if (serverConnection.isPaired()) {
                            serverConnection.sendCoords((int) ((event.getX() >= 0) ? event.getX() : 0) * xMaskSize / canvasImageView.getWidth(),
                                    (int) ((event.getY() >= 0) ? event.getY() : 0) * yMaskSize / canvasImageView.getHeight());
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

            }
        });
        return root;
    }

    @Override
    public void clientReceived(final String name) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("test", name + " added");
                namesList.add(name);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void connectionSuccessful() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshList.setVisibility(View.VISIBLE);
                enterQueue.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void connectionFailed() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonConnect.setVisibility(View.VISIBLE);
                editTextName.setVisibility(View.VISIBLE);
                refreshList.setVisibility(View.GONE);
                enterQueue.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void disconnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                namesList.clear();
                adapter.notifyDataSetChanged();
                buttonConnect.setVisibility(View.VISIBLE);
                editTextName.setVisibility(View.VISIBLE);
                refreshList.setVisibility(View.GONE);
                enterQueue.setVisibility(View.GONE);
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
                lv.setVisibility(View.GONE);
                buttonConnect.setVisibility(View.GONE);
                editTextName.setVisibility(View.GONE);
                refreshList.setVisibility(View.GONE);
                enterQueue.setVisibility(View.GONE);
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
                    if (isTouched) scratchCover(localX, localY, canvas, canvasImageView);
                    enemyFinger.setVisibility(View.VISIBLE);
                    enemyFinger.setX(localX - enemyFinger.getWidth() / 2);
                    enemyFinger.setY(localY - enemyFinger.getHeight() / 2);

                    if (partnerX >= 0)
                        canvasLines.drawLine(partnerX, partnerY, localX, localY, paintPartner);
                    partnerX = localX;
                    partnerY = localY;
                    linesImageView.invalidate();
                } else {
                    partnerX = -1;
                    partnerY = -1;
                    enemyFinger.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
