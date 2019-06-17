package com.iflytek.voicedemo;

public interface SpeechRecognitionInterface {
    int startRecognize();
    
    void stopRecognize();
    
    void cancelRecognize();
    
    String getAction();
    
    void destroy();
}
