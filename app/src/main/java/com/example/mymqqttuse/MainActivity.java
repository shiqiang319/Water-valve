package com.example.mymqqttuse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {



    private Timer timerSubscribeTopic = null;
    private TimerTask TimerTaskSubscribeTopic = null;
    private IntentFilter intentFilter;
    private NetworkChange networkChange;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 监听网络状态
         */
        intentFilter=new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChange=new NetworkChange();
        registerReceiver(networkChange,intentFilter);

        final Spinner spinner=findViewById(R.id.spi);
        final TextView faqianyali =findViewById(R.id.tv_fqyl);
        final TextView fahouyali=findViewById(R.id.tv_fhyl);
        final TextView fakoukaidu=findViewById(R.id.tv_fkkd);
        final Button   jia=findViewById(R.id.btn_kdjia);
        final Button   jian=findViewById(R.id.btn_kdjian);
        final SwipeRefreshLayout swipeRefresh=findViewById(R.id.swipe_refresh);

        MyMqttClient.sharedCenter().setConnect();


        MyMqttClient.sharedCenter().setOnServerReadStringCallback(new MyMqttClient.OnServerReadStringCallback() {
            @Override
            public void callback(String Topic, MqttMessage Msg, byte[] MsgByte) {
                Log.e("MqttMsg", "Topic" + Topic +"数据：" + Msg.toString());
                String responseData=Msg.toString();
                parseJSONWithJSONObject(responseData);
            }
           // json数据解析
            private void parseJSONWithJSONObject(String jsonData){
                ArrayList<Integer> shuju = new ArrayList<>();
                try{
                      //第一层解析
                     JSONObject jsonObject=new JSONObject(jsonData);
                     int Cmd =jsonObject.optInt("Cmd");
                    Log.e("Json解析","Cmd:"+Cmd);
                     int Id=jsonObject.optInt("Id");
                    Log.e("Json解析","Id:"+Id);
                     JSONArray Para =jsonObject.optJSONArray("Para");
                    Log.e("Json解析","Para原始："+Para);
                     //第二层解析
                    for (int i=0;i<Para.length();i++){
                           shuju.add(Para.optInt(i));
                        Log.e("Json解析","Para:"+shuju.get(i));
                    }
                    //刷新TextView
                    SpinnerShow(Id);
                    TvShow(faqianyali,shuju.get(0).toString());
                    TvShow(fakoukaidu,shuju.get(1).toString());
                    TvShow(fahouyali,shuju.get(2).toString());
//                    if ((shuju.get(3) & 8)==8){
//                        TvShow(yali,"正常");
//                    }else{
//                        TvShow(yali,"欠压");
//                    }

                    //刷新Button
                    if ((shuju.get(4) & 1)==1){
                        BtnShow(jia,"停止+","#FF0000");


                    }else {
                        BtnShow(jia,"开度+","#00AA44");
                    }

                    if ((shuju.get(4) & 2)==2){
                        BtnShow(jian,"停止-","#FF0000");


                    }else {
                        BtnShow(jian,"开度-","#00AA44");
                    }

                    }catch (Exception e){
                    e.printStackTrace();
                }
            }


            //数据显示在界面
            private void SpinnerShow(final int Msg ) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinner.setSelection(Msg);
                    }
                });
            }
            private void TvShow(final TextView TV, final String Msg ){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TV.setText(Msg);
                        //关闭下拉刷新
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
            //Btn按钮文字刷新
            private void BtnShow(final Button btn, final String BtnMsg, final String color){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn.setText(BtnMsg);
                        btn.setTextColor(Color.parseColor(color));
                    }
                });
            }
        });

        //下拉刷新
        swipeRefresh.setColorSchemeColors(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MyMqttClient.sharedCenter().setSendData(
                        //"/sys/a1S917F388O/wenxin/thing/event/property/post",
                        "/a1yPGkxyv1q/SimuApp/user/update",
                        //"{\"method\":\"thing.event.property.post\",\"id\":\"1111\",\"params\":{\"temperature\":99,\"humidity\":99},\"version\":\"1.0.0\"}",
                        "{\"method\":\"thing.event.property.post\",\"id\":\"1111\",\"params\":{\"Id\":1,\"Cmd\":112,\"Para\":[1]},\"version\":\"1.0.0\"}",
                        //jsonObject.toString(),
                        0,
                        false);
                Log.e("下拉刷新","已发送查询指令");
            }
        });

        //Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String cardNumber = MainActivity.this.getResources().getStringArray(R.array.ctype)[position];
                    if (cardNumber.equals("0")){
                        return;
                    }
                    Toast.makeText(MainActivity.this, "你已选择阀门" + cardNumber, Toast.LENGTH_SHORT).show();
                    String inputx= spinner.getSelectedItem().toString();
                    Log.e("设置阀门：",inputx);
                    Integer x=Integer.parseInt(inputx);
                    MyMqttClient.sharedCenter().setSendData(
                            "/sys/a1S917F388O/wenxin/thing/event/property/post",
                            //"/a1yPGkxyv1q/SimuApp/user/update",
                            //"{\"method\":\"thing.event.property.post\",\"id\":\"1111\",\"params\":{\"temperature\":99,\"humidity\":99},\"version\":\"1.0.0\"}",
                            // "{\"method\":\"thing.event.property.post\",\"id\":\"1111\",\"params\":{\"Id\":1,\"Cmd\":112,\"Para\":[9]},\"version\":\"1.0.0\"}",
                            // jsonObject.toString(),
                            ZhilingJson(x,112,1),
                            0,
                            false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        jia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             String inputx= spinner.getSelectedItem().toString();
             Log.e("获取值",inputx);
             Integer x=Integer.parseInt(inputx);
             MyMqttClient.sharedCenter().setSendData(
                        //"/sys/a1S917F388O/wenxin/thing/event/property/post",
                        "/a1yPGkxyv1q/SimuApp/user/update",
                       // "{\"method\":\"thing.event.property.post\",\"id\":\"1111\",\"params\":{\"Id\":1,\"Cmd\":112,\"Para\":[9]},\"version\":\"1.0.0\"}",
                       // jsonObject.toString(),
                        ZhilingJson(x,67,1),
                        0,
                        false);
                Log.e("Btn","已发送指令"+ ZhilingJson(x,67,1));
            }
        });
        jian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputx= spinner.getSelectedItem().toString();
                Log.e("获取值",inputx);
                Integer x=Integer.parseInt(inputx);
                MyMqttClient.sharedCenter().setSendData(
                        //"/sys/a1S917F388O/wenxin/thing/event/property/post",
                        "/a1yPGkxyv1q/SimuApp/user/update",
                        // "{\"method\":\"thing.event.property.post\",\"id\":\"1111\",\"params\":{\"Id\":1,\"Cmd\":112,\"Para\":[9]},\"version\":\"1.0.0\"}",
                        // jsonObject.toString(),
                        ZhilingJson(x,67,2),
                        0,
                        false);
                Log.e("Btn","已发送指令"+ ZhilingJson(x,67,1));
            }
        });




        /**
         * 订阅主题成功回调
         */
        MyMqttClient.sharedCenter().setOnServerSubscribeCallback(new MyMqttClient.OnServerSubscribeSuccessCallback() {
            @Override
            public void callback(String Topic, int qos) {
               if (Topic.equals("MyMqttClient.sharedCenter().setSubscribe(\"/a1yPGkxyv1q/Fjg1/user/get\",0);//订阅主题1111,消息等级0")){//订阅1111成功
               // if (Topic.equals("/a1yPGkxyv1q/Fjg1/user/get")){//订阅1111成功
                    stopTimerSubscribeTopic();//订阅到主题,停止订阅
                }
            }
        });
        startTimerSubscribeTopic();//定时订阅主题
    }
    //构建指令报文
    private String ZhilingJson(Integer id,int cmd,int para){
        //创建JSON
        JSONObject jsonObject = new JSONObject();
        JSONObject object_1 = new JSONObject();
        JSONArray jsonArray=new JSONArray();
        try {
            jsonArray.put(para);
            object_1.put("Para",jsonArray);
            object_1.put("Id",id);
            object_1.put("Cmd",cmd);
            jsonObject.put("method", "thing.event.property.post");
            jsonObject.put("params",  object_1);
            jsonObject.put("version", "1.0.0");
        } catch (JSONException e){
            e.printStackTrace();
        }

        return jsonObject.toString();
    }


    /**
     * 定时器每隔1S尝试订阅主题
     */
    private void startTimerSubscribeTopic(){
        if (timerSubscribeTopic == null) {
            timerSubscribeTopic = new Timer();
        }
        if (TimerTaskSubscribeTopic == null) {
            TimerTaskSubscribeTopic = new TimerTask() {
                @Override
                public void run() {
                   // MyMqttClient.sharedCenter().setSubscribe("/a1yPGkxyv1q/Fjg1/user/get",0);//订阅主题1111,消息等级0
                    MyMqttClient.sharedCenter().setSubscribe("MyMqttClient.sharedCenter().setSubscribe(\"/a1yPGkxyv1q/Fjg1/user/get\",0);//订阅主题1111,消息等级0",0);//订阅主题1111,消息等级0
                }
            };
        }
        if(timerSubscribeTopic != null && TimerTaskSubscribeTopic != null )
            timerSubscribeTopic.schedule(TimerTaskSubscribeTopic, 0, 1000);
    }

    private void stopTimerSubscribeTopic(){
        if (timerSubscribeTopic != null) {
            timerSubscribeTopic.cancel();
            timerSubscribeTopic = null;
        }
        if (TimerTaskSubscribeTopic != null) {
            TimerTaskSubscribeTopic.cancel();
            TimerTaskSubscribeTopic = null;
        }
    }

    //当活动不再可见时调用
    @Override
    protected void onStop()
    {
        super.onStop();
        stopTimerSubscribeTopic();//停止定时器订阅
    }

    /**
     * 当处于停止状态的活动需要再次展现给用户的时候，触发该方法
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        startTimerSubscribeTopic();//定时订阅主题
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimerSubscribeTopic();

    }
    /**
     * 动态注册接受者，记得取消注册
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChange);
    }


}
