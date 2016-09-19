package com.example.julisarrelli.findmycar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;


import android.os.Bundle;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.content.Context;
import android.location.Location;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;

import com.google.android.gms.common.GooglePlayServicesUtil;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * This shows how to create a simple activity with a map and a marker on the map.
 */
public class MainActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, DirectionCallback {

    private GoogleMap mMap;
    private Location location;
    private TextView direccion1=null;
    private TextView direccion2=null;
    private FloatingActionButton navegar;
    private LatLng origin=null;
    private LatLng destination=null;
    private double latitudMarker,longitudMarker;









    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

// Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        checkGPSenabled();
        navegar = (FloatingActionButton) findViewById(R.id.walk);
        navegar.setOnClickListener(this);





    }




    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

      //  map.addMarker(new MarkerOptions().position(new LatLng(-34.59085915929207, -58.503838777542114)).title("Destino"));





        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing pe rmissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //activamos la location
        mMap.setMyLocationEnabled(true);

        //se instancia la location
        location=getLocation();

        //centra la camara
        if (location != null) {centrarCamara(mMap);}




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                location=getLocation();

                if (location != null){

                    mMap.clear();

                    latitudMarker=location.getLatitude();
                    longitudMarker= location.getLongitude();


                    mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Aca dejaste el auto!").snippet(getAdress(location.getLatitude(),location.getLongitude())));

                    setTexto();

                    destination=new LatLng(location.getLatitude(),location.getLongitude());


                }

                if(location==null) {

                    Snackbar.make(navegar, "No anda el GPS del pelotudo de cesar", Snackbar.LENGTH_SHORT).show();
                }


            }
        });



        navegar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(destination!=null) {

                    mMap.clear();

                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitudMarker, longitudMarker)).title("Aca dejaste el auto!").snippet(getAdress(location.getLatitude(),location.getLongitude())));

                    requestDirection();

                    return;
                }


                    Snackbar.make(navegar, "No se encuentra un destino", Snackbar.LENGTH_SHORT).show();



            }
        });


////        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("You are here!").snippet("Consider yourself located"));
////
////
////
////        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));


    }

    public Location getLocation() {

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);


        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return null;
        }


        return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
       // return locationManager.getLastKnownLocation(provider);
    }


    public void centrarCamara(GoogleMap mMap)
    {

        //esta forma centra la camara pero sin animacion
//            double latitude = location.getLatitude();
//            double longitud = location.getLongitude();
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude,longitud)));


        //esta forma centra la camara con animacion




        location=getLocation();



        if(location!=null){

        LatLng target = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition position = this.mMap.getCameraPosition();

        CameraPosition.Builder builder = new CameraPosition.Builder();
        builder.zoom(17);
        builder.target(target);

        this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
    }
    }



    private void checkGPSenabled() {

        LocationManager mlocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(!enabled) {
            showDialogGPS();
        }

    }

    private void showDialogGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Activar GPS");
        builder.setMessage("Para que la app funcione correctamente, debes activar el GPS");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Activar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(
                        new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton("Ignorar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public String getAdress(double latitude,double longitude)
    {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        List<Address> addresses  = null;
        try {
            addresses = geocoder.getFromLocation(latitude,longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }


        String direccion = addresses.get(0).getAddressLine(0);

    return direccion;

    }


    public void requestDirection() {


        location=getLocation();


        LatLng origin= new LatLng(location.getLatitude(),location.getLongitude());
        Snackbar.make(navegar, "Solicitando Ruta...", Snackbar.LENGTH_SHORT).show();
        GoogleDirection.withServerKey("AIzaSyDj6bsaW6DYgSYfertBx16fug2u0W6Pstc")
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.WALKING)
                .execute(this);
    }

    public void onDirectionSuccess(Direction direction, String rawBody) {


        if (direction.isOK()) {


            ArrayList<LatLng>  directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.GREEN));




        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Snackbar.make(navegar, t.getMessage(), Snackbar.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View view) {



    }


    public void setTexto()
    {
        direccion2=(TextView)findViewById(R.id.direccion2);
        direccion2.setText(getAdress(location.getLatitude(),location.getLongitude()));

        direccion1=(TextView)findViewById(R.id.direccion1);
        direccion1.setText(R.string.direccion1);
    }
}