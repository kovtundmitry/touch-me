package tachos.ru.touchme;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivityFragment extends Fragment {
    String ip = "188.233.166.66";
    int port = 11122;

    DataOutputStream out;
    DataInputStream in;

    int oldx = 0;
    int oldy = 0;
    int newx = 0;
    int newy = 0;
    View root;
    ImageView circle;

    Thread receiverThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Socket socket = new Socket(ip, port);
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());
                while (true) {
                    final int x = in.readInt();
                    final int y = in.readInt();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (x == -100) {
                                circle.setVisibility(View.INVISIBLE);
                            } else {
                                circle.setVisibility(View.VISIBLE);
                                ObjectAnimator animX = ObjectAnimator.ofFloat(circle, "x", x * root.getWidth() / 100 - circle.getWidth() / 2);
                                ObjectAnimator animY = ObjectAnimator.ofFloat(circle, "y", y * root.getHeight() / 100 - circle.getHeight() / 2);
                                AnimatorSet animSetXY = new AnimatorSet();
                                animSetXY.setDuration(300);
                                animSetXY.playTogether(animX, animY);
                                animSetXY.start();
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    Thread senderThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    boolean isTouchedStarted = newx == -100 || oldx == -100;
                    boolean moved = oldx != newx || oldy != newy;
                    boolean isCloseEnough = (Math.abs(newx - 100 * (circle.getX() + circle.getWidth() / 2) / root.getWidth()) < 15)
                            && (Math.abs(newy - 100 * (circle.getY() + circle.getHeight() / 2) / root.getHeight()) < 15);
                    if (isTouchedStarted
                            || (moved && isCloseEnough)) {
                        oldx = newx;
                        oldy = newy;
                        out.writeInt(oldx);
                        out.writeInt(oldy);
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_main, container, false);
        circle = (ImageView) root.findViewById(R.id.imageViewCircle);
        receiverThread.start();

        root.findViewById(R.id.button_startServer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                senderThread.start();
            }
        });

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        newx = -100;
                        newy = -100;
                        break;
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        newx = (int) event.getX() * 100 / v.getWidth();
                        newy = (int) event.getY() * 100 / v.getHeight();
                }
                return true;
            }
        });
        return root;
    }
}
