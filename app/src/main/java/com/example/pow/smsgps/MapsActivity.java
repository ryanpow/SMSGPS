package com.example.pow.smsgps;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    public static GoogleMap mMap;
    private static final int LOCATION_REQUEST_CODE = 101;
    ArrayList<SelectUser> selectUsers;
    Cursor phones;
    ContentResolver resolver;
    SearchView search;
    SelectUserAdapter adapter;
    private Location mLastLocation;
    public static TextView txtSSID1, txtSSID2, txtSSID3, txtMAC1, txtMAC2, txtMAC3, txtlevel1, txtlevel2, txtlevel3;
    static String txtLocation, txtWifi;
    public LocationManager mLocationManager;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    Double latitude = IncomingSMSReceiver.latitude;
    Double longitude = IncomingSMSReceiver.longitude;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();

//    This method is used the moment the application starts.
//    It will display the mainpage and generate the map.
//    Request permission if needed.
//    It will also start scanning for information.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                    LOCATION_REQUEST_CODE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, 123);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 123);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, 123);
        }
        setContentView(R.layout.activity_maps);
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_REQUEST_CODE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        int LOCATION_REFRESH_TIME = 0;
        int LOCATION_REFRESH_DISTANCE = 0;

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
         mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
         receiverWifi = new WifiReceiver();
         registerReceiver(receiverWifi, new IntentFilter(
                 WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
         mainWifi.startScan();
     }
//    This method is called to destroy the map fragment, preventing overload of fragments
     public void onDestroyView() {
         FragmentManager fm = getSupportFragmentManager();
         Fragment fragment = (fm.findFragmentById(R.id.map));
         FragmentTransaction ft = fm.beginTransaction();
         ft.remove(fragment);
         ft.commit();
     }
//    This class is used when the application starts
//    It is for detecting WiFi information
     class WifiReceiver extends BroadcastReceiver {
//    This method runs when WiFi information gets updated
//    It will convert the WiFi info to string for storing of info
         public void onReceive(Context c, Intent intent) {
             sb = new StringBuilder();
             wifiList = mainWifi.getScanResults();
             int count=0;
                 for (ScanResult result : wifiList) {
                     if(count>2)
                     {
                         break;
                     }
                     sb.append((result.SSID).toString());
                     sb.append(",");
                     sb.append((result.BSSID).toString());
                     sb.append(",");
                     sb.append(String.valueOf(result.level));
                     sb.append("!");
                     count++;

             }
             System.out.println(sb);
             txtWifi="dHJjhnsjJ@"+sb;
             System.out.println(txtWifi);
         }
     }

//         This allows the GPS info to be received
    private final android.location.LocationListener mLocationListener = new android.location.LocationListener() {
        @Override
//         This method gets called when the GPS info changes
//         It converts the info into string coordinates for storing
         public void onLocationChanged(Location location) {
             System.out.println("onLocationChanged");

             mLastLocation = location;

             txtLocation = ("AQOZkasQSM"+":"+
                      String.valueOf(location.getLatitude()) + "," +
                      String.valueOf(location.getLongitude()));
         }

         @Override
         public void onStatusChanged(String provider, int status, Bundle extras) {
             System.out.println("onStatusChanged");
         }

         @Override
         public void onProviderEnabled(String provider) {
             System.out.println("onProviderEnabled");
         }

         @Override
         public void onProviderDisabled(String provider) {
             System.out.println("onProviderDisabled");
             //turns off gps services
         }
     };
//    This method runs when the user wants to view the WiFi page
//    It will generate the WiFi page and assign the stored external WiFi info to the text fields
//    If there is no SSID from the stored WiFi external info, "No SSID" will be displayed
     public void wifi_list(){
         setContentView(R.layout.wifi_list);
         txtSSID1 = (TextView) findViewById(R.id.txtSSID1);
         txtSSID2 = (TextView) findViewById(R.id.txtSSID2);
         txtSSID3 = (TextView) findViewById(R.id.txtSSID3);
         txtMAC1 = (TextView) findViewById(R.id.txtMAC1);
         txtMAC2 = (TextView) findViewById(R.id.txtMAC2);
         txtMAC3 = (TextView) findViewById(R.id.txtMAC3);
         txtlevel1 = (TextView) findViewById(R.id.txtlevel1);
         txtlevel2 = (TextView) findViewById(R.id.txtlevel2);
         txtlevel3 = (TextView) findViewById(R.id.txtlevel3);
         txtSSID1.setText(IncomingSMSReceiver.SSID1);
         txtSSID2.setText(IncomingSMSReceiver.SSID2);
         txtSSID3.setText(IncomingSMSReceiver.SSID3);
         txtMAC1.setText(IncomingSMSReceiver.MAC1);
         txtMAC2.setText(IncomingSMSReceiver.MAC2);
         txtMAC3.setText(IncomingSMSReceiver.MAC3);
         txtlevel1.setText(IncomingSMSReceiver.level1);
         txtlevel2.setText(IncomingSMSReceiver.level2);
         txtlevel3.setText(IncomingSMSReceiver.level3);
         System.out.println(IncomingSMSReceiver.MAC3);
         if (txtSSID1.getText().equals("")){
             txtSSID1.setText("(No SSID)");
         }
         if (txtSSID2.getText().equals("")){
             txtSSID2.setText("(No SSID)");
         }
         if (txtSSID3.getText().equals("")){
             txtSSID3.setText("(No SSID)");
         }
     }
//    This method is called when the user wants to view their contacts list for finding a contact
    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
// Android version is lesser than 6.0 or the permission is already granted.
//   Will call the method contactpopup()
            contactpopup();
        }
    }
//  This method will generate and display the contact list
//  It will generate a select button to allow the user to choose which contact they want to find
    public void contactpopup() {
         setContentView(R.layout.contactpopup);
         ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.READ_CONTACTS);
         selectUsers = new ArrayList<SelectUser>();
         resolver = this.getContentResolver();
         phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
         LoadContact loadContact = new LoadContact();
         loadContact.execute();
         (findViewById(R.id.btnSelect)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                 intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                 startActivityForResult(intent, 1);
             }
         });

     }


     // Load data on background
     class LoadContact extends AsyncTask<Void, Void, Void> {
         @Override
         protected void onPreExecute() {
             super.onPreExecute();

         }

         @Override
         protected Void doInBackground(Void... voids) {
             // Get Contact list from Phone

             if (phones != null) {
                 Log.e("count", "" + phones.getCount());
                 if (phones.getCount() == 0) {
                     Toast.makeText(MapsActivity.this, "No contacts in your contact list.", Toast.LENGTH_LONG).show();
                 }

                 while (phones.moveToNext()) {
                     String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                     String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                     String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                     SelectUser selectUser = new SelectUser();
                     selectUser.setName(name);
                     selectUser.setPhone(phoneNumber);
                     selectUser.setCheckedBox(false);
                     selectUsers.add(selectUser);
                 }
             } else {
                 Log.e("Cursor close 1", "----------------");
             }
             return null;
         }

         @Override
         protected void onPostExecute(Void aVoid) {
             super.onPostExecute(aVoid);
             adapter = new SelectUserAdapter(selectUsers, MapsActivity.this);

         }
     }
//  This method is called when a contact is selected in the contact list
//  It will send a SMS with a specific "request" code to the contact
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (data != null) {
             Uri uri = data.getData();
             String number = "";
             if (uri != null) {
                 Cursor c = null;
                 try {
                     c = getContentResolver().query(uri, new String[]{
                                     ContactsContract.CommonDataKinds.Phone.NUMBER,
                                     ContactsContract.CommonDataKinds.Phone.TYPE},
                             null, null, null);

                     if (c != null && c.moveToFirst()) {
                         number = c.getString(0);
                         int type = c.getInt(1);
                         showSelectedNumber(type, number);
                     }

                     Log.i("Send SMS", "");
                     String message = "JSao21zjM";
                     TextView textview = (TextView) findViewById(R.id.txtNumber);
                     textview.setText(number);
                     try {
                         SmsManager smsManager = SmsManager.getDefault();
                         smsManager.sendTextMessage(number, null, message, null, null);
                         Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
                         back();
                     } catch (Exception e) {
                         Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                         e.printStackTrace();
                     }
                 } finally {
                     if (c != null) {
                         c.close();
                     }
                 }
             }

         }

     }

     public void showSelectedNumber(int type, String number) {
         Toast.makeText(this, type + ": " + number, Toast.LENGTH_LONG).show();
     }

//    This method is called the moment the SMS is sent by the contact list successfully.
//    It will bring the user back to the main page
     public void back(){
         setContentView(R.layout.activity_maps);
         SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                 .findFragmentById(R.id.map);
         mapFragment.getMapAsync(this);
     }
//    This method is called whenever a button is pressed to change layouts only
//    It assigns some of the buttons to their appropriate methods
//    It will generate the map and destroy it if needed
     public void onSwitch(View view) {
         if (view.getId() == R.id.btnBack) {
             setContentView(R.layout.activity_maps);
             SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                     .findFragmentById(R.id.map);
             mapFragment.getMapAsync(this);
         }
         if (view.getId() == R.id.btnBack2) {
             setContentView(R.layout.activity_maps);
             SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                     .findFragmentById(R.id.map);
             mapFragment.getMapAsync(this);
         }
         if (view.getId() == R.id.btnContact) {
             onDestroyView();
             showContacts();
             Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
             intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
             startActivityForResult(intent, 1);
         }
         if (view.getId() == R.id.btnWifi) {
             setContentView(R.layout.wifi_list);
             onDestroyView();
             wifi_list();
         }

     }
//   This method is for the zoom buttons
//   It allows the map to zoom in and zoom out with the buttons
     public void onZoom(View view) {
         if (view.getId() == R.id.btnzoomin) {
             mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
         }
         if (view.getId() == R.id.btnzoomout) {
             mMap.animateCamera(CameraUpdateFactory.zoomTo(8), 2000, null);
         }
     }

//   This method is for the search address bar
//   It connects to the google map database with geocoder
     public void onSearch(View view) {
         EditText location_tf = (EditText) findViewById(R.id.txtAddress);
         String location = location_tf.getText().toString();
         List<Address> addressList = null;
         if (location != null || location.equals("")) {
             Geocoder geocoder = new Geocoder(this);
             try {
                 addressList = geocoder.getFromLocationName(location, 1);
             } catch (IOException e) {
                 e.printStackTrace();
             }
             Address address = addressList.get(0);
             LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
             mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
             mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
         }
     }
//    This method is for requesting permission for location for Android Version 6.0
     protected void requestPermission(String permissionType, int
             requestCode) {
         int permission = ContextCompat.checkSelfPermission(this,
                 permissionType);
         if (permission != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(this,
                     new String[]{permissionType}, requestCode
             );
         }
     }
//    This method is for requesting permission for location for Android Version 6.0
     @Override
     public void onRequestPermissionsResult(int requestCode,
                                            String permissions[], int[]
                                                    grantResults) {
         switch (requestCode) {
             case LOCATION_REQUEST_CODE: {
                 if (grantResults.length == 0
                         || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                 {
                     Toast.makeText(this, "Unable to show location - permission required", Toast.LENGTH_LONG).show();
                 }
                 return;
             }
         }
         if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
             if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 // Permission is granted
                 showContacts();
             } else {
                 Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
             }
         }
     }
//   This method always runs when the map is generated
//   It makes the map default location to be Temasek Polytechnic
//   If there are friend's external GPS info stored, a marker will be placed and the map camera will move on the friend's location instead
     @Override
     public void onMapReady(GoogleMap googleMap) {
         mMap = googleMap;
         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             return;
         }
         mMap.setMyLocationEnabled(true);
        if((IncomingSMSReceiver.latitude==null)&&(IncomingSMSReceiver.longitude==null)) {
            LatLng coordinate = new LatLng(1.3468, 103.9326);
            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            mMap.animateCamera(location);
        }
         else
        {
            LatLng latLng = new LatLng(IncomingSMSReceiver.latitude,IncomingSMSReceiver.longitude);
            MapsActivity.mMap.addMarker(new MarkerOptions().position(latLng).title("Contact"));
            MapsActivity.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
     }
     @Override
     public void onConnected(Bundle bundle) {

     }

     @Override
     public void onConnectionSuspended(int i) {

     }

     @Override
     public void onConnectionFailed(ConnectionResult connectionResult) {

     }
 }
