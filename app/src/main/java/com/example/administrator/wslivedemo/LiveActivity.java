package com.example.administrator.wslivedemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import me.lake.librestreaming.core.listener.RESConnectionListener;
import me.lake.librestreaming.filter.hardvideofilter.BaseHardVideoFilter;
import me.lake.librestreaming.filter.hardvideofilter.HardVideoGroupFilter;
import me.lake.librestreaming.ws.StreamAVOption;
import me.lake.librestreaming.ws.StreamLiveCameraView;
import me.lake.librestreaming.ws.filter.hardfilter.GPUImageBeautyFilter;
import me.lake.librestreaming.ws.filter.hardfilter.WatermarkFilter;
import me.lake.librestreaming.ws.filter.hardfilter.extra.GPUImageCompatibleFilter;

//https://github.com/WangShuo1143368701/WSLiveDemo/blob/master/README.md
public class LiveActivity extends AppCompatActivity {
    private static final String               TAG = LiveActivity.class.getSimpleName();
    private              StreamLiveCameraView mLiveCameraView;
    private              TextView             tv_time;
    private              TextView             tv_left_time;
    private              StreamAVOption       streamAVOption;
    private              String               rtmpUrl = "rtmp://twangjiepi.dynu.net:9906/live/"; //推流地址
    private              LiveUI               mLiveUI;
    private int totalTime = 3*60*60; //单位:秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        rtmpUrl = rtmpUrl + getIntent().getStringExtra("url");
        Log.i("test111","获取到的推流地址:"+rtmpUrl);
        initLiveConfig();
        mLiveUI = new LiveUI(this,mLiveCameraView,rtmpUrl);
    }

    /**
     * 设置推流参数
     */
    public void initLiveConfig() {
        mLiveCameraView = (StreamLiveCameraView) findViewById(R.id.stream_previewView);
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_left_time = (TextView) findViewById(R.id.tv_left_time);

        //参数配置 start
        streamAVOption = new StreamAVOption();
        streamAVOption.streamUrl = rtmpUrl;
        streamAVOption.videoWidth = 1280;
        streamAVOption.videoHeight = 720;
        streamAVOption.previewWidth = 1280;
        streamAVOption.previewHeight = 720;


        mLiveCameraView.init(this, streamAVOption);
        mLiveCameraView.addStreamStateListener(resConnectionListener);

        handler.sendEmptyMessage(0);
    }

    private void setShuiYin()
    {
        LinkedList<BaseHardVideoFilter> files = new LinkedList<>();
        files.add(new GPUImageCompatibleFilter(new GPUImageBeautyFilter()));
//        files.add(new WatermarkFilter(BitmapFactory.decodeResource(getResources(),R.mipmap.live),new Rect(100,100,200,200)));
        tv_time.setText(getDateTimeFromMillisecond(System.currentTimeMillis()));
        tv_time.setDrawingCacheEnabled(true);
        tv_time.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        tv_time.layout(0, 0, tv_time.getMeasuredWidth(), tv_time.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(tv_time.getDrawingCache());
        files.add(new WatermarkFilter(bitmap,new Rect(100,100,380,180)));
        mLiveCameraView.setHardVideoFilter(new HardVideoGroupFilter(files));
        tv_time.destroyDrawingCache();//千万别忘最后一步
    }

    //数据的接收
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            setLeftTime();
            setShuiYin();
            sendEmptyMessageDelayed(0,1000);
        }
    };

    private void setLeftTime()
    {
        totalTime = totalTime-1;
        int leftHour = totalTime / 3600; //剩余小时数
        int leftMin = (totalTime - leftHour * 3600)/60; //剩余分钟数
        if (leftMin > 10)
        {
            tv_left_time.setText("0"+leftHour+" 时 "+leftMin+" 分");
        }else {
            tv_left_time.setText("0"+leftHour+" 时 "+"0"+leftMin+" 分");
        }
    }

    public String getDateTimeFromMillisecond(Long millisecond){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date             date             = new Date(millisecond);
        String           dateStr          = simpleDateFormat.format(date);
        return dateStr;
    }


    RESConnectionListener resConnectionListener = new RESConnectionListener() {
        @Override
        public void onOpenConnectionResult(int result) {
            Log.i("test111","打开推流连接 状态："+result+ " 推流地址："+rtmpUrl);
            if (result == 0)
            {
                Toast.makeText(LiveActivity.this,"链接成功,开始推流",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(LiveActivity.this,"链接失败",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onWriteError(int errno) {
            Log.i("test111","推流出错,请尝试重连");
            Toast.makeText(LiveActivity.this,"推流出错",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCloseConnectionResult(int result) {
            Log.i("test111","关闭推流连接 状态："+result);
            if (result == 0)
            {
                Toast.makeText(LiveActivity.this,"关闭推流成功",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(LiveActivity.this,"关闭推流失败",Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLiveCameraView.destroy();
    }
}
