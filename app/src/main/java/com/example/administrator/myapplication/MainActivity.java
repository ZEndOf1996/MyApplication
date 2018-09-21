package com.example.administrator.myapplication;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final int SELECT_PHOTO = 1; //显示Android自带图库，用于选择用户自己的图片
    public static final int TAKE_PHTOT = 2;//选择照片
    private String picPath;//图片存储路径
    private String sdPath;//SD卡的路径
    private RelativeLayout switchTakePhoto;
    private RelativeLayout switchAlbum;
    private ImageView switchImg;
    private Uri mImageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "image.jpg"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchTakePhoto = findViewById(R.id.switch_take_photo);
        switchAlbum = findViewById(R.id.switch_album);
        switchImg = findViewById(R.id.switch_img);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        sdPath = Environment.getExternalStorageDirectory().getPath();//设置拍照照片的保存路径
        picPath = sdPath + "/" + "image.jpg";

        switchTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        switchAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePhoto();
            }
        });
    }

    public void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PHOTO);//运行Intent事件
    }

    public void takePhoto() {
        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mImageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "image.jpg"));
//        mImageUri = getImageContentUri(new File(String.valueOf(Environment.getExternalStorageDirectory())));
//        intent2.setDataAndType(getImageContentUri(new File(String.valueOf(Environment.getExternalStorageDirectory()))), "image,jpg");
        //为拍摄的图片指定一个存储的路径
        intent2.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(intent2, TAKE_PHTOT);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_PHOTO://从相册选择照片
                if (requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
                    if (data != null)
                    {
                        switchImg.setImageURI(data.getData());
                    }
                }
                break;

            case TAKE_PHTOT://通过相机拍照生成头像
                FileInputStream fis = null;
                Log.d("PersonMsgActivity", "requestCode:===" + requestCode);
                Log.d("PersonMsgActivity", "resultCode:===" + resultCode);
                if (requestCode == TAKE_PHTOT && resultCode == Activity.RESULT_OK){
                    try {
                        //把图片转化为字节流
                        fis = new FileInputStream(picPath);
                        //把流转化图片
                        Bitmap bitmap = BitmapFactory.decodeStream(fis);
                        //将图片由Bitmap格式转换为URI
                        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null));
                        Log.d("PersonMsgActivity", "sdPath:===" + sdPath);
                        Log.d("PersonMsgActivity", "picPath:===" + picPath);
                        switchImg.setImageURI(uri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fis.close();//关闭流
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    public Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
}
