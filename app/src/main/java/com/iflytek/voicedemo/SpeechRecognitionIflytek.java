package com.iflytek.voicedemo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.speech.util.JsonParser;

import model.dictionary.application.GlobalDictionary;
import model.dictionary.application.LookUpInterface;
import model.dictionary.exception.DictionaryException;

import static android.content.ContentValues.TAG;

public class SpeechRecognitionIflytek extends Application implements SpeechRecognitionInterface {
    private static final String LOG_TAG = SpeechRecognitionIflytek.class.getSimpleName();
    
    private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
    private static final String GRAMMAR_TYPE_ABNF = "abnf";
    private static final String GRAMMAR_TYPE_BNF = "bnf";
    private static final String mCloud = "cloud";
    private static Context context;
    private final String menginetype = SpeechConstant.TYPE_CLOUD;
    
    private SpeechRecognizer mrecognizer;
    private SharedPreferences msharedpreferences;
    private String mcloudgrammar = null;
    private Activity mcalleractivity;
    private String mgrammarpath;
    private String mparserresult;
    private LookUpInterface mlookuphandle;
    
    public SpeechRecognitionIflytek(Activity callerActivity, String grammarPath) {
        mcalleractivity = callerActivity;
        mgrammarpath = grammarPath;
        mparserresult = "";
        mlookuphandle = GlobalDictionary.createDictionary();
        this.initSpeechRecognizer();
        this.SetParam();
    }
    
    private void initSpeechRecognizer() {
        // Log.e(TAG, mcalleractivity + " " + minitlistener );
        // SpeechRecognitionIflytek.context = this.getApplicationContext();
        mrecognizer = SpeechRecognizer.createRecognizer(mcalleractivity, minitlistener);
        // if(mrecognizer == null){
        // Log.e("mrecognizer","NULL!!");
        // }
        mcloudgrammar = com.iflytek.speech.util.FucUtil.readFile(mcalleractivity, mgrammarpath, "utf-8");
        // Log.e(TAG, "initSpeechRecognizer: "+ this.getPackageName());
        msharedpreferences = mcalleractivity.getSharedPreferences(mcalleractivity.getPackageName(), MODE_PRIVATE);
    }
    
    private InitListener minitlistener = new InitListener() {
        
        @Override
        public void onInit(int code) {
            String info;
            if (code != ErrorCode.SUCCESS) {
                info = "init fail, error code: " + code;
                // TODO
            } else {
                info = "init success";
            }
            Log.d(LOG_TAG, info);
        }
    };
    
    // private LexiconListener mLexiconListener = new LexiconListener() {
    // @Override
    // public void onLexiconUpdated(String lexiconId, SpeechError speechError) {
    // String info;
    // if (speechError != null) {
    // info = "lexicon update fail, error code: " + speechError.getErrorCode();
    // // TODO
    // } else {
    // info = "lexicon update success";
    // }
    // Log.d(LOG_TAG, info);
    // }
    // };
    
    private GrammarListener mcloudgrammarlistener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError speechError) {
            String info;
            if (speechError != null) {
                info = "grammar build fail, error code: " + speechError.getErrorCode();
            } else {
                info = "grammar build success: (grammar ID)" + grammarId;
            }
            Log.d(LOG_TAG, info);
        }
    };
    
    private RecognizerListener mrecognizerlistener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            String info = "Talking...(volume: " + volume + ", data length: " + data.length + ")";
            Log.d(LOG_TAG, info);
        }
        
        @Override
        public void onBeginOfSpeech() {
            
        }
        
        @Override
        public void onEndOfSpeech() {
            
        }
        
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean islast) {
            if (null != recognizerResult) {
                String originResult = recognizerResult.getResultString();
                Log.d(LOG_TAG, "origin recognizer result: " + originResult);
                String parserResult;
                if (mCloud.equalsIgnoreCase(menginetype)) {
                    parserResult = JsonParser.parseGrammarResult(originResult);
                } else {
                    parserResult = JsonParser.parseLocalGrammarResult(originResult);
                }
                Log.d(LOG_TAG, "parser result: " + parserResult);
                mparserresult += parserResult;
            } else {
                Log.d(LOG_TAG, "recognizer result is null");
            }
        }
        
        @Override
        public void onError(SpeechError speechError) {
            if (speechError != null) {
                String info = "recognize fail, error code: " + speechError.getErrorCode();
                Log.d(LOG_TAG, info);
            }
        }
        
        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {
            
        }
    };
    
    private boolean buildGrammar() {
        int ret;
        ret = mrecognizer.buildGrammar(GRAMMAR_TYPE_ABNF, mcloudgrammar, mcloudgrammarlistener);
        String info;
        if (ret != ErrorCode.SUCCESS) {
            info = "grammar build procedure fail, error code: " + ret;
        } else {
            info = "grammar build procedure success";
        }
        Log.d(LOG_TAG, info);
        return ret == ErrorCode.SUCCESS;
    }
    
    private boolean SetParam() {
        
        // mrecognizer.setParameter("engine_type", "cloud");
        mrecognizer.setParameter(SpeechConstant.ENGINE_TYPE, menginetype);
        mrecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mrecognizer.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        boolean ret;
        ret = this.buildGrammar();
        if (!ret) {
            return ret;
        }
        
        if (mCloud.equalsIgnoreCase(menginetype)) {
            String grammarId = msharedpreferences.getString(KEY_GRAMMAR_ABNF_ID, null);
            if (TextUtils.isEmpty(grammarId)) {
                ret = false;
            } else {
                mrecognizer.setParameter(SpeechConstant.CLOUD_GRAMMAR, grammarId);
                ret = true;
            }
        }
        
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mrecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        boolean tmp = mrecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                Environment.getExternalStorageDirectory() + "/msc/asr.wav");
        
        // final String audio_path = "/msc/asr.wav";
        // mrecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        // boolean tmp = mrecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageState() +
        // audio_path);
        if (tmp == true) {
            Log.e(TAG, "SetParam: wav true");
        } else {
            Log.e(TAG, "SetParam: wav flase");
        }
        return ret;
    }
    
    public int startRecognize() {
        int ret;
        ret = mrecognizer.startListening(mrecognizerlistener);
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
        mrecognizer.stopListening();
    }
    
    public void cancelRecognize() {
        mrecognizer.cancel();
    }
    
    public String getAction() {
        this.stopRecognize();
        // if("" == mparserresult){
        // Log.e(TAG, "getAction: \"\" mParseResult" );
        // }
        // Toast.makeText(this,mparserresult,Toast.LENGTH_LONG);
        // Log.d(LOG_TAG, "total parser result" + mparserresult);
        
        // mparserresult="a";
        StringBuffer ret = new StringBuffer("");
        try {
            mlookuphandle.exactLookUpWord(mparserresult, ret);
            
        } catch (DictionaryException e) {
            e.printStackTrace();
        }
        mparserresult = "";
        if (ret.length() == 0) {
            // 因为输入零字节的字符到Session.write中会报错,所以改为写入换行符
            ret.append('\n');
        }
        Log.d(LOG_TAG, "action result:" + ret + ";");
        return ret.toString();
    }
    
    public void destroy() {
        if (null != mrecognizer) {
            mrecognizer.cancel();
            mrecognizer.destroy();
        }
    }
}
