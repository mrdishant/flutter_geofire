package in.appyflow.geofire;

import android.util.Log;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;


import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * GeofirePlugin
 */
public class GeofirePlugin implements MethodCallHandler, EventChannel.StreamHandler {

    GeoFire geoFire;
    DatabaseReference databaseReference;

    private EventChannel.EventSink events;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {

        GeofirePlugin geofirePlugin = new GeofirePlugin();

        final MethodChannel channel = new MethodChannel(registrar.messenger(), "geofire");
        channel.setMethodCallHandler(geofirePlugin);

        EventChannel eventChannel = new EventChannel(registrar.messenger(), "geofireStream");
        eventChannel.setStreamHandler(geofirePlugin);

    }

    @Override
    public void onMethodCall(MethodCall call, final Result result) {

        Log.i("TAG",call.method.toString());

        if (call.method.equals("GeoFire.start")) {

            databaseReference = FirebaseDatabase.getInstance().getReference(call.argument("path").toString());
            geoFire = new GeoFire(databaseReference);

            if (geoFire.getDatabaseReference() != null) {
                result.success(true);
            } else
                result.success(false);
        } else if (call.method.equals("setLocation")) {

            geoFire.setLocation(call.argument("id").toString(), new GeoLocation(Double.parseDouble(call.argument("lat").toString()), Double.parseDouble(call.argument("lng").toString())), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                    if (error != null) {
                        result.success(false);
                    } else {
                        result.success(true);
                    }

                }
            });


        } else if (call.method.equals("removeLocation")) {

            geoFire.removeLocation(call.argument("id").toString(), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                    if (error != null) {
                        result.success(false);
                    } else {
                        result.success(true);
                    }

                }
            });


        } else if (call.method.equals("getLocation")) {

            geoFire.getLocation(call.argument("id").toString(), new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    HashMap<String, Object> map = new HashMap<>();
                    if (location != null) {


                        map.put("lat", location.latitude);
                        map.put("lng", location.longitude);
                        map.put("error", null);

                    } else {


                        map.put("error", String.format("There is no location for key %s in GeoFire", key));

                    }

                    result.success(map);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("error", "There was an error getting the GeoFire location: " + databaseError);

                    result.success(map);
                }
            });


        } else if (call.method.equals("queryAtLocation")) {
            geoFireArea(Double.parseDouble(call.argument("lat").toString()), Double.parseDouble(call.argument("lng").toString()), result, Double.parseDouble(call.argument("radius").toString()));
        }
        else if (call.method.equals("stopListener")) {

            if(geoQuery!=null){
                geoQuery.removeAllListeners();
            }

            result.success(true);
        }
        else {
            result.notImplemented();
        }
    }

    GeoQuery geoQuery;

    HashMap<String, Object> hashMap = new HashMap<>();


    private void geoFireArea(final double latitude, double longitude, final Result result, double radius) {
        try {

            final ArrayList<String> arrayListKeys = new ArrayList<>();
            geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), radius);
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {

                    if (events != null) {
                        hashMap.clear();
                        hashMap.put("callBack", "onKeyEntered");
                        hashMap.put("key",key);
                        hashMap.put("latitude",location.latitude);
                        hashMap.put("longitude",location.longitude);
                        events.success(hashMap);
                    } else {
                        geoQuery.removeAllListeners();
                    }

                    arrayListKeys.add(key);

                }

                @Override
                public void onKeyExited(String key) {
                    arrayListKeys.remove(key);

                    if (events != null) {

                        hashMap.clear();
                        hashMap.put("callBack", "onKeyExited");
                        hashMap.put("key",key);
                        events.success(hashMap);
                    } else {
                        geoQuery.removeAllListeners();
                    }

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                    if (events != null) {
                        hashMap.clear();

                        hashMap.put("callBack", "onKeyMoved");
                        hashMap.put("key",key);
                        hashMap.put("latitude",location.latitude);
                        hashMap.put("longitude",location.longitude);

                        events.success(hashMap);
                    } else {
                        geoQuery.removeAllListeners();
                    }

                }

                @Override
                public void onGeoQueryReady() {
//                    geoQuery.removeAllListeners();
//                    result.success(arrayListKeys);

                    if (events != null) {
                        hashMap.clear();

                        hashMap.put("callBack", "onGeoQueryReady");
                        hashMap.put("result",arrayListKeys);

                        events.success(hashMap);

                    } else {
                        geoQuery.removeAllListeners();
                    }

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                    if (events != null) {

                        events.error("Error ", "GeoQueryError", error);
                    } else {
                        geoQuery.removeAllListeners();
                    }


                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            result.error("Error ", "General Error", e);
        }
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        events = eventSink;
    }

    @Override
    public void onCancel(Object o) {

        geoQuery.removeAllListeners();
        events = null;

    }
}