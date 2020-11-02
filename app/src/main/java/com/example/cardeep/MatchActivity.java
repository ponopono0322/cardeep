package com.example.cardeep;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerLocalModel;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerOptions;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerRemoteModel;

import java.util.List;

public class MatchActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_match);

        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        ImageView imageview = (ImageView)findViewById(R.id.customImage);
        imageview.setImageBitmap(bitmap);

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        labeler.process(image).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        // Task completed successfully
                        // ...
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
}
