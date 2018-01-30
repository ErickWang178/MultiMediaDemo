package com.erick.multimediademo.util;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public class VideoTools {
    private static String TAG = VideoTools.class.getSimpleName();

    public static void extractFrame(String path){
        if (TextUtils.isEmpty(path)) return;
        if (Build.VERSION.SDK_INT < 10) return;
        String fileName = FileUtil.getFileNameFromPath(path);

        if (fileName == null) return;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        long duration = Long.parseLong(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000; //转成微秒
        int sum = 50; //总帧数
        long step = duration / sum; // 每两帧之间的时间间隔
        int count = 1;
        Bitmap bitmap = null;
        for (long i=0; i<=duration - 100000; i += step){
            bitmap = retriever.getFrameAtTime(i,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            FileUtil.saveBitmapToSDCard(
                    bitmap,Constants.PATH_FRAME,fileName + "-" + count + ".jpeg",
                    60,true);
            Log.d(TAG, "extractFrame: 保存 ： " + count);
            count ++;
        }

        retriever.release();
    }
}
