package com.jason.capstone.Report;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jason.capstone.Mail.JavaMailAPI;
import com.jason.capstone.R;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ReportFragment extends Fragment {

    private static final String TAG = "ReportFragment";
    private Button mReportButton, mClassifyButton;
    private TextView mTextField;
    private ImageView mImageView;
    private static final String  KEY_COLLECTION = "Users";

    private static final String C_FILEPATH = "BuildingDefects";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String[] s;
    private static final String KEY_COMPONENT = "component";
    private static final String KEY_DATE = "date";
    private static final String KEY_DEFECT = "defect";
    private static final String KEY_LENGTH = "length";
    private static final String KEY_PRIORITY = "priority";

    private long startNow;
    private long endNow;

    //All Tensorflow initialization
    protected Interpreter tflite;
    private TensorImage inputImageBuffer;
    private int imageSizeX;
    private int imageSizeY;
    private TensorBuffer outputProbabilityBuffer;
    private TensorProcessor probabilityProcessor;
    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 1.0f;
    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROBABILITY_STD = 255.0f;
    private Bitmap photo;
    private List<String> labels;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 1001;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report, container, false);

        mReportButton = v.findViewById(R.id.report_button);
        mImageView = v.findViewById(R.id.report_image);
        mClassifyButton = v.findViewById(R.id.classify_button);
        mTextField = v.findViewById(R.id.report_text);

        mTextField.setText(R.string.report_text);

        mImageView.setOnClickListener(v1 -> cameraFunction());

        try {
            tflite = new Interpreter(loadModelFile(getActivity()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mClassifyButton.setOnClickListener(v2 -> classifyImage());

        mReportButton.setOnClickListener(v3 -> sendEmail());

        return v;
    }

    private void classifyImage() {
        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
        imageSizeY = imageShape[1];
        imageSizeX = imageShape[2];
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();

        int probabilityTensorIndex = 0;
        int[] probabilityShape =
                tflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES}
        DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

        inputImageBuffer = new TensorImage(imageDataType);
        outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
        probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build();

        inputImageBuffer = loadImage(photo);

        tflite.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind());
        showResult();
    }

    private void cameraFunction() {
        if (hasPermission()) {
            openCamera();
        } else {
            requestPermission();
        }
    }

    private void sendEmail() {
        if(s == null) {
            Toast.makeText(getActivity(), "Please classify image first", Toast.LENGTH_SHORT).show();
            mClassifyButton.requestFocus();
        } else {
            final String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                    .format(new Date());
            final String timeMillis = String.valueOf(System.currentTimeMillis());
            Map<String, Object> upload = new HashMap<>();
            upload.put(KEY_COMPONENT, s[0]);
            upload.put(KEY_DATE, currentDate);
            upload.put(KEY_DEFECT, s[1]);
            upload.put(KEY_LENGTH, s[2]);
            upload.put(KEY_PRIORITY, s[3]);

            db.collection(C_FILEPATH).document(timeMillis).set(upload).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getActivity(), "Report Successful", Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, e.toString());
                }
            });
        }


        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot d : task.getResult()){
                        String mEmail = d.getString("email");
                        String mSubject = "Building Defect: " + s[0] + ", Priority: " + s[3];
                        String mMessage = "Dear Building Occupants,\n\n" +
                                "A new building defect has been reported in your building, please check the application for more details. The facility manager has been notified on the issues.";

                        JavaMailAPI javaMailAPI = new JavaMailAPI(getActivity(), mEmail, mSubject, mMessage);
                        javaMailAPI.execute();
                    }
                }
            }
        });
    }

    private TensorImage loadImage(final Bitmap bitmap) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap);

        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        // TODO(b/143564309): Fuse ops inside Imag eProcessor.
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .add(getPreprocessNormalizeOp())
                        .build();
        return imageProcessor.process(inputImageBuffer);
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("model-export_icn_tflite-cement_20210527112317-2021-05-27T04_38_38.690533Z_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startoffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startoffset, declaredLength);
    }

    private TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }

    private TensorOperator getPostprocessNormalizeOp() {
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }

    private void showResult()
    {   startNow = android.os.SystemClock.uptimeMillis();
        try {
            labels = FileUtil.loadLabels(getActivity(), "model-export_icn_tflite-cement_20210527112317-2021-05-27T04_38_38.690533Z_dict.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer))
                        .getMapWithFloatValue();
        float maxValueInMap = (Collections.max(labeledProbability.values()));

        for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
            if (entry.getValue() == maxValueInMap) {
                s = entry.getKey().split("_");
                Log.e(TAG, Arrays.toString(s));
                if(s[1].contentEquals("Large")){
                    mTextField.setText("Large Cement Crack\n5cm");
                } else {
                    mTextField.setText("Hairline Crack\n2cm");
                }
            }
        }
        endNow = android.os.SystemClock.uptimeMillis();
        Log.e(TAG, "Execution Time: " + (endNow - startNow) + "ms");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            photo = (Bitmap) Objects.requireNonNull(Objects.requireNonNull(data).getExtras()).get("data");
            mImageView.setImageBitmap(photo);
            mImageView.setBackground(null);
            mTextField.setText("Click Classify to get result");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (hasAllPermissions(grantResults)) {
                openCamera();
            } else {
                requestPermission();
            }
        }
    }

    private boolean hasAllPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), CAMERA_REQUEST_CODE);
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(getActivity(), "Camera Permission Required", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

}