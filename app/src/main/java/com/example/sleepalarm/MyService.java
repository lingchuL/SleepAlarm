package com.example.sleepalarm;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


public class MyService extends Service {
    int starthour=22;                       //启动小时
    int startminute=00;                     //启动分钟
    int receiversec=5;
    int running=0;                          //是开始运行还是启动
    int nn=0;
    int nnt=0;
    int ni=0;
    int working=0;                          //是否还在当天运行 0为当天 1为第二天（跨零点）
    int endh=0;                             //持续时
    int endmin=10;                          //持续分
    String info;                            //设置时的提示信息
    boolean screenwaked=false;
    private AnnounceBinder binder=new AnnounceBinder();
    Random rand=new Random();
    String tforr;
    private String[] notiword=new String[102];


    int stringnum=98;                       //常用语条数

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate(){

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent,int flag,int startId){
        super.onStartCommand(intent,flag,startId);

        if(nnt==0){
            tforr=notiword[0];
        }else{
            tforr=notiword[rand.nextInt(101)+1];                 //除第一次外，剩下的随机显示
        }

        final NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String chid="SleepAlarm_ch_01";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            CharSequence name="SleepAlarm";
            int importance=NotificationManager.IMPORTANCE_HIGH;
            String Description="早睡身体好";
            NotificationChannel mChannel=new NotificationChannel(chid,name,importance);
            mChannel.setDescription(Description);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        final NotificationCompat.Builder builder=new NotificationCompat.Builder(this,chid)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("睡眠闹钟")
                .setContentText(tforr);         //随机通知内容
        android.support.v4.app.NotificationCompat.BigTextStyle style=new android.support.v4.app.NotificationCompat.BigTextStyle()
                .bigText(tforr)
                .setBigContentTitle("睡眠闹钟");
        builder.setStyle(style);
        builder.setAutoCancel(true);




        new Thread(new Runnable(){
            @Override
            public void run(){
                Date date=new Date();
                if(running==0){
                    Log.d("SleepAlarm","开始执行于"+date.toString());
                    prepare();
                    running=1;
                }else {
                    Log.d("SleepAlarm","执行于"+date.toString());
                    screenwaked=false;
                    PowerManager powerManager=(PowerManager)getSystemService(Context.POWER_SERVICE);
                    screenwaked=powerManager.isScreenOn();

                    if(screenwaked){
                        if(nn!=nnt){
                            notificationManager.notify(ni,builder.build());
                            ni++;
                            nn++;
                            nnt++;
                        }else{
                            prepare();
                            nn++;
                        }
                        Log.d("SleepAlarms","屏幕状态："+screenwaked);
                        Log.d("SleepAlarms",""+nn+"||"+nnt);
                    }

                }

            }
        }).start();

        AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
        Calendar calendar=Calendar.getInstance();
        int hour=calendar.get(Calendar.HOUR_OF_DAY);
        int minute=calendar.get(Calendar.MINUTE);
        int seconds=calendar.get(Calendar.SECOND);
        int holdon=0;



        if(hour<(starthour+endh+(startminute+endmin)/60)&&hour>=7){
            holdon=((starthour-hour)*60*60+(startminute-minute)*60-(seconds)-receiversec)*1000;           //时未达到钟点
        }else if(hour==(starthour+endh+(startminute+endmin)/60)&&hour>=7){
            if(minute<(startminute+endmin)%60){
                holdon=((starthour-hour)*60*60+(startminute-minute)*60-(seconds)-receiversec)*1000;           //时达到，分未达到终点
            }else{
                holdon=((24+starthour-hour)*60*60+(startminute-minute)*60-(seconds)-receiversec)*1000;           //分到达，跨一天到目标时间的时间
            }
        }else if(hour<7&&hour>=0){
            if(nnt!=0){
                holdon=((starthour-hour-24)*60*60+(startminute-minute)*60-(seconds)-receiversec)*1000;
                Log.d("SleepAlarm","I'm here! Running is not 0");
            }else{
                holdon=((starthour-hour)*60*60+(startminute-minute)*60-(seconds)-receiversec)*1000;
                Log.d("SleepAlarm","I'm here! Running is 0");
            }
        }else{
            holdon=((24+starthour-hour)*60*60+(startminute-minute)*60-(seconds)-receiversec)*1000;           //跨一天到目标时间的时间
        }

        if(hour<starthour&&hour>=7){
            nn=0;
            nnt=0;
        }else if(hour==starthour&&minute<startminute){
            nn=0;
            nnt=0;
        }else if(hour==((starthour+endh+(startminute+endmin)/60)%24)&&minute>=((startminute+endmin)%60)){
            nn=0;
            nnt=0;
        }else if(hour>starthour+endh+(startminute+endmin)/60){
            nn=0;
            nnt=0;
        }


        Log.d("SleepAlarms","现在要等"+(starthour-hour)+"h");
        Log.d("SleepAlarms","现在要等"+(startminute-minute-1)+"m");
        Log.d("SleepAlarms","现在要等"+(60-seconds)+"s");
        Log.d("SleepAlarms","现在要等"+holdon);
        long triggerAtTime=System.currentTimeMillis()+holdon;
        Intent i=new Intent(this,AlarmReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(this,0,i,0);
        Log.d("SleepAlarms","定在"+triggerAtTime);
        manager.set(AlarmManager.RTC,triggerAtTime,pi);


        flag=START_REDELIVER_INTENT;
        //return super.onStartCommand(intent,flag,startId);
        return flag;
    }

    private void prepare(){
        int pi=0;

        Calendar calendart=Calendar.getInstance();
        int month=calendart.get(Calendar.MONTH)+1;
        int day=calendart.get(Calendar.DAY_OF_MONTH);
        int dofw=calendart.get(Calendar.DAY_OF_WEEK);
        int year=calendart.get(Calendar.YEAR);
        String lastdays;

        if(month==3&&day==28){
            notiword[0]=getResources().getString(R.string.truebirthday);
            for(pi=1;pi<53;pi++){
                notiword[pi]=getResources().getString(R.string.tb_1+(pi-1));                  //通过加号可以推演使用下方的字符串！
            }
            for(pi=53;pi<102;pi++){
                notiword[pi]=getResources().getString(R.string.tb_1+(pi-50));
            }
        }else if(month==8&&day==28){
            notiword[0]=getResources().getString(R.string.birthday);
            for(pi=1;pi<7;pi++){
                notiword[pi]=getResources().getString(R.string.b_1+(pi-1));                  //通过加号可以推演使用下方的字符串！
            }
        }else{
            if(month==1&&day==1){
                notiword[0]=getResources().getString(R.string.festival_0);
            }else if(month==2&&day<=5&&day>=3){
                notiword[0]=getResources().getString(R.string.festival_1);
            }else if(month==2&&day==14){
                notiword[0]=getResources().getString(R.string.festival_2);
            }else if(month==3&&day==8){
                notiword[0]=getResources().getString(R.string.festival_3);
                Log.d("SleepAlarm","FLAGGGGGGGG111111111");
            }else if(month==5&&day==1){
                notiword[0]=getResources().getString(R.string.festival_4);
            }else if(month==6&&day==1){
                notiword[0]=getResources().getString(R.string.festival_5);
            }else if(month==6&&day<=22&&day>=21){
                notiword[0]=getResources().getString(R.string.festival_6);
            }else if(month==9&&day>=22&&day<=24){
                notiword[0]=getResources().getString(R.string.festival_7);
            }else if(month==10&&day>=1&&day<=2){
                notiword[0]=getResources().getString(R.string.festival_8);
            }else if(month==11&&day>=7&&day<=8){
                notiword[0]=getResources().getString(R.string.festival_9);
            }else if(month==12&&day==25){
                notiword[0]=getResources().getString(R.string.festival_10);
            }else if(month==12&&day==31){
                notiword[0]=getResources().getString(R.string.festival_11);
                Log.d("SleepAlarm","FLAGGGGGGGG");
            }else if(month==3&&day==12){
                notiword[0]=getResources().getString(R.string.festival_12);
            }else if(dofw==1||dofw==2){                     //国外的一周从星期天开始算 这里是星期天和星期一
                notiword[0]="明天有早课吧？要早点睡么";
            }else if(dofw==7){                     //国外的一周从星期天开始算 这里星期六
                notiword[0]="周末来了！愉快的同时也要注意休息喔";
            }else if(dofw==6){                      //这里是星期五
                notiword[0]="形体很累了，要不早点休息吧？不过大概周末的娱乐诱惑也很大";
            }else{
                notiword[0]="明天没有早课，稍微晚点睡也没关系……也别太晚了";
            }

            for(pi=1;pi<=stringnum;pi++){
                notiword[pi]=getResources().getString(R.string.normal_0+(pi-1));                  //通过加号可以推演使用下方的字符串！
            }
            for(pi=(stringnum+1);pi<(stringnum+3);pi++){
                if(month>=12||month<2){
                    notiword[pi]=getResources().getString(R.string.winter_0+(pi-stringnum-1));
                }else if(month>=7&&month<=9){
                    notiword[pi]=getResources().getString(R.string.summer_0+(pi-stringnum-1));
                }else if(month>2&&month<6){
                    notiword[pi]=getResources().getString(R.string.spring_0+(pi-stringnum-1));
                }
            }

            if(year==2019&&month==3){
                if(day==19||day==20){
                    notiword[0]=getResources().getString(R.string.special_0);
                    notiword[101]=getResources().getString(R.string.normal_52);
                }else{
                    notiword[101]=getResources().getString(R.string.special_0);
                }
            }else if(year==2019&&month==4&&day<=9){
                notiword[101]=getResources().getString(R.string.special_0);
            }else if(year==2019&&month==4&&day>9&&day<=12){
                if(day==10||day==11){
                    notiword[0]=getResources().getString(R.string.special_1);
                    notiword[101]=getResources().getString(R.string.normal_52);
                }else{
                    notiword[101]=getResources().getString(R.string.special_1);
                }
            }else{
                if(year>=2019){
                    notiword[101]=getResources().getString(R.string.special_2);
                }
            }

        }

        Log.d("SleepAlarm","装载完成");
        //Log.d("SleepAlarm",notiword[1]);
    }


    public class AnnounceBinder extends Binder{
        public MyService getService(){
            return MyService.this;
        }
        public void setTime(int newhour,int newmin,int lasthour,int lastmin){
            starthour=newhour;
            startminute=newmin;
            endh=lasthour;
            endmin=lastmin;
            Log.d("SleepAlarm","设置为"+starthour+":"+startminute+"||"+endh+":"+endmin);

            nnt=(endh*60+endmin)*60/10;
        }
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.d("SleepAlarms","Unbind is working");


        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy(){
        Log.d("SleepAlarms","Destroy is working");
        Intent thisintent=new Intent();
        thisintent.setClass(this,MyService.class);
        startService(thisintent);                   //重启service
    }
}
