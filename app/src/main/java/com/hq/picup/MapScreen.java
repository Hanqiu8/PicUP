package com.hq.picup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.hq.picup.R.id.map;

//, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
public class MapScreen extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    //    private GoogleApiClient mGoogleApiClient;
    private Location myLocation;
    private Criteria criteria;
    private LocationManager locationManager;
//    private ImageView cameraButton;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    public ImageView mImageView;
    private Button mButton;
    private static final int CAMERA_REQUEST = 1888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_screen);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        mImageView = (ImageView) findViewById(R.id.testCamView);
        mButton = (Button) findViewById(R.id.button1);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        mImageView.bringToFront();


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        Toast.makeText(MapScreen.this, "wtf", Toast.LENGTH_SHORT).show();
                    }
                    if(photoFile!= null){
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });
//        cameraButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Toast.makeText(MapScreen.this, "Start Camera", Toast.LENGTH_SHORT).show();
////                Intent startCameraIntent = new Intent(MapScreen.this, CameraScreen.class);
////                MapScreen.this.startActivity(startCameraIntent);
////                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
//                    File photoFile = null;
//                    try {
//                        photoFile = createImageFile();
//                    } catch (IOException e) {
//                        Toast.makeText(MapScreen.this, "wtf", Toast.LENGTH_SHORT).show();
//                    }
//                    if (photoFile != null) {
//                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
//                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
//                    }
//                }
//
//                return true;
//            }
//        });


    }
//        if (mGoogleApiClient == null) {
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build();
//
//        }
//    @Override
//    protected void onStart(){
//        super.onStart();
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    protected void onStop() {
//        mGoogleApiClient.disconnect();
//        super.onStop();
//    }
//
//    @Override
//    public void onConnectionSuspended(int n){
//        //ummm
//        mGoogleApiClient.connect();
//    }
//    @Override
//    public void onConnected(Bundle b){
//        myLocation = LocationServices.FusedLocationApi.getLastLocation(
//                mGoogleApiClient);
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult r){
//        Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();
//    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        myLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        //Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (myLocation != null) {
            LatLng here = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(here));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    // Sets the orientation of the camera to east
                    // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        }
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                try {
                    mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
                    mImageView.setImageBitmap(mImageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();//CameraScreen.this.finish();
                }
            }
        }
    }
}
