package com.example.mustafa.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class JoyStickClass {

    MainActivity activity;
    ServerSocket serverSocket;

    static final int socketServerPORT = 8080;
    public String x = "0",y = "0",guc = "0";

    private int STICK_ALPHA = 120;
    private int LAYOUT_ALPHA = 120;
    private int OFFSET = 0;

    private Context mContext;
    private ViewGroup mLayout;
    private LayoutParams params;
    private int stick_width, stick_height;

    private float position_x = 0, position_y = 0, min_distance = 0;
    private float distance = 0, angle = 0;

    private DrawCanvas draw;
    private Paint paint;
    private Bitmap stick;

    private boolean touch_state = false;

    public JoyStickClass(MainActivity activity) {
        this.activity = activity;
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    public JoyStickClass (Context context, ViewGroup layout, int stick_res_id) {
        mContext = context;

        stick = BitmapFactory.decodeResource(mContext.getResources(), stick_res_id);

        stick_width = stick.getWidth();
        stick_height = stick.getHeight();

        draw = new DrawCanvas(mContext);
        paint = new Paint();
        mLayout = layout;
        params = mLayout.getLayoutParams();
    }

    public void drawStick(MotionEvent arg1) {
        position_x = (arg1.getX() - (params.width / 2));
        position_y = (arg1.getY() - (params.height / 2)) ;
        distance = (float) Math.sqrt(Math.pow(position_x, 2) + Math.pow(position_y, 2));
        angle = (float) cal_angle(position_x, position_y);


        if(arg1.getAction() == MotionEvent.ACTION_DOWN) {
            if(distance <= (params.width / 2) - OFFSET) {
                position_x = (arg1.getX() - (params.width / 2));
                position_y =-(arg1.getY() - (params.height / 2));
                draw.position(arg1.getX(), arg1.getY());
                draw();
                touch_state = true;
            }
        } else if(arg1.getAction() == MotionEvent.ACTION_MOVE && touch_state) {
            if(distance <= (params.width / 2) - OFFSET) {
                position_x = (arg1.getX() - (params.width / 2));
                position_y = -(arg1.getY() - (params.height / 2));
                draw.position(arg1.getX(), arg1.getY());
                draw();
            } else if(distance > (params.width / 2) - OFFSET){
                float x = (float) (Math.cos(Math.toRadians(cal_angle(position_x, position_y)))
                        * ((params.width / 2) - OFFSET));
                float y = (float) (Math.sin(Math.toRadians(cal_angle(position_x, position_y)))
                        * ((params.height / 2) - OFFSET));
                x += (params.width / 2);
                y += (params.height / 2);
                position_x =  (x - (params.width / 2));
                position_y = -(y - (params.height / 2));
                draw.position(x, y);
                draw();
            } else {
                mLayout.removeView(draw);
            }
        } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
            mLayout.removeView(draw);
            touch_state = false;
        }
        else if(arg1.getAction() == MotionEvent.ACTION_HOVER_ENTER)
        {
            draw.position(arg1.getX(), arg1.getY());
            draw();
        }
    }

    public float[] getPosition() {
        if(distance > min_distance && touch_state) {
            return new float[] { position_x, position_y };
        }
        return new float[] { 0, 0 };
    }

    public int getX() {
        if(distance > min_distance && touch_state) {
            return (int) position_x;
        }
        return 0;
    }

    public int getY() {
        if(distance > min_distance && touch_state) {
            return (int) position_y;
        }
        return 0;
    }

    public float getAngle() {
        if(distance > min_distance && touch_state) {
            return angle;
        }
        return 0;
    }

    public float getDistance() {
        if(distance > min_distance && touch_state) {
            return distance;
        }
        return 0;
    }

    public void setMinimumDistance(int minDistance) {
        min_distance = minDistance;
    }

    public float getMinimumDistance() {
        return min_distance;
    }



    public void setOffset(int offset) {
        OFFSET = offset;
    }

    public int getOffset() {
        return OFFSET;
    }

    public void setStickAlpha(int alpha) {
        STICK_ALPHA = alpha;
        paint.setAlpha(alpha);
    }

    public int getStickAlpha() {
        return STICK_ALPHA;
    }

    public void setLayoutAlpha(int alpha) {
        LAYOUT_ALPHA = alpha;
        mLayout.getBackground().setAlpha(alpha);
    }

    public int getLayoutAlpha() {
        return LAYOUT_ALPHA;
    }

    public void setStickSize(int width, int height) {
        stick = Bitmap.createScaledBitmap(stick, width, height, false);
        stick_width = stick.getWidth();
        stick_height = stick.getHeight();
    }

    public void setStickWidth(int width) {
        stick = Bitmap.createScaledBitmap(stick, width, stick_height, false);
        stick_width = stick.getWidth();
    }

    public void setStickHeight(int height) {
        stick = Bitmap.createScaledBitmap(stick, stick_width, height, false);
        stick_height = stick.getHeight();
    }

    public int getStickWidth() {
        return stick_width;
    }

    public int getStickHeight() {
        return stick_height;
    }

    public void setLayoutSize(int width, int height) {
        params.width = width;
        params.height = height;
    }

    public int getLayoutWidth() {
        return params.width;
    }

    public int getLayoutHeight() {
        return params.height;
    }

    private double cal_angle(float x, float y) {
        if(x >= 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x));
        else if(x < 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if(x < 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if(x >= 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 360;
        return 0;
    }

    private void draw() {
        try {
            mLayout.removeView(draw);
        } catch (Exception e) { }
        mLayout.addView(draw);
    }

    private class DrawCanvas extends View{
        float x, y;

        private DrawCanvas(Context mContext) {
            super(mContext);
        }

        public void onDraw(Canvas canvas) {
            canvas.drawBitmap(stick, x, y, paint);
        }

        private void position(float pos_x, float pos_y) {
            x = pos_x - (stick_width / 2);
            y = pos_y - (stick_height / 2);
        }
    }

    ///////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////
    ////////////////////////SERVER/////////////////////////////////



    public int getPort() {
        return socketServerPORT;
    }

    public void onDestroy() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

   /* public void setGuc(String x)
    {
        this.guc = x;
    }*/

    private class SocketServerThread extends Thread {

        int count = 0;

        @Override
        public void run() {
            try {
                // create ServerSocket using specified port
                serverSocket = new ServerSocket(socketServerPORT);

                while (true) {
                    // block the call until connection is created and return
                    // Socket object
                    Socket socket = serverSocket.accept();
                    count++;


                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });

                    SocketServerReplyThread socketServerReplyThread =
                            new SocketServerReplyThread(socket, count);
                    socketServerReplyThread.run();

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class SocketServerReplyThread extends Thread {

        private Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
        }

        @Override
        public void run() {
            OutputStream outputStream;


            try {
                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(guc + " " + x + " " + y + " ");
                printStream.close();


                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                    }
                });

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                }
            });
        }

    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Server running at : "
                                + inetAddress.getHostAddress() + " ";
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }
}
