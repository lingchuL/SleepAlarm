package com.example.sleepalarm;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button SetTime;
    private EditText hourE;
    private EditText minE;
    private EditText lasthE;
    private EditText lastmE;
    private int hour;
    private int min;
    private int lasth;
    private int lastm;
    private String info;
    private MyService.AnnounceBinder binder;
    private MyService m;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder=(MyService.AnnounceBinder)service;
            m=binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            m=null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("SleepAlarm","I'm Created!!!!!!!!!!!!!!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SetTime=(Button)findViewById(R.id.button);
        SetTime.setOnClickListener(this);

        hourE=(EditText)findViewById(R.id.hour);
        minE=(EditText)findViewById(R.id.min);
        lasthE=(EditText)findViewById(R.id.lasth);
        lastmE=(EditText)findViewById(R.id.lastm);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            Window window=getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }


        preferences=getSharedPreferences("config",MODE_PRIVATE);
        if(preferences.getBoolean("intro",true)){
            Log.d("SleepAlarm","ed:"+preferences.getBoolean("intro",true));
            Intent it=new Intent(MainActivity.this,LaunchActivity.class);
            MainActivity.this.startActivity(it);
            //MainActivity.this.finish();
        }else{
            Log.d("SleepAlarm","ed:"+preferences.getBoolean("intro",false));
            editor=preferences.edit();
            editor.putBoolean("intro",true);
            editor.commit();                //一定要提交

            Intent intent=new Intent(this,MyService.class);
            startService(intent);
            bindService(intent,conn, Service.BIND_AUTO_CREATE);
        }
    }

    private int changeTime(){
        hour=Integer.valueOf(hourE.getText().toString()).intValue();
        min=Integer.valueOf(minE.getText().toString()).intValue();
        lasth=Integer.valueOf(lasthE.getText().toString()).intValue();
        lastm=Integer.valueOf(lastmE.getText().toString()).intValue();

        if(hour<=23&&hour>=0&&min<60&&min>=0&&lasth>=0&&lastm>=0){
            if(lasth==0&&lastm==0){
                return 3;
            }else{
                return 0;
            }
        }else if(hour>23||hour<0){
            return 1;
        }else if(min>=60||min<0){
            return 2;
        }else{
            return 3;
        }
    }


    @Override
    public void onClick(View v){
        Intent it=new Intent(this,MyService.class);
        int check;
        //it.putExtra("firstset",hour.getText().toString());
        if(v.getId()==R.id.button){
            check=changeTime();
            if(check==0){
                binder.setTime(hour,min,lasth,lastm);
                startService(it);
                info="设置为"+hour+":"+min+"||"+lasth+":"+lastm;
                Toast.makeText(this,info, Toast.LENGTH_SHORT).show();
            }else if(check==1){
                Toast.makeText(this,"时应介于0-23", Toast.LENGTH_SHORT).show();
            }else if(check==2){
                Toast.makeText(this,"分应介于0-59", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"持续时间应大于且不同时为0", Toast.LENGTH_SHORT).show();
            }

        }
    }
    @Override
    protected void onDestroy(){
        Log.d("SleepAlarm","I'm Destroy!");
        super.onDestroy();
    }
    @Override
    public void finish(){
        Log.d("SleepAlarm","I'm BACK!!!!!!!!!!!!!!");
        moveTaskToBack(true);
    }
}
