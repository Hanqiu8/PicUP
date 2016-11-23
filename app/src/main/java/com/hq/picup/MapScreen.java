package com.hq.picup;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
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
import com.google.firebase.database.DatabaseError;
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

import static com.google.android.gms.analytics.internal.zzy.e;
import static com.hq.picup.R.id.map;

//, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
public class MapScreen extends FragmentActivity implements OnMapReadyCallback {

    private long SURVIVAL_TIME = 2000000000;
    final private long UPVOTE_SUPPLEMENT = 10000;
    private GoogleMap mMap;
    //    private GoogleApiClient mGoogleApiClient;
    private long mCount;
    public static final String PREFS_NAME = "voteDataFile";

    private Criteria criteria;
    private LocationManager locationManager;
//    private ImageView cameraButton;
    static final int REQUEST_IMAGE_CAPTURE = 1;
//    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
//    public ImageView mImageView;
    private Button mButton;
    private Button mUpVote;

    private Location here;
    private StorageReference mStorage;
    private UploadTask uploadTask;
    private File photoFile;
    private Firebase mRef;
    private int stupidhack = 0;
    private ImageLoader imageLoader;
//    private Hashtable<String, String> voteList;
    private Marker marker;
    private SharedPreferences.Editor editor;


    private SharedPreferences prefSplash;

//    private DisplayImageOptions options;

    private int picNum=0;



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
        prefSplash = getSharedPreferences(PREFS_NAME,0);
        editor = prefSplash.edit();
//        mImageView = (ImageView) findViewById(R.id.testCamView);
        mButton = (Button) findViewById(R.id.button1);
        mUpVote = (Button) findViewById(R.id.button2);
        mUpVote.setVisibility(View.GONE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
//        mImageView.bringToFront();

        mStorage = FirebaseStorage.getInstance().getReference();
        Firebase.setAndroidContext(this);
        mRef = new Firebase("https://ivory-plane-150106.firebaseio.com/");

//        voteList = new Hashtable<String, String>();
        //should map marker to 

        imageLoader  = ImageLoader.getInstance();
        //gets the number of elements stored online
        getTotalNum();

        //Toast.makeText(MapScreen.this, picNum + "", Toast.LENGTH_LONG).show();
        //This gets all the children from Firebase
            mRef.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {

                @Override
                public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                    PictureInfo value;
                    try {
                        //Iterate through all the children and determine which markers to display
                        for(DataSnapshot child : dataSnapshot.getChildren()){
                            value = child.getValue(PictureInfo.class);
                            //if the marker is not beyond the survival time
                            if( ((System.currentTimeMillis() / 1000L) - value.getTime()-(UPVOTE_SUPPLEMENT*value.getVote()))<SURVIVAL_TIME ) {
                                LatLng temp = new LatLng(value.getLongitude(), value.getLatitude());

                                Marker test = mMap.addMarker(new MarkerOptions().position(temp).title(Integer.toString(value.getVote()))
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                test.setTag(value.getUrl());
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(MapScreen.this, "Nothing found", Toast.LENGTH_LONG).show();

                    }//mRef.child("Pic1").child("vote").setValue(value.getVote()+1);

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

        //Opens camera to take picture and returns it in the onActivitySucceeded method
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
//

    }
//        if (mGoogleApiClient == null) {
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build();
//        }

    //Runs after map is loaded
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
            //Moves app to current location
            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());


            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {


                @Override
                public void onMyLocationChange(Location arg0) {
                    // TODO Auto-generated method stub
                    here = arg0;
                    //only updates location once at beginnning of launch
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
//This class used to implement the info windows
    private class MarkerCallback implements Callback {
        Marker marker=null;

        MarkerCallback(Marker marker) {
            this.marker=marker;
        }


        @Override
        public void onSuccess() {
            if (marker != null && marker.isInfoWindowShown()) {
//                marker.hideInfoWindow();
//                LatLng tempp = new LatLng(marker.getPosition().latitude+0.001, marker.getPosition().longitude);

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

            if (MapScreen.this.marker != null && MapScreen.this.marker.isInfoWindowShown()) {
                MapScreen.this.marker.hideInfoWindow();

                MapScreen.this.marker.showInfoWindow();
            }
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            MapScreen.this.marker = marker;

            final ImageView image = ((ImageView) view.findViewById(R.id.badge));

            //Finds the image from firebase and stores it
            getTotalNum();
            String wth = (String) marker.getTag();
            mUpVote.setVisibility(View.VISIBLE);
            mStorage.child("images").child(wth).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if (uri != null) {
                        MarkerCallback temp = new MarkerCallback(marker);
                        Picasso.with(MapScreen.this)
                                .load(uri)
                                .resize(200,300)
                                .into(image,temp);
                    }
//                    if (not_first_time_showing_info_window) {
//                        Picasso.with(MapScreen.this).load(uri).into(image);
//                    } else { // if it's the first time, load the image with the callback set
//                        not_first_time_showing_info_window=true;
//                        Picasso.with(MapScreen.this).load(uri).into(image,new InfoWindowRefresher(marker));
//                    }
                }
            });



//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tempp, 17));
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

            mUpVote.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    mRef.child("Pic"+marker.getTag()).addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                        @Override
                        public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                            PictureInfo value = dataSnapshot.getValue(PictureInfo.class);
                            if(!prefSplash.getBoolean(value.getUrl(),false)){

                                editor.putBoolean(value.getUrl(), true);
                                mRef.child("Pic"+marker.getTag()).child("vote").setValue(value.getVote()+1);
                                marker.setTitle(Integer.toString(value.getVote()+1));}
                                editor.commit();
                            editor.apply();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
            });

            final String title = marker.getTitle();
            final TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                titleUi.setText(title);
            } else {
                titleUi.setText("");
            }

//            final String snippet = marker.getSnippet();
//            final TextView snippetUi = ((TextView) view
//                    .findViewById(R.id.snippet));
//            if (snippet != null) {
//                snippetUi.setText(snippet);
//            } else {
//                snippetUi.setText("");
//            }

            return view;
        }
    }

    public void getTotalNum(){

//        com.firebase.client.DataSnapshot tempSnapshot;
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                picNum = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void addPicture(PictureInfo picture){
        mRef.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                mCount = dataSnapshot.getChildrenCount();
                picNum = (int) mCount;
                //This should be fine in our case as long as we don't upload over 2 billion images

                //Get a list of all data
//                for (com.firebase.client.DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
//                    PictureInfo post = postSnapshot.getValue(PictureInfo.class);
//                    System.out.println(post.getLatitude());
//                    System.out.println(post.getLongitude());
//                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        mRef.child("Pic"+(picNum)).setValue(picture);
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
//                try {
//                    mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
//                    mImageView.setImageBitmap(mImageBitmap);


                    //Uri file = data.getData();
                    Uri file = Uri.fromFile(photoFile);
                    getTotalNum();
                    StorageReference filepath = mStorage.child("images").child(picNum+"");
                    //Uri file = Uri.fromFile(new File(mCurrentPhotoPath));
                    //Uri file = Uri.parse(mCurrentPhotoPath);

                    filepath.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                          @Override
                          public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                              Toast.makeText(MapScreen.this, "Upload Done", Toast.LENGTH_LONG).show();
                              Long currentTime =  System.currentTimeMillis() / 1000L;
                              PictureInfo taken = new PictureInfo(""+picNum,here.getLatitude(),here.getLongitude(), 0,currentTime);
                              addPicture(taken);

                              LatLng temp = new LatLng(here.getLatitude(), here.getLongitude());
                              Marker test = mMap.addMarker(new MarkerOptions().position(temp)
                                      .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                              test.setTag(picNum+"");

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
//                    LatLng temp = new LatLng(here.getLatitude(), here.getLongitude());
//                    mMap.addMarker(new MarkerOptions().position(temp)
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

//                } catch (IOException e) {
//                    e.printStackTrace();//CameraScreen.this.finish();
//                }
            }
        }
//    }
}
