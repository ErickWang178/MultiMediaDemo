package com.erick.multimediademo.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by Erick on 2017/9/28 0028.
 * Email: jsp_103@163.com
 * version: V1.0
 */

public class MediaStoreUtil {
    private static String TAG = "MediaStoreUtil";



    /**
     * 获取设备上所有的视频信息
     */
    public static void getVideosFromDevice(Context context) {
       // System.out.println("******************** Video *********************");

        if (Build.VERSION.SDK_INT >= 10) {
            getVideosFromDeviceAboveV10(context);
        } else {
            getVideosFromDeviceBelowV9(context);
        }

        return;
    }

    private static void getVideosFromDeviceBelowV9(Context context) {
        ContentResolver contentResolver = context.getContentResolver();

        String[] videoColumns = null;
        videoColumns =new String[]{
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
        };

        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoColumns, null, null, null);
        if (cursor == null) return;

        while (cursor.moveToNext()) {
            String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            final String lastModified = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED));
            final String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
            final String size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

            final File f = new File(filePath);

            if (!f.exists()) continue;

            final Uri uri = Uri.fromFile(f);
            final MediaPlayer player = new MediaPlayer();

            try {
                player.reset();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(filePath);
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        int width = mp.getVideoWidth();
                        int height = mp.getVideoHeight();

                        if (TextUtils.isEmpty(duration) || TextUtils.isEmpty(lastModified) || TextUtils.isEmpty(size)){
                            return;
                        }

                        String title = f.getName();
                        title = title.substring(0,title.lastIndexOf('.'));

                        long length = (long)Float.parseFloat(duration);
                        long mwLastModified = (long) Float.parseFloat(lastModified);
                        long mwSize = (long) Float.parseFloat(size);

                       // Log.d(TAG, "getVideosFromDeviceV16: videoWidth==" + width + ",videoHeight==" + height + ",uri==" + uri.toString());

                        player.release();
                    }
                });

                player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        System.out.println("无法解析，无法播放，从数据库中删除" + uri);
                        player.release();
                        return false;
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
    }

    /**
     * 在API10以上使用MediaMetadataRetriever类进行解析
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD_MR1)
    private static void getVideosFromDeviceAboveV10(Context context) {
        ContentResolver contentResolver = context.getContentResolver();

        String[] videoColumns = null;
        videoColumns =new String[]{
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
        };

        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoColumns, null, null, null);
        if (cursor == null) return;

        while (cursor.moveToNext()) {
            String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            final String lastModified = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED));
            final String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
            final String size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

            Log.e(TAG, "getVideosFromDeviceAboveV10: " + filePath);

            final File f = new File(filePath);

            if (!f.exists()) continue;

            final Uri uri = Uri.fromFile(f);

            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(filePath);
                Bitmap bitmap = retriever.getFrameAtTime();

                //在华为Android 2.3.3的测试机上bitmap==null
                //无法获取缩略图就无法获得视频的宽高，所以只能使用最原始的方法。
                if(bitmap == null){
                    getVideosFromDeviceBelowV9(context);

                    cursor.close();
                    return;
                }
                int videoWidth = bitmap.getWidth();
                int videoHeight = bitmap.getHeight();

                bitmap.recycle();

              //  Log.d(TAG, "getVideosFromDeviceAboveV10 11111: videoWidth==" + videoWidth + ",videoHeight==" + videoHeight + ",uri==" + uri.getPath());
                if (TextUtils.isEmpty(duration) || TextUtils.isEmpty(lastModified) || TextUtils.isEmpty(size)){
                    return;
                }

                String title = f.getName();
                title = title.substring(0,title.lastIndexOf('.'));

                long length = (long)Float.parseFloat(duration);
                long mwLastModified = (long) Float.parseFloat(lastModified);
                long mwSize = (long) Float.parseFloat(size);

                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();

                getVideosFromDeviceBelowV9(context);
            }
        }
        cursor.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private static void getVideosFromDeviceV16(Context context) {
        ContentResolver contentResolver = context.getContentResolver();

        String[] videoColumns = null;

        /**
         *  缺陷：
         *  MediaStore.Video.Media.WIDTH和MediaStore.Video.Media.HEIGHT 虽然可以得出视频的宽和高，
         *  但是只是把长的做宽，短的做高，造成和视频的真实宽高不符，例如：竖屏录的手机视频高＞宽，结果就反了。
         *  坑我好多时间。。。。。。。。
         */
        videoColumns =new String[]{
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE

        };

        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoColumns, null, null, null);
        if (cursor == null) return;

        while (cursor.moveToNext()) {
            String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            String width = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH));
            String height = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT));
            String lastModified = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
            String size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

            File f = new File(filePath);
            Uri uri = Uri.fromFile(f);
            if (!f.exists() || TextUtils.isEmpty(duration) || TextUtils.isEmpty(width)
                    || TextUtils.isEmpty(height) || TextUtils.isEmpty(lastModified) || TextUtils.isEmpty(size)){
                continue;
            }

            Log.e(TAG, "getVideosFromDeviceV16: " + filePath);

            String title = f.getName();
            title = title.substring(0,title.lastIndexOf('.'));

            long length = (long)Float.parseFloat(duration);
            int mwWidth = (int) Float.parseFloat(width);
            int mwHeight = (int) Float.parseFloat(height);
            long mwLastModified = (long) Float.parseFloat(lastModified);
            long mwSize = (long) Float.parseFloat(size);
        }
        cursor.close();
    }

    //获取设备上所有的音频信息
    public static void getAudiosFromDevice(Context context) {
       // System.out.println("******************** Audio *********************");

        ContentResolver contentResolver = context.getContentResolver();
        String[] audioColumns = new String[]{
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.AlbumColumns.ARTIST,
        };

        Cursor cursor = contentResolver.query
                (MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioColumns, null, null, null);

        if (cursor == null) return;

        while (cursor.moveToNext()) {
            String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            String lastModified = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED));
            String size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
            String albumArist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ARTIST));

            File f = new File(filePath);
            Uri uri = Uri.fromFile(f);
            if (!f.exists() || TextUtils.isEmpty(duration)){
                continue;
            }

            String title = f.getName();
            title = title.substring(0,title.lastIndexOf('.'));

            long length = (long)Float.parseFloat(duration);
            long mwLastModified = (long) Float.parseFloat(lastModified);
            long mwSize = (long) Float.parseFloat(size);
           // System.out.println("audio-filePath=" + filePath);
        }
        cursor.close();

    }

    //获取歌曲封面
    public static Bitmap getAudioDefauleCover(Context context,String audioPath) {
        Bitmap bm = null;
        int album_id = getAlbumId(context,audioPath);
        String albumArt = getAlbumArt(context,album_id);

        if (albumArt != null)
            bm = BitmapFactory.decodeFile(albumArt);

        return bm;
    }

    /**
     * 通过MP3路径得到指向当前MP3的Cursor
     *
     * @param filePath
     *            MP3路径
     *
     * @return Cursor 返回的Cursor指向当前MP3
     */
    private static int getAlbumId(Context context, String filePath) {
        int albumId = 0;
        String path = null;
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);


        if (c.moveToFirst()) {
            do {
                // 通过Cursor 获取路径，如果路径相同则break；
                path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                // 查找到相同的路径则返回，此时cursorPosition 便是指向路径所指向的Cursor 便可以返回了
                if (path.equals(filePath)) {
                    albumId = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                    break;
                }
            } while (c.moveToNext());

            c.close();
        }

        return albumId;
    }

    /**
     *
     * 功能 通过album_id查找 album_art 如果找不到返回null
     *
     * @param album_id
     * @return album_art
     */
    private static String getAlbumArt(Context context, int album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[] { "album_art" };
        Cursor cur = context.getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
                projection, null, null, null);

        String album_art = null;

        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }

        cur.close();
        cur = null;

        return album_art;
    }

}
