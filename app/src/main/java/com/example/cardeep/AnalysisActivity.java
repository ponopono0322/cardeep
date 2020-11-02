package com.example.cardeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerLocalModel;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AnalysisActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 0;
    private static boolean BUTTON_FROM_CAMERA = false;       //false:갤러리, true:카메라
    final private static String TAG = "my_image";
    String mCurrentPhotoPath;

    private TextView textView;
    private TextView inviText;
    private ImageView imageView;

    AutoMLImageLabelerLocalModel localModel = new AutoMLImageLabelerLocalModel.Builder()
            .setAssetFilePath("manifest.json")
            // or .setAbsoluteFilePath(absolute file path to manifest file)
            .build();

    AutoMLImageLabelerOptions autoMLImageLabelerOptions = new AutoMLImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(0.0f)  // Evaluate your model in the Firebase console
            // to determine an appropriate value.
            .build();
    ImageLabeler labeler = ImageLabeling.getClient(autoMLImageLabelerOptions);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        //이미지 뷰에 사진 올리기
        imageView = findViewById(R.id.imageView2);
        imageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                BUTTON_FROM_CAMERA = true;
                dispatchTakePictureIntent();
            }
        });
        //갤러리 통해서 이미지 얻기
        textView = findViewById(R.id.butten_get_image);
        textView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                BUTTON_FROM_CAMERA = false;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        //이미지 삭제 버튼
        inviText = findViewById(R.id.butten_delete_image);
        inviText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                imageView.setImageResource(R.drawable.ic_baseline_camera_alt_24);
                inviText.setVisibility(View.GONE);
            }
        });
        //결과화면 버튼
        Button button1 = (Button)findViewById(R.id.butten_get_data);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                InputImage image = InputImage.fromBitmap(bitmap, 0);
                labeler.process(image).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        String[] textarr = new String[10];
                        String[] textarr1 = new String[10];
                        int i = 0;
                        for (ImageLabel label : labels) {
                            String text = label.getText();
                            textarr[i] = text;
                            float confidence = label.getConfidence();
                            String s_num = Float.toString(confidence);
                            textarr1[i] = s_num;
                            i++;
                            if (i == 9) {
                                TextView textView1 = (TextView) findViewById(R.id.textView1);
                                textView1.setText(textarr[0]);
                                TextView textView2 = (TextView) findViewById(R.id.textView2);
                                textView2.setText(textarr1[0]);
                            }
                        }
                    }
                });
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "권한 설정 완료");
            } else {
                Log.d(TAG, "권한 설정 요청");
                ActivityCompat.requestPermissions(AnalysisActivity.this, new String[]
                        {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    //권한 요청
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission:" + permissions[0] + "was" + grantResults[0]);
        }
    }

    //카메라로 찍은 영상을 가져오는 부분
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                try {
                    inviText.setVisibility(View.VISIBLE);
                    if(BUTTON_FROM_CAMERA == true) {        //카메라를 통해 이미지 얻는 경우
                        File file = new File(mCurrentPhotoPath);
                        Bitmap bitmap;
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
                        imageView.setImageBitmap(bitmap);
                    }
                    else {                                  //갤러리를 통해 이미지 얻는 경우
                        InputStream in = getContentResolver().openInputStream(data.getData());
                        Bitmap img = BitmapFactory.decodeStream(in);
                        in.close();
                        imageView.setImageBitmap(img);
                    }
                } catch(Exception e) { }
            }
            else if(resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg", storageDir);

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try { photoFile = createImageFile(); }
            catch (IOException ex) {}
            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.cardeep.fileprovider",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CODE);
            }
        }
    }
}