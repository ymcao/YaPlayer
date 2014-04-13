package com.yamin.kk.utils;

import android.util.Log;

public class AppLog {

	    public static boolean enableDebug=false;
	    
        public static void debug(String tag,String msg){
        	if(enableDebug){
        		Log.d(tag, msg);
        	}
        }
        public static void logi(String tag,String msg){
        	if(enableDebug){
        		Log.i(tag, msg);
        	}
        }
        public static void loge(String tag,String msg){
        	if(enableDebug){
        		Log.e(tag, msg);
        	}
        }
	
}
