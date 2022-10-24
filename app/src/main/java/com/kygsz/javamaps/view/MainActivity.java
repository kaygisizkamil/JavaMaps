package com.kygsz.javamaps.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.kygsz.javamaps.R;
import com.kygsz.javamaps.adapter.PlaceAdapter;
import com.kygsz.javamaps.databinding.ActivityMainBinding;
import com.kygsz.javamaps.model.Place;
import com.kygsz.javamaps.roomdb.PlaceDao;
import com.kygsz.javamaps.roomdb.PlaceDatabase;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
   private ActivityMainBinding binding;
   PlaceDatabase db;
   PlaceDao placeDao;
   private CompositeDisposable disposable=new CompositeDisposable();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        //search for the diff between View view=binding.getRoot()and hen setContentView(view)
        setContentView(binding.getRoot());
        //setContentView(R.layout.activity_main);
        db=Room.databaseBuilder(getApplicationContext(), PlaceDatabase.class,"Places").build();
        placeDao=db.placeDao();
        disposable.add(placeDao.getComplete().
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(this::handleResponse));
    }
    public void handleResponse(List<Place> places){
            binding.recycleViewM.setLayoutManager(new LinearLayoutManager(this));
            //to be able to use binding we also need to use the adapter that we binded textview to recycle_row
            PlaceAdapter placeAdapter=new PlaceAdapter(places);
            binding.recycleViewM.setAdapter(placeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        //after adding this inflater than add the layout to bind
        menuInflater.inflate(R.menu.travel_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.add_place){
            //to let the maps.activity know intend is to add new places to recycleview
            Intent intent=new Intent(getApplicationContext(), MapsActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);

        }
       return super.onOptionsItemSelected(item);
   }
   @Override
    protected void onDestroy(){
        super.onDestroy();
        disposable.dispose();
    }

}