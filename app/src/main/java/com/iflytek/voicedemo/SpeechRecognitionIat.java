package com.iflytek.voicedemo;


import android.app.Activity;
import android.app.Application;
import android.app.job.JobInfo;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.speech.util.FucUtil;
import com.iflytek.speech.util.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;
import model.dictionary.application.GlobalDictionary;
import model.dictionary.application.LookUpInterface;
import model.dictionary.exception.DictionaryException;

import static android.content.Context.MODE_PRIVATE;


public class SpeechRecognitionIat extends Activity implements SpeechRecognitionInterface{
    private static final String LOG_TAG = SpeechRecognitionIflytek.class.getSimpleName();

    private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
    private static final String GRAMMAR_TYPE_ABNF = "abnf";
    private static final String GRAMMAR_TYPE_BNF = "bnf";
    private static final String mCloud = "cloud";
    private static final String mResultType = "json";
    private static final String MUTE_BEGIN_TIME = "4000";
    private static final String MUTE_STOP_TIME = "1000";
    private static final String ENABLE_PUNCTUATE = "0";
    private final String mEngineType = SpeechConstant.TYPE_CLOUD;

    private String mLanguage = "zh_cn";
    private String mAccent = "mandarin";
    private SpeechRecognizer mRecognizer;
    private SharedPreferences mSharedPreferences;
    private Activity mCallerActivity;
    private StringBuffer mParserResult;
    private LookUpInterface mLookUpHandle;
    private boolean mEnableTranslate;
    public boolean mRecognitionDone=false;

    Handler han = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };



    public SpeechRecognitionIat(Activity callerActivity, String userwordPath){
        mCallerActivity = callerActivity;
        mParserResult = new StringBuffer();
        mParserResult.setLength(0);
        mLookUpHandle = GlobalDictionary.createDictionary();
        mEnableTranslate = false;
        this.initSpeechRecognizer(userwordPath);
        this.SetParam();
    }



    private void initSpeechRecognizer(String userwordPath) {
        //Log.e(TAG, mCallerActivity + " " + mInitListener );
        //SpeechRecognitionIflytek.context = this.getApplicationContext();
        mRecognizer = SpeechRecognizer.createRecognizer(mCallerActivity, mInitListener);
        //if(mRecognizer == null){
        //    Log.e("mRecognizer","NULL!!");
        //}
//        Log.e(TAG, "initSpeechRecognizer: "+ this.getPackageName());
        String userwordContent = FucUtil.readFile(mCallerActivity, userwordPath, "utf-8");
        int ret = mRecognizer.updateLexicon("userword", userwordContent, mLexiconListener);
        Log.d(LOG_TAG, "update lexicon fail, error code: " + ret);
        mSharedPreferences = mCallerActivity.getSharedPreferences(mCallerActivity.getPackageName(),	MODE_PRIVATE);
    }


    private InitListener mInitListener = new InitListener(){

        @Override
        public void onInit(int code) {
            String info;
            if (code != ErrorCode.SUCCESS) {
                info = "init fail, error code: "+code;
                //TODO
            } else {
                info = "init success";
            }
            Log.d(LOG_TAG, info);
        }
    };

    private LexiconListener mLexiconListener = new LexiconListener() {
        @Override
        public void onLexiconUpdated(String lexiconId, SpeechError speechError) {
            String info;
            if (speechError != null) {
                info = "lexicon update fail, error code: "+speechError.getErrorCode();
                //TODO
            } else {
                info = "lexicon update success";
            }
            Log.d(LOG_TAG, info);
        }
    };


    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            String info = "Talking...(volume: " + volume + ", data length: " + data.length + ")";
            Log.d(LOG_TAG, info);
        }

        @Override
        public void onBeginOfSpeech() {
            mRecognitionDone=false;
            Toast.makeText(mCallerActivity,"Begin of Speech",Toast.LENGTH_SHORT);
        }

        @Override
        public void onEndOfSpeech() {
            Toast.makeText(mCallerActivity,"OnEndOfSpeech",Toast.LENGTH_SHORT);
            Log.d(LOG_TAG, "end of speech : "+System.currentTimeMillis());
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean islast) {
            Log.d(TAG, "OnResult: begin : "+System.currentTimeMillis());
            if (null != recognizerResult) {
                String origin_result = recognizerResult.getResultString();
                Log.d(LOG_TAG, "origin recognizer result: " + origin_result);
                String parser_result = null;
                if (mResultType.equals("json")) {
                    if (mEnableTranslate) {
                        parser_result = parseTranslateResult(recognizerResult);
                    } else {
                        parser_result = parseNormalResult(recognizerResult);
                    }
                } else if (mResultType.equals("plain")) {
                    parser_result = recognizerResult.getResultString();
                }
                Log.d(LOG_TAG, "parser result: "+ parser_result);
                mParserResult.append(parser_result);

                Log.d(TAG, "Onresult: end : "+System.currentTimeMillis());
                if (islast) {
                        Log.d(LOG_TAG, "last result" + mParserResult+"-------------------------");
                    mRecognitionDone=true;
                        //Log.d(LOG_TAG, "action"+getAction());

                }
            } else {
                Log.d(LOG_TAG, "recognizer result is null");
            }

        }

        @Override
        public void onError(SpeechError speechError) {
            if (speechError != null) {
                String info = "recognize fail, error code: "+speechError.getErrorCode();
                Log.d(LOG_TAG, info);
            }
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    private String parseNormalResult(RecognizerResult result) {
        String text = JsonParser.parseIatResult(result.getResultString());
        String sn_part = null;
        try {
            JSONObject result_json = new JSONObject(result.getResultString());
            sn_part = result_json.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return text;
    }

    private String parseTranslateResult(RecognizerResult result) {
        String dst = JsonParser.parseTransResult(result.getResultString(), "dst");
        String src = JsonParser.parseTransResult(result.getResultString(),"src");
        if (TextUtils.isEmpty(dst) || TextUtils.isEmpty(src)) {
            Log.d(LOG_TAG, "translate fail");
        }
        return dst;
    }


    private boolean SetParam() {
        boolean ret=false;
        //mRecognizer.setParameter("engine_type", "cloud");
        mRecognizer.setParameter(SpeechConstant.PARAMS, null);
        mRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        mRecognizer.setParameter(SpeechConstant.RESULT_TYPE, mResultType);
        mRecognizer.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");

        mRecognizer.setParameter(SpeechConstant.LANGUAGE, mLanguage);
        mRecognizer.setParameter(SpeechConstant.ACCENT, mAccent);
        if (mEnableTranslate) {
            Log.d(LOG_TAG, "translate enable");
            // TODO
            mRecognizer.setParameter( SpeechConstant.ASR_SCH, "1" );
            mRecognizer.setParameter( SpeechConstant.ADD_CAP, "translate" );
            mRecognizer.setParameter( SpeechConstant.TRS_SRC, "its" );
            if (mLanguage.equalsIgnoreCase("en_us")) {
                mRecognizer.setParameter(SpeechConstant.ORI_LANG, "en");
                mRecognizer.setParameter(SpeechConstant.TRANS_LANG, "cn");
            } else if (mLanguage.equalsIgnoreCase("zh_cn")) {
                mRecognizer.setParameter(SpeechConstant.ORI_LANG, "cn");
                mRecognizer.setParameter(SpeechConstant.TRANS_LANG, "en");
            } else {
                Log.d(LOG_TAG, "unknown language type:" + mLanguage);
            }
        }
        mRecognizer.setParameter(SpeechConstant.VAD_BOS, MUTE_BEGIN_TIME);
        mRecognizer.setParameter(SpeechConstant.VAD_EOS, MUTE_STOP_TIME);
        mRecognizer.setParameter(SpeechConstant.ASR_PTT, ENABLE_PUNCTUATE);
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/asr.wav");


        return ret;
    }

    public int startRecognize() {
        int ret;
        ret = mRecognizer.startListening(mRecognizerListener);
        String info;
        if (ret != ErrorCode.SUCCESS) {
            info = "recognize interface fail, error code: " + ret;
        } else {
            info = "recognize interface success";
        }
        Log.d(LOG_TAG, info);
        return ret;
    }

    public void stopRecognize() {
        Log.d(LOG_TAG, "stop time : "+System.currentTimeMillis());
        mRecognizer.stopListening();
    }

    public void cancelRecognize() {
        mRecognizer.cancel();
    }

    public String getAction() {
        Log.d(LOG_TAG, "get action in");

        if(mParserResult.length() == 0){//因为输入零字节的字符到Session.write中会报错,所以改为写入换行符
            Toast.makeText(mCallerActivity,"what?",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(mCallerActivity,"[" + mParserResult.toString()+"]",Toast.LENGTH_SHORT).show();
        }
        StringBuffer ret = new StringBuffer();
        try {
            Log.d(LOG_TAG, "nyz ook up word"+mParserResult.toString());
            mLookUpHandle.exactLookUpWord(mParserResult.toString().toLowerCase(), ret);
        } catch (DictionaryException e){
            e.printStackTrace();
            Log.e(LOG_TAG, "nyz not found"+mParserResult.toString());
        }
        mParserResult.setLength(0);

        Log.d(LOG_TAG, "action result:" + ret+";");
        Log.d(TAG, "getAction: end : "+System.currentTimeMillis());
        return ret.toString();
    }

    public void destroy() {
        if (null != mRecognizer) {
            mRecognizer.cancel();
            mRecognizer.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if( null != mRecognizer ){
            // 退出时释放连接
            mRecognizer.cancel();
            mRecognizer.destroy();
        }
    }

    @Override
    protected void onResume() {
        // 开放统计 移动数据统计分析
        //FlowerCollector.onResume(IatDemo.this);
        //FlowerCollector.onPageStart(TAG);
        super.onResume();
    }

    @Override
    protected void onPause() {
        // 开放统计 移动数据统计分析
        //FlowerCollector.onPageEnd(TAG);
        //FlowerCollector.onPause(IatDemo.this);
        super.onPause();
    }
}
