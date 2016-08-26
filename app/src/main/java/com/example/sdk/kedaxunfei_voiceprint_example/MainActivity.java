package com.example.sdk.kedaxunfei_voiceprint_example;

import android.app.Activity;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeakerVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VerifierListener;
import com.iflytek.cloud.VerifierResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //buttons
    Button trainButton;
    Button verifyButton;
    Button queButton;
    Button delButton;
    Button getPasswordButton;
    //
    TextView textView;
    EditText editText;
    //voiceprint
    SpeechListener passwordListener;
    SpeakerVerifier speakerVerifier;
    VerifierListener verifierListener;
    VerifierListener registerListener;
    SpeechListener queListener;
    SpeechListener delListener;
    //存储在云端的注册的声纹模型
    String vid;
    JSONArray passwordJsonArr;
    String pwdText = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //initial the speech utility
        SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID+"=5785c87a");
        super.onCreate(savedInstanceState);
        requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        trainButton = (Button)findViewById(R.id.trainButton);
        verifyButton = (Button)findViewById(R.id.verifyButton);
        queButton = (Button)findViewById(R.id.queButton);
        delButton = (Button)findViewById(R.id.delButton);
        getPasswordButton = (Button)findViewById(R.id.getPasswordButton);
        textView = (TextView)findViewById(R.id.textView);
        editText = (EditText)findViewById(R.id.editText);
        trainButton.setOnClickListener(this);
        verifyButton.setOnClickListener(this);
        queButton.setOnClickListener(this);
        delButton.setOnClickListener(this);
        getPasswordButton.setOnClickListener(this);
        //voiceprint
        speakerVerifier = SpeakerVerifier.createVerifier(this,new InitListener() {
            @Override
            public void onInit(int i) {
                if (ErrorCode.SUCCESS==i){
                    Log.v("----->","初始化成功");
                }else {
                    Log.v("----->","初始化失败");
                }
            }
        });
        speakerVerifier.setParameter(SpeechConstant.ISV_PWDT,""+3 );
        //空表示匿名用户
        //speakerVerifier.setParameter(SpeechConstant.AUTH_ID, "");
        passwordListener = new SpeechListener() {
            @Override
            public void onEvent(int i, Bundle bundle) {
            }
            @Override
            public void onBufferReceived(byte[] bytes) {
                Log.v("----->","onBufferReceived password");
                String res = new String(bytes);
                Log.v("----->",""+res);
                try{
                    JSONObject object = new JSONObject(res);
                    passwordJsonArr = object.getJSONArray("num_pwd");
                    pwdText = passwordJsonArr.toString();
                    textView.setText(pwdText);
                }catch (JSONException e){
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "كۇنۇپكىنى بېسىپ تۇرۇپ ساننى بىردىن بىردىن ئالدىرىماي ئۇقۇڭ ، ئاۋال ئاۋازىڭىزنى تۇنىۋالاي ", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCompleted(SpeechError speechError) {

            }
        };
        speakerVerifier.getPasswordList(passwordListener);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.trainButton:
                // train the speakerVerifier
                //the user id which is matching to the voiceprint , a identifier of 6-18 digit
                //
                speakerVerifier.cancel();
                speakerVerifier.setParameter(SpeechConstant.PARAMS, null);
                speakerVerifier.setParameter(SpeechConstant.AUTH_ID, editText.getText().toString());
                speakerVerifier.setParameter(SpeechConstant.ISV_PWDT,""+3 );
                speakerVerifier.setParameter(SpeechConstant.ISV_SST, "train");
                StringBuffer sb = new StringBuffer();
                try {
                    for(int i = 0; i < passwordJsonArr.length(); i++){
                        if (i!=0){
                            sb.append("-");
                        }
                        sb.append(passwordJsonArr.getString(1));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                speakerVerifier.setParameter(SpeechConstant.ISV_PWD, sb.toString()   );
                //音频文件位置
                speakerVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH, Environment.getExternalStorageDirectory().getAbsolutePath()+"/kedaxunfei-voiceprint-example.pcm");
                //训练次数
                speakerVerifier.setParameter(SpeechConstant.ISV_RGN, "2");
                //自由说必须设置，其他不设置
//                speakerVerifier.setParameter(SpeechConstant.SAMPLE_RATE, "8000");


                //
                registerListener = new VerifierListener() {
                    @Override
                    public void onVolumeChanged(int i, byte[] bytes) {}

                    @Override
                    public void onBeginOfSpeech() {
//                        Toast.makeText(MainActivity.this, "onBeginOfSpeech train", Toast.LENGTH_SHORT).show();
                        Log.v("----->","onBeginOfSpeech train");
                    }

                    @Override
                    public void onEndOfSpeech() {
//                        Toast.makeText(MainActivity.this, "onEndOfSpeech train", Toast.LENGTH_SHORT).show();
                        Log.v("----->","onEndOfSpeech train");
                    }

                    @Override
                    public void onResult(VerifierResult verifierResult) {
                        Log.v("----->","--------------------------------------------");
                        vid = verifierResult.vid;
                        //需要所两次
                        if (verifierResult.suc == 1){
                            Toast.makeText(MainActivity.this,"注册成功，在说一遍", Toast.LENGTH_SHORT).show();
                        }else if (verifierResult.suc == 2){
                            Toast.makeText(MainActivity.this,"注册完毕，vid = "+vid, Toast.LENGTH_SHORT).show();
                        }
//                        Toast.makeText(MainActivity.this, "onResult train", Toast.LENGTH_SHORT).show();
                        Log.v("----->","onResult train");
                    }

                    @Override
                    public void onError(SpeechError speechError) {
//                        Toast.makeText(MainActivity.this, "onError train", Toast.LENGTH_SHORT).show();
                        Log.v("----->","onError train");
                        Log.v("----->", ""+speechError.getErrorDescription());
                        if (speechError.getErrorCode() == ErrorCode.MSP_ERROR_ALREADY_EXIST) {
                            Log.v("------>","模型已存在，如需重新注册，请先删除");
                            Toast.makeText(MainActivity.this, "模型已存在，如需重新注册，请先删除", Toast.LENGTH_SHORT).show();
                            textView.setText("模型已存在，如需重新注册，请先删除");
                        } else {
                            Log.v("------>","onError Code：" + speechError.getPlainDescription(true));
                        }
                    }

                    @Override
                    public void onEvent(int i, int i1, int i2, Bundle bundle) {
                        Log.v("------>","onEvent ：" + i);
                    }
                };
                // start to register the voiceprint
                Toast.makeText(MainActivity.this,"开始注册，请说一遍", Toast.LENGTH_SHORT).show();
                speakerVerifier.startListening(registerListener);
                break;
            case R.id.verifyButton:
                // verify
                speakerVerifier.cancel();
                speakerVerifier.setParameter(SpeechConstant.PARAMS, null);
                //the user id which is matching to the voiceprint , a identifier of 6-18 digit
                speakerVerifier.setParameter(SpeechConstant.AUTH_ID, editText.getText().toString());
                // train the speakerVerifier
                speakerVerifier.setParameter(SpeechConstant.ISV_SST, "verify");
                speakerVerifier.setParameter(SpeechConstant.ISV_PWDT, ""+3);
                speakerVerifier.setParameter(SpeechConstant.ISV_PWD, pwdText);
                //自由说要设置，其他不要
//                speakerVerifier.setParameter(SpeechConstant.SAMPLE_RATE, "8000");
                //
                verifierListener = new VerifierListener() {
                    @Override
                    public void onVolumeChanged(int i, byte[] bytes) {}

                    @Override
                    public void onBeginOfSpeech() {

                    }

                    @Override
                    public void onEndOfSpeech() {

                    }

                    @Override
                    public void onResult(VerifierResult verifierResult) {
                        Toast.makeText(MainActivity.this,
                                "onResult verify : vid = " +verifierResult.vid+
                                ", score = "+verifierResult.score,
                                Toast.LENGTH_LONG).show();
                        Log.v("----->","onResult verify");
                    }

                    @Override
                    public void onError(SpeechError speechError) {
                        Log.v("----->","onError verify");
                        Log.v("----->","speechError.getErrorDescription()");
                        textView.setText(speechError.getErrorDescription());

                    }

                    @Override
                    public void onEvent(int i, int i1, int i2, Bundle bundle) {

                    }
                };
                // start to register the voiceprint
                speakerVerifier.startListening(verifierListener);
                break;
            case R.id.queButton:
                //
                speakerVerifier.cancel();
                speakerVerifier.setParameter(SpeechConstant.PARAMS, null);
                //the user id which is matching to the voiceprint , a identifier of 6-18 digit
                speakerVerifier.setParameter(SpeechConstant.AUTH_ID, editText.getText().toString());
                speakerVerifier.setParameter(SpeechConstant.ISV_PWDT, ""+3);
                speakerVerifier.setParameter(SpeechConstant.ISV_PWD,pwdText);
//                speakerVerifier.setParameter(SpeechConstant.SAMPLE_RATE, "8000");
                speakerVerifier.setParameter(SpeechConstant.ISV_VID, vid);
                queListener = new SpeechListener() {
                    @Override
                    public void onEvent(int i, Bundle bundle) {
//                        Log.v("----->","-------onevent quee");
                    }

                    @Override
                    public void onBufferReceived(byte[] bytes) {
                        try {
//                            Toast.makeText(MainActivity.this, "onBufferReceived que", Toast.LENGTH_SHORT).show();
                            Log.v("----->","onBufferReceived que ");
                            textView.setText("que:模型数据存在");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCompleted(SpeechError speechError) {
                        try {
                            Log.v("----->",speechError.getErrorDescription());
                            textView.setText("que:"+speechError.getErrorDescription());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                };
                speakerVerifier.sendRequest("que", editText.getText().toString(),queListener );
                break;
            case R.id.delButton:
                //
                speakerVerifier.cancel();
                speakerVerifier.setParameter(SpeechConstant.PARAMS, null);
                //the user id which is matching to the voiceprint , a identifier of 6-18 digit
                speakerVerifier.setParameter(SpeechConstant.AUTH_ID, editText.getText().toString());
                speakerVerifier.setParameter(SpeechConstant.ISV_PWDT, ""+3);
                speakerVerifier.setParameter(SpeechConstant.ISV_PWD, pwdText);
//                speakerVerifier.setParameter(SpeechConstant.SAMPLE_RATE, "8000");
                speakerVerifier.setParameter(SpeechConstant.ISV_VID, vid);
                delListener = new SpeechListener() {
                    @Override
                    public void onEvent(int i, Bundle bundle) {

                    }

                    @Override
                    public void onBufferReceived(byte[] bytes) {
                        try {
//                            Toast.makeText(MainActivity.this, "onBufferReceived del", Toast.LENGTH_SHORT).show();
                            Log.v("----->","onBufferReceived del");
                            textView.setText("del:模型数据已删除");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCompleted(SpeechError speechError) {
                        try {
                            Log.v("----->",speechError.getErrorDescription());
                            textView.setText("del:"+speechError.getErrorDescription());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                };
                speakerVerifier.sendRequest("del", editText.getText().toString(),delListener );
                break;
            case R.id.getPasswordButton:
                speakerVerifier.setParameter(SpeechConstant.ISV_PWDT,""+3 );
                passwordListener = new SpeechListener() {
                    @Override
                    public void onEvent(int i, Bundle bundle) {
                    }
                    @Override
                    public void onBufferReceived(byte[] bytes) {
                        Log.v("----->","onBufferReceived password");
                        String res = new String(bytes);
                        try{
                            JSONObject object = new JSONObject(res);
                            pwdText = object.getJSONArray("txt_pwd").getString(0);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        Log.v("----->",""+pwdText);
                        textView.setText(pwdText);
                    }

                    @Override
                    public void onCompleted(SpeechError speechError) {

                    }
                };
                speakerVerifier.getPasswordList(passwordListener);
                break;
            default:
                break;
        }
    }
}
