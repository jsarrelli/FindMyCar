package com.example.julisarrelli.findmycar;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.support.v7.app.AlertDialog;


import android.os.Bundle;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.content.Context;
import android.location.Location;
import android.location.Criteria;

import com.google.android.gms.maps.model.CameraPosition;


import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, View.OnClickListener, DirectionCallback{

    private GoogleMap mMap;
    private Location location;
    private FloatingActionButton navegar;
    private LatLng origin = null;
    private LatLng destination = null;
    private double latitudMarker, longitudMarker;
    private Toolbar toolbar;
    private boolean refreshVisible = false;
    private boolean AddressVisible = false;
    private boolean clearVisible = false;
    private boolean AutomaticModeOn = false;
    private IntentFilter intentFilter;
    private BroadcastReceiver receiver;
    private double distance=0;
    private Location marker;
    private boolean RouteOn=false;
    private boolean MarkerOn=false;
    private boolean NavegarOn=false;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);


///esto es para los permission para el cornudo del marshamallow 6.0
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }


// Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.setRetainInstance(true);

        //activa GPS
        checkGPSenabled();

        //te dice que si activas el WIFI es mas precisa la ubicacion
        checkWIFIenabled();
        navegar = (FloatingActionButton) findViewById(R.id.walk);
        navegar.setOnClickListener(this);


        if(!NavegarOn)navegar.hide();



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();



        LocationManager locationmanager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location=locationmanager.getLastKnownLocation(BestProvider());

        android.location.LocationListener locationListener = new android.location.LocationListener() {
            public void onLocationChanged(Location location) {
                //Any method here
                try {

                    if (MarkerOn &&!RouteOn) {
                        distance = getLocation().distanceTo(marker);





                        if (distance >=3) {
                            navegar.show();
                            refreshVisible = false;
                            AddressVisible = true;
                            clearVisible = true;
                            NavegarOn=true;
                            invalidateOptionsMenu();

                        }

                    }
                }

                catch(Exception e){
                    e.printStackTrace();

                }


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {


            }


            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        locationmanager.requestLocationUpdates(BestProvider(),2000,0,locationListener);




    }



    private void checkWIFIenabled() {


        final WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.CheckWifi_title)
                    //.setIcon(R.drawable.ic_launcher)
                    .setMessage(R.string.CheckWifi_message)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            return;
                        }
                    })
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {


                            wifiManager.setWifiEnabled(true);


                        }
                    }).show();

        }

    }


    private void instanciarReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();


                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

//            cuando el dispositivo se conecta por alguna razon se conecta,desconecta y se vuelve a conectar
//            y eso hace que cuando se desconecte entre al SaveLocation(), por eso tenemos que anularlo

                    mMap.clear();
                    refreshVisible = false;
                    AddressVisible = false;
                    clearVisible = false;
                    navegar.hide();
                    invalidateOptionsMenu();


                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

                    saveLocation();

                }

            }
        };


        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(receiver, intentFilter);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_activity, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (refreshVisible) menu.findItem(R.id.action_refresh).setVisible(true);
        else menu.findItem(R.id.action_refresh).setVisible(false);

        if (AddressVisible) menu.findItem(R.id.action_adress).setVisible(true);
        else menu.findItem(R.id.action_adress).setVisible(false);

        if (clearVisible) menu.findItem(R.id.action_clear).setVisible(true);
        else menu.findItem(R.id.action_clear).setVisible(false);

        if (AutomaticModeOn)
            menu.findItem(R.id.action_AutomaticMode).setTitle(R.string.turnOffAutomaticMode);
        else menu.findItem(R.id.action_AutomaticMode).setTitle(R.string.turnOnAutomaticMode);

        return true;
    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;


        //activamos la location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        activarLocation();

        //se instancia la location
        location = getLocation();

        //centra la camara
        if (location != null) {
            centrarCamara(mMap, location.getLatitude(), location.getLongitude());
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                saveLocation();

            }
        });


        navegar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (destination != null) {

                    //dibuja la ruta hacia el destino
                    DrawRoute();


                    //esconde el boton de trazar la ruta y activa el boton de refresh ruta
                    navegar.hide();
                    refreshVisible = true;
                    invalidateOptionsMenu();


                    return;
                }


                Snackbar.make(navegar,R.string.NoDestination, Snackbar.LENGTH_SHORT).show();


            }
        });


    }





    public void saveLocation() {




        try {

            mMap.clear();
            refreshVisible = false;
            AddressVisible =true;
            clearVisible=true;
            navegar.hide();
            NavegarOn=false;
            invalidateOptionsMenu();
            marker=null;
            RouteOn=false;
            MarkerOn=true;

            location = getLocation();

            if (location != null) {





                Toast toast = Toast.makeText(getApplicationContext(), R.string.LocationSaved, Toast.LENGTH_SHORT);
                toast.show();
                latitudMarker = location.getLatitude();
                longitudMarker = location.getLongitude();

                marker = new Location(String.valueOf(R.string.Marker));
                marker.setLatitude(latitudMarker);
                marker.setLongitude(longitudMarker);


                mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title(String.valueOf(R.string.YourCarIsHere)).snippet(getAdress(location.getLatitude(), location.getLongitude())));

                destination = new LatLng(location.getLatitude(), location.getLongitude());



            }

            if (location == null) {

                Snackbar.make(navegar, R.string.NoGPS, Snackbar.LENGTH_SHORT).show();
            }

        }

        catch (Exception e)
        {
            Snackbar.make(navegar, R.string.CannotBeDone, Snackbar.LENGTH_SHORT).show();
        }
    }


    public void DrawRoute() {
        if (destination != null) {

            mMap.clear();

            mMap.addMarker(new MarkerOptions().position(new LatLng(marker.getLatitude(),marker.getLongitude())).title(String.valueOf(R.string.YourCarIsHere)).snippet(getAdress(marker.getLatitude(), marker.getLongitude())));

            requestDirection();

            RouteOn=true;
        }
    }


    public Location getLocation() {

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);




        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return null;
        }



        if (locationManager.getLastKnownLocation(BestProvider()) == null) {
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }


        return locationManager.getLastKnownLocation(BestProvider());
    }


    public String BestProvider()
    {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        return  provider;
    }


    public void centrarCamara(GoogleMap mMap, double latitud, double longitud) {

        //esta forma centra la camara pero sin animacion
//            double latitude = location.getLatitude();
//            double longitud = location.getLongitude();
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude,longitud)));


        //esta forma centra la camara con animacion

        if (location != null) {

            LatLng target = new LatLng(latitud, longitud);
            CameraPosition position = this.mMap.getCameraPosition();

            CameraPosition.Builder builder = new CameraPosition.Builder();
            builder.zoom(17);
            builder.target(target);

            this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
        }
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    try {
                        activarLocation();
                        location = getLocation();
                        centrarCamara(mMap,location.getLatitude(),location.getLongitude());
                    }
                    catch(Exception e) {
                        Snackbar.make(navegar,R.string.NoGPS, Snackbar.LENGTH_SHORT).show();

                    }

                } else {
                    Toast.makeText(this,R.string.AcceptGPS, Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    public void activarLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);
    }

    private void checkGPSenabled() {


        LocationManager mlocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
            showDialogGPS();
        }

    }

    private void showDialogGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.TurnOnGPS);
        builder.setMessage(R.string.AcceptGPS);
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton(R.string.Accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(
                        new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.Ignore, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public String  getAdress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }


        String direccion = addresses.get(0).getAddressLine(0);

        return direccion;

    }


    public void requestDirection() {


        location = getLocation();


        LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
        Toast toast = Toast.makeText(getApplicationContext(),R.string.CalculateRoute, Toast.LENGTH_SHORT);
        toast.show();
        GoogleDirection.withServerKey("AIzaSyDj6bsaW6DYgSYfertBx16fug2u0W6Pstc")
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.WALKING)
                .execute(this);
    }

    public void onDirectionSuccess(Direction direction, String rawBody) {


        if (direction.isOK()) {


            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.BLUE));


        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Snackbar.make(navegar, t.getMessage(), Snackbar.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View view) {


    }




    public void alertView(String message, int title) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(title)
                //.setIcon(R.drawable.ic_launcher)
                .setMessage(message)
//  .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//      public void onClick(DialogInterface dialoginterface, int i) {
//          dialoginterface.cancel();
//          }})
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                }).show();
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.CloseAppTitle)
                .setMessage(R.string.CloseAppMessage)
                .setPositiveButton(R.string.Accept, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton(R.string.Cancel, null)
                .show();
    }




    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.action_locate:

                try {
                    location = getLocation();
                    centrarCamara(mMap, location.getLatitude(), location.getLongitude());
                    return true;
                } catch (Exception e)
                {
                    Snackbar.make(navegar,R.string.NoGPS, Snackbar.LENGTH_SHORT).show();
                }

                return true;

            case R.id.action_refresh:
                DrawRoute();


                return true;

            case R.id.action_adress:
               // String direction=R.string.CarDirection;

               String direction= this.getString(R.string.CarDirection);
                alertView(direction+getAdress(latitudMarker, longitudMarker), R.string.Adress);
                location=getLocation();
                //centra la camara en el punto guardado
                centrarCamara(mMap,location.getLatitude(),location.getLongitude());


                return true;

            case R.id.action_clear:
                clear();
                return true;

            case R.id.retrofit:


                AlertDialog.Builder dialog2 = new AlertDialog.Builder(this);

                try {
                    dialog2.setTitle(R.string.RetrofitTitle)
                            //.setIcon(R.drawable.ic_launcher)
                            .setMessage(MovidaRetrofit())

                            .setPositiveButton(R.string.Accept, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {     }
                            }).show();
                }
                catch (Exception e)
                {
                    Toast.makeText(this,R.string.CannotBeDone, Toast.LENGTH_SHORT).show();

                }


                return true;

            case R.id.action_AutomaticMode:

                if(!AutomaticModeOn) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);

                    dialog.setTitle(R.string.AutomaticModeOnTitle)
                            //.setIcon(R.drawable.ic_launcher)
                            .setMessage(R.string.AutomaticModeOnMessage)
                            .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    return;
                                }
                            })
                            .setPositiveButton(R.string.Accept, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {


                                    if(receiver==null) instanciarReceiver();

                                    else registerReceiver(receiver,intentFilter);


                                    AutomaticModeOn=true;


                                }
                            }).show();

                }

                else
                {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);

                    dialog.setTitle(R.string.AutomaticModeOffTitle)
                            //.setIcon(R.drawable.ic_launcher)
                            .setMessage(R.string.AutomaticModeOffMessage)
                            .setNegativeButton(R.string.Accept, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    return;
                                }
                            })
                            .setPositiveButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {


                                    unregisterReceiver(receiver);
                                    AutomaticModeOn=false;


                                }
                            }).show();
                }


                return true;

            default:

                return super.onOptionsItemSelected(item);

        }
    }

    private void clear() {

        mMap.clear();
        refreshVisible = false;
        AddressVisible =false;
        clearVisible=false;
        navegar.hide();
        invalidateOptionsMenu();
        RouteOn=false;
        MarkerOn=false;
        NavegarOn=false;
    }


    @Override
    public void onStart() {
        super.onStart();


      MovidaRetrofit();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.julisarrelli.findmycar/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);



    }

    private String MovidaRetrofit() {

        final String[] cadena = new String[1];


        final String BASE_URL = "https://private-c9d064-prueba138.apiary-mock.com/";
        //final String BASE_URL = "https://private-e3aac-ipm1.apiary-mock.com/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        QuestionService service = retrofit.create(QuestionService.class);

        Call<List<Question>> questions = service.getQuestions();

        questions.enqueue(new Callback<List<Question>>() {


            @Override

            public void onResponse(Call<List<Question>> call, Response<List<Question>> response) {

                Log.v("Retrofit","aca entro");
                if (response.isSuccessful()){
                    /*
                        Acá es cuando las response Http  poseen un código 200 o 201.
                        Es cuando hubo éxito con la consulta.
                     */
                    Log.v("Retrofit",response.body().toString());
                    cadena[0] = response.body().toString();

//
                }else{
                    /*Aunque hubo respuesta del servidor, todavía puede haber error 404 o 500.
                        Es decir, error del cliente (se está queriendo acceder a un recurso inexistente)
                        o del servidor (errores con la base de datos, por ejemplo).
                     */
                }

            }

            @Override
            public void onFailure(Call<List<Question>> call, Throwable t) {

                Log.v("Retrofit","failure");

            }
        });



        return cadena[0];

    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.julisarrelli.findmycar/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


    @Override
    protected void onResume() {
        super.onResume();



    }


    public void OnDestroy()
    {
        super.onDestroy();
        unregisterReceiver(receiver);
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {



        outState.putBoolean("refreshVisible", refreshVisible);
        outState.putBoolean("AddressVisible", AddressVisible);
        outState.putBoolean("clearVisible", clearVisible);
        outState.putBoolean("AutomaticModeOn", AutomaticModeOn);
        outState.putBoolean("RouteOn", RouteOn);
        outState.putBoolean("MarkerOn", MarkerOn);
        outState.putBoolean("NavegarOn", NavegarOn);


        if(MarkerOn){
            outState.putDouble("LatitudMarker", marker.getLatitude());
            outState.putDouble("LongitudMarker", marker.getLongitude());
        }

        if(marker!=null)outState.putBoolean("MarkerOn",true);


        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {



        refreshVisible = savedInstanceState.getBoolean("refreshVisible");
        AddressVisible = savedInstanceState.getBoolean("AddressVisible");
        clearVisible = savedInstanceState.getBoolean("clearVisible");
        AutomaticModeOn = savedInstanceState.getBoolean("AutomaticModeOn");
        RouteOn = savedInstanceState.getBoolean("RouteOn");
        MarkerOn = savedInstanceState.getBoolean("MarkerOn");

        if(MarkerOn) {

            destination = new LatLng(savedInstanceState.getDouble("LatitudMarker"), savedInstanceState.getDouble("LongitudMarker"));

            marker = new Location("Marker");
            marker.setLongitude(savedInstanceState.getDouble("LongitudMarker"));
            marker.setLatitude(savedInstanceState.getDouble("LatitudMarker"));
        }

        if(NavegarOn) navegar.show();






        invalidateOptionsMenu();








        super.onRestoreInstanceState(savedInstanceState);




    }

}