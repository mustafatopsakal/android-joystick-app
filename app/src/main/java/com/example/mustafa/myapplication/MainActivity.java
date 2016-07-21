package com.example.mustafa.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.app.Activity;
import android.widget.Toast;


import java.text.BreakIterator;
import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {
    RelativeLayout layout_joystick;
    TextView textView1, textView2, textView3;
    TextView infoip;
    JoyStickClass js;
    JoyStickClass jsServer;
    SeekBar seekBar;
    Button button;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        infoip = (TextView) findViewById(R.id.textView5);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        button= (Button) findViewById(R.id.button);

        jsServer = new JoyStickClass(this);

        infoip.setText(jsServer.getIpAddress() + ":" + jsServer.getPort());

        layout_joystick = (RelativeLayout)findViewById(R.id.layout_joystick);


        //JOYSTİCK SETLEMELERİ
        js = new JoyStickClass(getApplicationContext(), layout_joystick, R.drawable.image_button);
        js.setStickSize(90, 90); //İçteki daire
        js.setLayoutSize(300, 300); //JoyStick boyutu(Dıştaki daire)
        js.setLayoutAlpha(90);
        js.setStickAlpha(60);
        js.setOffset(54);
        js.setMinimumDistance(1);

        //PALETTE SETLEMELERİ
        seekBar.setEnabled(false);
        layout_joystick.setEnabled(false);
        jsServer.x = String.valueOf(0);
        jsServer.y = String.valueOf(0);
        jsServer.guc = String.valueOf(0);
        button.setText("BASLAT");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (button.getText() == "BASLAT") {
                    button.setText("DURDUR");

                    //BAŞLATA TIKLANILDIĞINDA
                    seekBar.setEnabled(true);
                    layout_joystick.setEnabled(true);

                } else {
                    button.setText("BASLAT");

                    seekBar.setEnabled(false);
                    layout_joystick.setEnabled(false);
                    jsServer.x = String.valueOf(0);
                    jsServer.y = String.valueOf(0);
                    jsServer.guc = String.valueOf(0);

                }
            }
        });

        layout_joystick.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);
                {
                    textView1.setText("X : " + String.valueOf(js.getX()));
                    textView2.setText("Y : " + String.valueOf(js.getY()));
                    jsServer.x = String.valueOf(js.getX());
                    jsServer.y = String.valueOf(js.getY());

                    if(jsServer.x == String.valueOf(0) && jsServer.y == String.valueOf(0) && jsServer.guc == String.valueOf(0))
                    {
                    button.setEnabled(true);
                    }
                    else{
                    button.setEnabled(false);
                    }
                }
                return true;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {

                textView3.setText("Güç : " + Integer.toString(progresValue) + "/" + seekBar.getMax());
                jsServer.guc = String.valueOf(progresValue);

                if(jsServer.x == String.valueOf(0) && jsServer.y == String.valueOf(0) && jsServer.guc == String.valueOf(0))
                {
                    button.setEnabled(true);
                }
                else{
                    button.setEnabled(false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        jsServer.onDestroy();
    }


}
