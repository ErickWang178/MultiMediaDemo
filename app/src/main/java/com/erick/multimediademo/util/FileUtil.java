package com.erick.multimediademo.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by Administrator on 2018/1/30 0030.
 */

public class FileUtil {
    private static String TAG = FileUtil.class.getSimpleName();

    public static String getFileNameFromPath(String path){
        if (TextUtils.isEmpty(path)) return null;

        File file = new File(path);

        if (file == null || !file.exists()) return null;

        return file.getName();
    }

    /**
     * 将Bitmap保存到磁盘
     * @param dir 保存的文件夹
     * @param fName 保存后的文件名字
     * @param percent 图片压缩比例
     * @param isRecycle 图片资源是否被回收
     */
    public static void saveBitmapToSDCard(Bitmap bitmap, String dir, String fName, int percent, boolean isRecycle){
        if (bitmap == null || TextUtils.isEmpty(dir) || TextUtils.isEmpty(fName)){
            Log.d(TAG, "saveBitmapToSDCard: 参数错误，保存图片失败");
            return;
        }
        File dirFile = new File(dir);
        if (!dirFile.exists()) dirFile.mkdirs(); //目录不存在，创建目录

        String fileName = dir + File.separator + fName;

        File fileBitmap = new File(fileName);
        if (fileBitmap.exists()) {
            Log.d(TAG, "saveBitmapToSDCard: 文件已经存在，无需创建");
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(fileBitmap);
            boolean result = bitmap.compress(Bitmap.CompressFormat.JPEG, percent, fos);
            if (result){
                fos.flush();
                fos.close();
            } else {
                Log.d(TAG, "saveBitmapToSDCard: 图片写入失败");
                return;
            }
            if (bitmap.isRecycled()){
                bitmap.recycle();
            }

            Log.d(TAG, "saveBitmapToSDCard: 图片写入成功,path = " + fileName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
