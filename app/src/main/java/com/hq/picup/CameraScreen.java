//package com.hq.picup;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.nfc.Tag;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import java.io.File;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//public class CameraScreen extends AppCompatActivity {
//
//    static final int REQUEST_IMAGE_CAPTURE = 1;
//    private Bitmap mImageBitmap;
//    private String mCurrentPhotoPath;
//    public ImageView mImageView;
//    private Button mButton;
//    private static final int CAMERA_REQUEST = 1888;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_camera_screen);
//
//        mImageView = (ImageView) findViewById(R.id.testCamView);
//        mButton = (Button) findViewById(R.id.button1);
//
//        mButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
//                    File photoFile = null;
//                    try {
//                        photoFile = createImageFile();
//                    } catch (IOException e) {
//                        Toast.makeText(CameraScreen.this, "wtf", Toast.LENGTH_SHORT).show();
//                    }
//                    if(photoFile!= null){
//                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
//                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
//                    }
//                }
//            }
//        });
//
////        Intent makeCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
////        if(makeCameraIntent.resolveActivity(getPackageManager()) != null) {
////            File photoFile = null;
////            try {
////                photoFile = createImageFile();
////            } catch (IOException e) {
////                Toast.makeText(this, "wtf", Toast.LENGTH_SHORT).show();
////            }
////
////            if (photoFile != null){
////                makeCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
////            }
////
////        }
//
//    }
//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  // prefix
//                ".jpg",         // suffix
//                storageDir      // directory
//        );
//
//        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
//        return image;
//    }
//
//
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(resultCode != RESULT_CANCELED){
//
//            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
//                try {
//                    mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
//                    mImageView.setImageBitmap(mImageBitmap);
//                } catch (IOException e){
//                    e.printStackTrace();//CameraScreen.this.finish();
//                }
//            }
//        }
//
////        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
////            try {
////                mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
////                mImageView.setImageBitmap(mImageBitmap);
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////        }
//    }
//
//}
