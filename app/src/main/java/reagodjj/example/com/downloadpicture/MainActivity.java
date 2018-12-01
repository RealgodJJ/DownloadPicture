package reagodjj.example.com.downloadpicture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    public static final int DOWNLOAD_SUCCESS = 1;
    public static final String HTTPS_DOWNLOAD = "https://img2.mukewang.com/5adfee7f0001cbb906000338-240-135.jpg";
    public static final int DOWNLOAD_FAIL = 0;
    private TextView tvProgress;
    private ProgressBar pbProgress;
    private Button btDownload;
    private ImageView ivShow;
    private DownloadHandler downloadHandler;
    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        downloadHandler = new DownloadHandler(this);

        btDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(HTTPS_DOWNLOAD);
                            URLConnection urlConnection = url.openConnection();
                            InputStream inputStream = urlConnection.getInputStream();

                            //获取文件总长度
                            int contentLength = urlConnection.getContentLength();
                            //设置文件存储的路径
                            String downloadFolderName = Environment.getExternalStorageDirectory()
                                    + File.separator + "imooc" + File.separator;

                            //判断文件夹是否存在
                            File folder = new File(downloadFolderName);
                            if (!folder.exists()) {
                                folder.mkdir();
                            }

                            //判断文件是否存在
                            filename = downloadFolderName + "picture.png";
                            File file = new File(filename);
                            if (file.exists()) {
                                file.delete();
                            }

                            int downloadSize = 0;//当前下载文件大小
                            byte bytes[] = new byte[1024];
                            int length;

                            OutputStream outputStream = new FileOutputStream(filename);
                            while ((length = inputStream.read(bytes)) != -1) {
                                outputStream.write(bytes, 0, length);
                                downloadSize += length;
                            }

                            Message message = Message.obtain();
                            message.what = DOWNLOAD_SUCCESS;
                            message.obj = downloadSize * 100 / contentLength;
                            downloadHandler.sendMessage(message);
                            inputStream.close();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    private void initView() {
        tvProgress = findViewById(R.id.tv_progress);
        pbProgress = findViewById(R.id.pb_progress);
        btDownload = findViewById(R.id.bt_download);
        ivShow = findViewById(R.id.iv_show);
        tvProgress.setText(String.format(getResources().getString(R.string.progress), 0));
    }

    private static class DownloadHandler extends Handler {
        WeakReference<MainActivity> mainActivityWeakReference;

        DownloadHandler(MainActivity mainActivity) {
            mainActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            MainActivity mainActivity = mainActivityWeakReference.get();
            if (mainActivity != null) {
                switch (msg.what) {
                    case DOWNLOAD_SUCCESS:
                        mainActivity.pbProgress.setProgress((Integer) msg.obj);
                        mainActivity.tvProgress.setText(String.format(mainActivity.getResources().getString(R.string.progress),
                                (Integer) msg.obj));

                        if ((Integer) msg.obj == 100) {
                            Bitmap bitmap = BitmapFactory.decodeFile(mainActivity.filename);
                            mainActivity.ivShow.setImageBitmap(bitmap);
                            Uri uri = Uri.fromFile(new File(mainActivity.filename));
                            mainActivity.ivShow.setImageURI(uri);
                        }
                        break;
                    case DOWNLOAD_FAIL:
                        mainActivity.tvProgress.setText(R.string.download_failed);
                        break;
                }
            }
        }
    }
}
