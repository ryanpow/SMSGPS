package com.example.pow.smsgps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class IncomingSMSReceiver extends BroadcastReceiver{
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    String txtLocation= MapsActivity.txtLocation;
    String txtWifi= MapsActivity.txtWifi;
    static Double latitude,longitude;
    public static String wifiresult,SSID1,SSID2,SSID3,MAC1,MAC2,MAC3,level1,level2,level3;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus.length == 0) {
                    return;
                }
                SmsMessage[] messages = new SmsMessage[pdus.length];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    sb.append(messages[i].getMessageBody());
                }
                String sender = messages[0].getOriginatingAddress();
                String message = sb.toString();

                if (message.startsWith("JSao21zjM"))
                {

                    Toast.makeText(context, "Sending Location", Toast.LENGTH_SHORT).show();

                    this.abortBroadcast();
                    try {
                        String txtSMS = txtWifi;
                        sendSMS(sender, txtSMS);
                    }catch(Exception e) {
                        String txtSMS = "Wifi Unavaliable";
                        sendSMS(sender, txtSMS);
                    }

                    try {
                        String txtSMS2 = txtLocation;
                        sendSMS(sender, txtSMS2);
                    }catch(Exception e) {
                        String txtSMS2 = "Location Unavaliable";
                        sendSMS(sender, txtSMS2);
                    }
                }
                if (message.startsWith("AQOZkasQSM"))
                {
                    Toast.makeText(context, "Received Location", Toast.LENGTH_SHORT).show();
                    this.abortBroadcast();
                    String[] separated = message.split(":");
                    String[] latlong = separated[1].split(",");
                    latitude = Double.parseDouble(latlong[0]);
                    longitude = Double.parseDouble(latlong[1]);
                    LatLng latLng = new LatLng(latitude,longitude);
                    MapsActivity.mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
                    MapsActivity.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    System.out.println(latitude);
                    System.out.println(longitude);
                }
                if (message.startsWith("dHJjhnsjJ"))
                {
                    Toast.makeText(context, "Received Wifi", Toast.LENGTH_SHORT).show();
                    this.abortBroadcast();
                    String[] separated = message.split("@");
                    wifiresult=separated[1];
                    MapsActivity.mainLabel.setText(wifiresult);
                    String[] wifiinfo = wifiresult.split("!");
                    String wifi1=wifiinfo[0];
                    String wifi2=wifiinfo[1];
                    String wifi3=wifiinfo[2];
                    String[] wifiinfo1 = wifi1.split(",");
                    SSID1=wifiinfo1[0];
                    MAC1=wifiinfo1[1];
                    level1=wifiinfo1[2];
                    String[] wifiinfo2 = wifi2.split(",");
                    SSID2=wifiinfo2[0];
                    MAC2=wifiinfo2[1];
                    level2=wifiinfo2[2];
                    String[] wifiinfo3 = wifi3.split(",");
                    SSID3=wifiinfo3[0];
                    MAC3=wifiinfo3[1];
                    level3=wifiinfo3[2];
                    System.out.println(wifi3);
                    System.out.println(wifiinfo1[0]);
                    System.out.println(wifiinfo3[1]);
                    System.out.println(MAC3);
                }
            }
        }
    }
    // sends the SMS
    private void sendSMS(final String phoneNumber, String message) {


        if(!phoneNumber.isEmpty() && !message.isEmpty()) {

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
        }
    }
}