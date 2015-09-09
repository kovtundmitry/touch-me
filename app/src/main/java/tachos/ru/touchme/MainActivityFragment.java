package tachos.ru.touchme;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
    private Canvas canvas;
    private int lastDrawX = -20;
    private int lastDrawY = -20;
    private float selfLastX = -20;
    private float selfLastY = -20;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_main, container, false);
        enemyFinger = (ImageView) root.findViewById(R.id.enemyFinger);
        canvasImageView = (ImageView) root.findViewById(R.id.imageViewCanvas);

        buttonConnect = (Button) root.findViewById(R.id.button_connect);
        editTextName = (EditText) root.findViewById(R.id.editText_name);
        refreshList = (Button) root.findViewById(R.id.button_refresh);
        enterQueue = (Button) root.findViewById(R.id.button_enter_queue);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        canvasImageView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getVisibility() != View.VISIBLE) return false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        serverConnection.sendCoords(-20, -20);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        canvas.drawColor(Color.WHITE);
                        canvasImageView.invalidate();
                        selfLastX = event.getX();
                        selfLastY = event.getY();
                        serverConnection.sendCoords((int) ((event.getX() >= 0) ? event.getX() : 0) * xMaskSize / root.getWidth(),
                                (int) ((event.getY() >= 0) ? event.getY() : 0) * yMaskSize / root.getHeight());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("coords", (int) event.getX() / xMaskSize + " " + (int) event.getY() / yMaskSize);
                        if (serverConnection.isPaired()) {
                            serverConnection.sendCoords((int) ((event.getX() >= 0) ? event.getX() : 0) * xMaskSize / root.getWidth(),
                                    (int) ((event.getY() >= 0) ? event.getY() : 0) * yMaskSize / root.getHeight());
                            canvas.drawLine(selfLastX, selfLastY, event.getX(), event.getY(), paintSelf);
                            selfLastX = event.getX();
                            selfLastY = event.getY();
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
                lv.setVisibility(View.GONE);
                buttonConnect.setVisibility(View.GONE);
                editTextName.setVisibility(View.GONE);
                refreshList.setVisibility(View.GONE);
                enterQueue.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void coordsReceived(final int x, final int y) {
        enemyFinger.post(new Runnable() {
            @Override
            public void run() {
                int localX = x * canvasImageView.getWidth() / xMaskSize;
                int localY = y * canvasImageView.getHeight() / yMaskSize;
                if (lastDrawX != -20 && x != -20) {
                    final Paint paintPartner = new Paint();
                    paintPartner.setColor(Color.rgb(255, 0, 0));
                    float mmSize = 0.5f;
                    DisplayMetrics dm = getResources().getDisplayMetrics();
                    float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mmSize, dm);
                    paintPartner.setStrokeWidth(strokeWidth);
                    canvas.drawLine(lastDrawX, lastDrawY, localX, localY, paintPartner);
                    canvasImageView.invalidate();
                }

                if (x != -20) {
                    enemyFinger.setVisibility(View.VISIBLE);
                    lastDrawX = localX;
                    lastDrawY = localY;
                    enemyFinger.setX(localX - enemyFinger.getWidth() / 2);
                    enemyFinger.setY(localY - enemyFinger.getHeight() / 2);
                } else {
                    enemyFinger.setVisibility(View.INVISIBLE);
                    lastDrawX = -20;
                    lastDrawY = -20;
                }
            }
        });
    }
}
