package com.hq.picup;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import static com.hq.picup.R.id.map;

//, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
public class MapScreen extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    //    private GoogleApiClient mGoogleApiClient;
    private long mCount;

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
    private Location here;
    private StorageReference mStorage;
    private UploadTask uploadTask;
    private File photoFile;
    private Firebase mRef;
    private int stupidhack = 0;
    private ImageLoader imageLoader;
    private Hashtable<String, String> markerList;
    private Marker marker;
    private DisplayImageOptions options;

    private final LatLng HAMBURG = new LatLng(53.558, 9.927);
    private final LatLng KIEL = new LatLng(53.551, 9.993);


    private void initImageLoader() {
        int memoryCacheSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            int memClass = ((ActivityManager)
                    getSystemService(Context.ACTIVITY_SERVICE))
                    .getMemoryClass();
            memoryCacheSize = (memClass / 8) * 1024 * 1024;
        } else {
            memoryCacheSize = 2 * 1024 * 1024;
        }

        final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).threadPoolSize(5)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(memoryCacheSize)
                .memoryCache(new FIFOLimitedMemoryCache(memoryCacheSize-1000000))
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)//.enableLogging()
                .build();

        ImageLoader.getInstance().init(config);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_screen);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        initImageLoader();
        mImageView = (ImageView) findViewById(R.id.testCamView);
        mButton = (Button) findViewById(R.id.button1);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        mImageView.bringToFront();

        mStorage = FirebaseStorage.getInstance().getReference();
        Firebase.setAndroidContext(this);
        mRef = new Firebase("https://ivory-plane-150106.firebaseio.com/");

        markerList = new Hashtable<String,String>();
        //should map marker to 

        imageLoader  = ImageLoader.getInstance();


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    photoFile = null;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(MapScreen.this, "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
        if (mMap != null) {
            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());




            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {


                @Override
                public void onMyLocationChange(Location arg0) {
                    // TODO Auto-generated method stub
                    here = arg0;
                    if(stupidhack == 0) {
                        stupidhack++;
                        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(arg0.getLatitude(), arg0.getLongitude()));
                        CameraUpdate zoom = CameraUpdateFactory.zoomTo(17);

                        mMap.moveCamera(center);
                        mMap.animateCamera(zoom);
                    }
                }
            });
        }
//        mMap.setMyLocationEnabled(true);
//        myLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
//        //Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (myLocation != null) {
//            here = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
//            //mMap.moveCamera(CameraUpdateFactory.newLatLng(here));
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .target(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))      // Sets the center of the map to location user
//                    .zoom(17)                   // Sets the zoom
//                    // Sets the orientation of the camera to east
//                    // Sets the tilt of the camera to 30 degrees
//                    .build();                   // Creates a CameraPosition from the builder
//            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
//        }
    }

    private class MarkerCallback implements Callback {
        Marker marker=null;

        MarkerCallback(Marker marker) {
            this.marker=marker;
        }

        @Override
        public void onSuccess() {
            if (marker != null && marker.isInfoWindowShown()) {
                //marker.hideInfoWindow();
                marker.showInfoWindow();
            }
        }

        @Override
        public void onError() {}
    }
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{

//        private boolean not_first_time_showing_info_window;
        private View view;

        public CustomInfoWindowAdapter() {
            view = getLayoutInflater().inflate(R.layout.custom_info_window,
                    null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            if (MapScreen.this.marker != null
                    && MapScreen.this.marker.isInfoWindowShown()) {
                MapScreen.this.marker.hideInfoWindow();
                MapScreen.this.marker.showInfoWindow();
            }
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            MapScreen.this.marker = marker;


//
//            if (marker.getId() != null && markerList != null && markerList.size() > 0) {
//                if ( markerList.get(marker.getId()) != null &&
//                        markerList.get(marker.getId()) != null) {
//                    url = markerList.get(marker.getId());
//                }
//            }
            final ImageView image = ((ImageView) view.findViewById(R.id.badge));

//            if (url != null && !url.equalsIgnoreCase("null")
//                    && !url.equalsIgnoreCase("")) {
//                imageLoader.displayImage(url, image, options,
//                        new SimpleImageLoadingListener() {
//                            @Override
//                            public void onLoadingComplete(String imageUri,
//                                                          View view, Bitmap loadedImage) {
//                                super.onLoadingComplete(imageUri, view,
//                                        loadedImage);
//                                getInfoContents(marker);
//                            }
//                        });
//            } else {
//                image.setImageResource(R.drawable.com_facebook_button_like_icon_selected);
//            }
            //TODO: Properly get the uri
            mStorage.child("images").child("0").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if (uri != null) {
                        Picasso.with(MapScreen.this)
                                .load(uri)
                                .resize(200,300)
                                .into(image, new MarkerCallback(marker));
                    }
//                    if (not_first_time_showing_info_window) {
//                        Picasso.with(MapScreen.this).load(uri).into(image);
//                    } else { // if it's the first time, load the image with the callback set
//                        not_first_time_showing_info_window=true;
//                        Picasso.with(MapScreen.this).load(uri).into(image,new InfoWindowRefresher(marker));
//                    }
                }
            });

            final String title = marker.getTitle();
            final TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                titleUi.setText(title);
            } else {
                titleUi.setText("");
            }

            final String snippet = marker.getSnippet();
            final TextView snippetUi = ((TextView) view
                    .findViewById(R.id.snippet));
            if (snippet != null) {
                snippetUi.setText(snippet);
            } else {
                snippetUi.setText("");
            }

            return view;
        }
    }

    public void addPicture(PictureInfo picture){
        mRef.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                mCount = dataSnapshot.getChildrenCount();

                //Get a list of all data
                for (com.firebase.client.DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    PictureInfo post = postSnapshot.getValue(PictureInfo.class);
                    System.out.println(post.getLatitude());
                    System.out.println(post.getLongitude());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        mRef.child("Pic"+mCount).setValue(picture);
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
//        if (resultCode != RESULT_CANCELED) {

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                try {
                    mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
                    mImageView.setImageBitmap(mImageBitmap);



                    //Uri file = data.getData();
                    Uri file = Uri.fromFile(photoFile);
                    StorageReference filepath = mStorage.child("images").child(mCount+"");
                    //Uri file = Uri.fromFile(new File(mCurrentPhotoPath));
                    //Uri file = Uri.parse(mCurrentPhotoPath);

                    filepath.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                          @Override
                          public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                              Toast.makeText(MapScreen.this, "Upload Done", Toast.LENGTH_LONG).show();
                              PictureInfo taken = new PictureInfo(mCurrentPhotoPath,here.getLatitude(),here.getLongitude(), 0);
                              addPicture(taken);
                          }
                      }).addOnFailureListener(new OnFailureListener() {
                          @Override
                          public void onFailure(@NonNull Exception e) {
                              Toast.makeText(MapScreen.this, "Failure", Toast.LENGTH_LONG).show();
                          }
                      });

//                    Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();
//                    mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

//            final Marker hamburg = googleMap.addMarker(new MarkerOptions().position(HAMBURG)
//                    .title("Hamburg"));
//            markerList.put(hamburg.getId(), "pg");
//
//            final Marker kiel = googleMap.addMarker(new MarkerOptions()
//                    .position(KIEL)
//                    .title("Kiel")
//                    .snippet("Kiel is cool")
//                    .icon(BitmapDescriptorFactory
//                            .fromResource(R.drawable.messenger_bubble_large_blue)));
//            markerList.put(kiel.getId(), "images-sm.png");
//
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HAMBURG, 15));
//            googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
                    LatLng temp = new LatLng(here.getLatitude(), here.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(temp)

                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                } catch (IOException e) {
                    e.printStackTrace();//CameraScreen.this.finish();
                }
            }
        }
//    }
}
