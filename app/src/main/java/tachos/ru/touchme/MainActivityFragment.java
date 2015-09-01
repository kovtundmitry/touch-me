package tachos.ru.touchme;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivityFragment extends Fragment {
    final Paint paintSelf = new Paint();
    final Paint paintPartner = new Paint();
    String ip = "185.26.120.147";
    int port = 11122;
    int pingPort = 11123;
    DataOutputStream out;
    DataInputStream in;
    TextView tvPing;
    int myOldX = 0;
    int myOldY = 0;
    int myNewX = 0;
    int myNewY = 0;
    View root;
    ImageView canvasImageView;
    Thread senderThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!senderThread.isInterrupted()) {
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    boolean isTouchedEnded = myNewX < 0 || myOldX < 0;
                    boolean moved = myOldX != myNewX || myOldY != myNewY;
                    boolean isCloseEnough = true || (Math.abs(myNewX - 100 * (canvasImageView.getX() + canvasImageView.getWidth() / 2) / root.getWidth()) < 15)
                            && (Math.abs(myNewY - 100 * (canvasImageView.getY() + canvasImageView.getHeight() / 2) / root.getHeight()) < 15);
                    if (moved && (isCloseEnough || isTouchedEnded)) {
                        myOldX = myNewX;
                        myOldY = myNewY;
                        out.writeInt(myOldX);
                        out.writeInt(myOldY);
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });
    Thread pingThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Socket socket = new Socket(ip, pingPort);
                DataOutputStream pingOut = new DataOutputStream(socket.getOutputStream());
                DataInputStream pingIn = new DataInputStream(socket.getInputStream());
                long sentTime = System.currentTimeMillis();
                pingOut.writeLong(sentTime);
                while (!pingThread.isInterrupted()) {
                    final long receivedLong = pingIn.readLong();
                    if (receivedLong != sentTime) pingOut.writeLong(receivedLong);
                    else {
                        final long finalSentTime = sentTime;
                        tvPing.post(new Runnable() {
                            @Override
                            public void run() {
                                tvPing.setText("Ping: " + (System.currentTimeMillis() - finalSentTime) + "ms");
                            }
                        });
                        Log.d("ping", (System.currentTimeMillis() - sentTime) + "ms");
                        sentTime = System.currentTimeMillis();
                        pingOut.writeLong(sentTime);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
    private Canvas canvas;
    Thread receiverThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Socket socket = new Socket(ip, port);
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());

                int startX = -100;
                int startY = -100;


                while (!receiverThread.isInterrupted()) {
                    final int x = in.readInt() * root.getWidth() / 100;
                    final int y = in.readInt() * root.getHeight() / 100;
                    Log.d("test", "new coords: " + x + " " + y);

                    if (x < 0) {
                        startX = x;
                        startY = y;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                canvas.drawColor(Color.WHITE);
                                canvasImageView.invalidate();
                            }
                        });
                    } else {
                        if (startX >= 0) {
                            final int finalStartX = startX;
                            final int finalStartY = startY;
                            startX = x;
                            startY = y;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    canvas.drawLine(finalStartX, finalStartY, x, y, paintPartner);
                                    canvasImageView.invalidate();
                                }
                            });
                        } else {
                            startX = x;
                            startY = y;
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    public void onStop() {
        super.onStop();
        pingThread.interrupt();
        senderThread.interrupt();
        receiverThread.interrupt();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_main, container, false);
        canvasImageView = (ImageView) root.findViewById(R.id.imageViewCanvas);
        receiverThread.start();

        paintSelf.setColor(Color.rgb(255, 0, 0));
        paintSelf.setStrokeWidth(6);
        paintPartner.setColor(Color.rgb(0, 255, 0));
        paintPartner.setStrokeWidth(6);
        root.findViewById(R.id.button_startServer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                senderThread.start();
                pingThread.start();
            }
        });
        tvPing = (TextView) root.findViewById(R.id.textViewPing);
        root.setOnTouchListener(new View.OnTouchListener() {
            float oldSelfX = -1;
            float oldSelfY = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        myNewX = -100;
                        myNewY = -100;
                        oldSelfX = -1;
                        oldSelfY = -1;
                        canvas.drawColor(Color.WHITE);
                        canvasImageView.invalidate();
                        break;
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        if (oldSelfX >= 0)
                            canvas.drawLine(oldSelfX, oldSelfY, event.getX(), event.getY(), paintSelf);
                        oldSelfX = event.getX();
                        oldSelfY = event.getY();
                        canvasImageView.invalidate();
                        myNewX = (int) event.getX() * 100 / v.getWidth();
                        myNewY = (int) event.getY() * 100 / v.getHeight();
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
}
