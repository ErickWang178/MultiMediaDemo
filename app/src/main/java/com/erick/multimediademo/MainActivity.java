package com.erick.multimediademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ContentFrameLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.erick.multimediademo.adapter.FrameAdapter;
import com.erick.multimediademo.util.Constants;
import com.erick.multimediademo.util.VideoTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static String TAG = MainActivity.class.getSimpleName();

    private Button btnExtractFrame;
    private Button btnShowFrame;
    private RecyclerView mFrameList;
    private FrameAdapter mFrameAdapter;
    private List<String> mPathList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // MediaStoreUtil.getVideosFromDevice(this);

        File file = new File(Constants.PATH_VIDEO);
        if (file.exists()){
            Log.d(TAG, "onCreate: 视频文件存在");
        }

        btnExtractFrame = (Button) findViewById(R.id.btn_extract_frame);
        btnExtractFrame.setOnClickListener(this);

        btnShowFrame = (Button) findViewById(R.id.btn_show_frame);
        btnShowFrame.setOnClickListener(this);

        mFrameList = (RecyclerView) findViewById(R.id.list_frame);
        LinearLayoutManager llm = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,false);
        mFrameList.setLayoutManager(llm);
        mFrameAdapter = new FrameAdapter();
        mFrameList.setAdapter(mFrameAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_extract_frame:
                VideoTools.extractFrame(Constants.PATH_VIDEO);
                Toast.makeText(this,"提取图片完成",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_show_frame:
                showFrame();
                break;
        }
    }

    private void showFrame() {
        File dirFile = new File(Constants.PATH_FRAME);
        if (!dirFile.exists()){
            mFrameAdapter.setList(mPathList);
        } else {
            File[] files = dirFile.listFiles();
            for (File f : files) {
                mPathList.add(f.getPath());
            }
            mFrameAdapter.setList(mPathList);
        }
    }
}
