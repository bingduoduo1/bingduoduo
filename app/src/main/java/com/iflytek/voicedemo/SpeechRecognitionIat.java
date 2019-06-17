package com.iflytek.voicedemo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
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

import model.dictionary.application.GlobalDictionary;
import model.dictionary.application.LookUpInterface;
import model.dictionary.exception.DictionaryException;

import static android.content.ContentValues.TAG;

public class SpeechRecognitionIat extends Activity implements SpeechRecognitionInterface {
    private static final String LOG_TAG = SpeechRecognitionIflytek.class.getSimpleName();
    
    private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
    private static final String GRAMMAR_TYPE_ABNF = "abnf";
    private static final String GRAMMAR_TYPE_BNF = "bnf";
    private static final String mCloud = "cloud";
    private static final String mResultType = "json";
    private static final String MUTE_BEGIN_TIME = "4000";
    private static final String MUTE_STOP_TIME = "1000";
    private static final String ENABLE_PUNCTUATE = "0";
    private final String menginetype = SpeechConstant.TYPE_CLOUD;
    
    private String mlanguage = "zh_cn";
    private String maccent = "mandarin";
    private SpeechRecognizer mrecognizer;
    private SharedPreferences msharedpreferences;
    private Activity mcalleractivity;
    private StringBuffer mparserresult;
    private LookUpInterface mlookuphandle;
    private boolean menabletranslate;
    public boolean mrecognitiondone = false;
    
    Handler han = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    
    public SpeechRecognitionIat(Activity callerActivity, String userwordPath) {
        mcalleractivity = callerActivity;
        mparserresult = new StringBuffer();
        mparserresult.setLength(0);
        mlookuphandle = GlobalDictionary.createDictionary();
        menabletranslate = false;
        this.initSpeechRecognizer(userwordPath);
        this.SetParam();
    }
    
    private void initSpeechRecognizer(String userwordPath) {
        // Log.e(TAG, mcalleractivity + " " + minitlistener );
        // SpeechRecognitionIflytek.context = this.getApplicationContext();
        mrecognizer = SpeechRecognizer.createRecognizer(mcalleractivity, minitlistener);
        // if(mrecognizer == null){
        // Log.e("mrecognizer","NULL!!");
        // }
        // Log.e(TAG, "initSpeechRecognizer: "+ this.getPackageName());
        String userwordContent = FucUtil.readFile(mcalleractivity, userwordPath, "utf-8");
        int ret = mrecognizer.updateLexicon("userword", userwordContent, mlexiconlistener);
        Log.d(LOG_TAG, "update lexicon fail, error code: " + ret);
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
    
    private LexiconListener mlexiconlistener = new LexiconListener() {
        @Override
        public void onLexiconUpdated(String lexiconId, SpeechError speechError) {
            String info;
            if (speechError != null) {
                info = "lexicon update fail, error code: " + speechError.getErrorCode();
                // TODO
            } else {
                info = "lexicon update success";
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
            mrecognitiondone = false;
            Toast.makeText(mcalleractivity, "Begin of Speech", Toast.LENGTH_SHORT);
        }
        
        @Override
        public void onEndOfSpeech() {
            Toast.makeText(mcalleractivity, "OnEndOfSpeech", Toast.LENGTH_SHORT);
            Log.d(LOG_TAG, "end of speech : " + System.currentTimeMillis());
        }
        
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean islast) {
            Log.d(TAG, "OnResult: begin : " + System.currentTimeMillis());
            if (null != recognizerResult) {
                String originResult = recognizerResult.getResultString();
                Log.d(LOG_TAG, "origin recognizer result: " + originResult);
                String parserResult = null;
                if (mResultType.equals("json")) {
                    if (menabletranslate) {
                        parserResult = parseTranslateResult(recognizerResult);
                    } else {
                        parserResult = parseNormalResult(recognizerResult);
                    }
                } else if (mResultType.equals("plain")) {
                    parserResult = recognizerResult.getResultString();
                }
                Log.d(LOG_TAG, "parser result: " + parserResult);
                mparserresult.append(parserResult);
                
                Log.d(TAG, "Onresult: end : " + System.currentTimeMillis());
                if (islast) {
                    Log.d(LOG_TAG, "last result:" + mparserresult + "-------------------------");
                    mrecognitiondone = true;
                    // Log.d(LOG_TAG, "action"+getAction());
                    
                }
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
    
    private String parseNormalResult(RecognizerResult result) {
        String text = JsonParser.parseIatResult(result.getResultString());
        String snPart = null;
        try {
            JSONObject resultJson = new JSONObject(result.getResultString());
            snPart = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return text;
    }
    
    private String parseTranslateResult(RecognizerResult result) {
        String dst = JsonParser.parseTransResult(result.getResultString(), "dst");
        String src = JsonParser.parseTransResult(result.getResultString(), "src");
        if (TextUtils.isEmpty(dst) || TextUtils.isEmpty(src)) {
            Log.d(LOG_TAG, "translate fail");
        }
        return dst;
    }
    
    private boolean SetParam() {
        boolean ret;
        ret = false;
        // mrecognizer.setParameter("engine_type", "cloud");
        mrecognizer.setParameter(SpeechConstant.PARAMS, null);
        mrecognizer.setParameter(SpeechConstant.ENGINE_TYPE, menginetype);
        mrecognizer.setParameter(SpeechConstant.RESULT_TYPE, mResultType);
        mrecognizer.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        
        mrecognizer.setParameter(SpeechConstant.LANGUAGE, mlanguage);
        mrecognizer.setParameter(SpeechConstant.ACCENT, maccent);
        if (menabletranslate) {
            Log.d(LOG_TAG, "translate enable");
            // TODO
            mrecognizer.setParameter(SpeechConstant.ASR_SCH, "1");
            mrecognizer.setParameter(SpeechConstant.ADD_CAP, "translate");
            mrecognizer.setParameter(SpeechConstant.TRS_SRC, "its");
            if (mlanguage.equalsIgnoreCase("en_us")) {
                mrecognizer.setParameter(SpeechConstant.ORI_LANG, "en");
                mrecognizer.setParameter(SpeechConstant.TRANS_LANG, "cn");
            } else if (mlanguage.equalsIgnoreCase("zh_cn")) {
                mrecognizer.setParameter(SpeechConstant.ORI_LANG, "cn");
                mrecognizer.setParameter(SpeechConstant.TRANS_LANG, "en");
            } else {
                Log.d(LOG_TAG, "unknown language type:" + mlanguage);
            }
        }
        mrecognizer.setParameter(SpeechConstant.VAD_BOS, MUTE_BEGIN_TIME);
        mrecognizer.setParameter(SpeechConstant.VAD_EOS, MUTE_STOP_TIME);
        mrecognizer.setParameter(SpeechConstant.ASR_PTT, ENABLE_PUNCTUATE);
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mrecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mrecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                Environment.getExternalStorageDirectory() + "/msc/asr.wav");
        
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
        Log.d(LOG_TAG, "stop time : " + System.currentTimeMillis());
        mrecognizer.stopListening();
    }
    
    public void cancelRecognize() {
        mrecognizer.cancel();
    }
    
    public String getAction() {
        Log.d(LOG_TAG, "get action in");
        
        if (mparserresult.length() == 0) {
            // 因为输入零字节的字符到Session.write中会报错,所以改为写入换行符
            Toast.makeText(mcalleractivity, "what?", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mcalleractivity, "[" + mparserresult.toString() + "]", Toast.LENGTH_SHORT).show();
        }
        StringBuffer ret = new StringBuffer();
        try {
            Log.d(LOG_TAG, "nyz ook up word" + mparserresult.toString());
            mlookuphandle.exactLookUpWord(mparserresult.toString().toLowerCase(), ret);
        } catch (DictionaryException e1) {
            try {
                mlookuphandle.fuzzyLookUpWord(mparserresult.toString(), ret);
            } catch (DictionaryException e2) {
                e1.printStackTrace();
                e2.printStackTrace();
                Log.e(LOG_TAG, "nyz not found" + mparserresult.toString());
            }
        }
        mparserresult.setLength(0);
        
        Log.d(LOG_TAG, "action result:" + ret + ";");
        Log.d(TAG, "getAction: end : " + System.currentTimeMillis());
        return ret.toString();
    }
    
    public void destroy() {
        if (null != mrecognizer) {
            mrecognizer.cancel();
            mrecognizer.destroy();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (null != mrecognizer) {
            // 退出时释放连接
            mrecognizer.cancel();
            mrecognizer.destroy();
        }
    }
    
    @Override
    protected void onResume() {
        // 开放统计 移动数据统计分析
        // FlowerCollector.onResume(IatDemo.this);
        // FlowerCollector.onPageStart(TAG);
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        // 开放统计 移动数据统计分析
        // FlowerCollector.onPageEnd(TAG);
        // FlowerCollector.onPause(IatDemo.this);
        super.onPause();
    }
}
