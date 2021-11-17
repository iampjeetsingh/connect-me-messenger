package com.connectme.messenger;

import android.os.Environment;

public class DIRECTORY {
    public static String SDCARD = Environment.getExternalStorageDirectory()+"",
            APP = SDCARD+"/ConnectMe",
            IMAGE = APP+"/Images",
            VOICE = APP+"/Voices",
            AUDIO = APP+"/Audios",
            VIDEO = APP+"/Videos",
            DOCUMENT = APP+"/Documents";
}
