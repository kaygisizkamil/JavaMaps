package com.kygsz.javamaps.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.kygsz.javamaps.R;
import com.kygsz.javamaps.databinding.ActivityMapsBinding;
import com.kygsz.javamaps.model.Place;
import com.kygsz.javamaps.roomdb.PlaceDao;
import com.kygsz.javamaps.roomdb.PlaceDatabase;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
   PlaceDatabase db;
   PlaceDao placeDao;
   double selectedLatitute;
   double selectedLongitude;
   boolean trackBoolean;
   Place selectedPlace;
    SharedPreferences sharedPreferences;
    private CompositeDisposable disposable=new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
      selectedLatitute=0.0;
      selectedLongitude=0.0;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        sharedPreferences=MapsActivity.this.getSharedPreferences("com.kygsz.javamaps.view",MODE_PRIVATE);
        binding.saveButton.setEnabled(false);
       // binding.deleteButton.setEnabled(false);
        registerLauncher();
        //we have to remain the using the names"Places" to read and write to database but it is wrong to do it onCreate
        //it might cause the killing of main thread
        db= Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places").
                allowMainThreadQueries()
                .build();
        placeDao=db.placeDao();
    }

    /**
     * Manipulates the *map once available.
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
        mMap.setOnMapLongClickListener(this);
        Intent intent=getIntent();
        String newOrOldInfo=intent.getStringExtra("info");
        if(newOrOldInfo.equals("new")){
            //it means data is coming via menu button
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE);

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    trackBoolean = sharedPreferences.getBoolean("trackBoolean",false);

                    if(!trackBoolean) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply();
                    }
                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //request permission
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(binding.getRoot(),"Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();

                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

                }

            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                }

                mMap.setMyLocationEnabled(true);

            }
        }
        //else if(newOrOldInfo=="old")
        else if(newOrOldInfo.equals("new")){
                mMap.clear();
                selectedPlace= (Place) intent.getSerializableExtra("place");
                LatLng latLng=new LatLng(selectedPlace.latitude,selectedPlace.longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                binding.placeNameText.setText(selectedPlace.name);
                binding.saveButton.setVisibility(View.GONE);
                binding.deleteButton.setVisibility(View.VISIBLE);

        }








    }


    private void registerLauncher() {
        permissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if(result) {
                            //permission granted
                            if (ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (lastLocation != null) {
                                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                                }
                            }

                        } else {
                            //permission denied
                            Toast.makeText(MapsActivity.this,"Permisson needed!",Toast.LENGTH_LONG).show();
                        }
                    }

                });
    }


    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        //her degisimde marker koymak icin
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng));
            selectedLatitute=latLng.latitude;
            selectedLongitude=latLng.longitude;
            //dont let the user save till he entered a location using longclick now user picked a loction to save
            binding.saveButton.setEnabled(true);
            binding.deleteButton.setEnabled(true);
    }
    public void save(View view){
        Place place=new Place(binding.placeNameText.getText().toString(),selectedLatitute,selectedLongitude);
        disposable.add(placeDao.insert(place).
                subscribeOn(Schedulers.io()).//Do it in io thread (insert operatino)
                observeOn(AndroidSchedulers.mainThread()).//observe in main thread
                subscribe(MapsActivity.this::handleResponse));//after operation completed
        //subscribe can be used without method in it

    }
    public void delete(View view){
       if(selectedPlace!=null){
         disposable.add(placeDao.delete(selectedPlace).
                 subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread()).
                 subscribe(MapsActivity.this::handleResponse));
       }

    }
    private void handleResponse(){
        Intent intent=new Intent(MapsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //it will close all the activity basically it'll clear them all
        startActivity(intent);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        disposable.clear();
    }
}

//main activity icine recycler view ekle
//****olusturdugun main activity'yi sil adam gibi ayarlaradn default activity olarak ayarla

//binding olustur
//binding xml duzenle
//fragment'i constraint layout tarafindan wrap'le
//mapactivity icine plain text save and delete button ekle
//res icine menu olustur item ekle bindingi gercekle