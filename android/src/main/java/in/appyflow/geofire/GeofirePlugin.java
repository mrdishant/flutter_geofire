package in.appyflow.geofire;

import android.webkit.GeolocationPermissions;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;

/**
 * GeofirePlugin
 */
public class GeofirePlugin implements MethodCallHandler, EventChannel.StreamHandler {

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "geofire");
        final EventChannel stream_onEvent = new EventChannel( registrar.messenger(),"geofirestream");
        GeofirePlugin instance = new GeofirePlugin();
        channel.setMethodCallHandler(instance);
        stream_onEvent.setStreamHandler(instance);
    }

    private GeoFire geoFire;
    private DatabaseReference databaseReference;
    private GeoQuery circleQuery;
    private Boolean listening = false;
    private EventChannel.EventSink eventSink;


    @Override
    public void onMethodCall(MethodCall call, final Result result) {

        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        }

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


        }else if (call.method.equals("removeLocation")) {

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


        }else if (call.method.equals("getLocation")) {
            geoFire.getLocation(call.argument("id").toString(),new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    HashMap<String ,Object> map=new HashMap<>();
                    if (location != null) {

                        map.put("lat",location.latitude);
                        map.put("lng",location.longitude);
                        map.put("error",null);

                    } else {


                        map.put("error",String.format("There is no location for key %s in GeoFire", key));

                    }

                    result.success(map);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    HashMap<String ,Object> map=new HashMap<>();
                    map.put("error","There was an error getting the GeoFire location: " + databaseError);

                    result.success(map);
                }
            });


        } else if (call.method.equals("queryAtLocation")) {
            if (circleQuery == null) {
                Double lat = Double.parseDouble(call.argument("lat").toString());
                Double lng = Double.parseDouble(call.argument("lng").toString());
                Double radius = Double.parseDouble(call.argument("radius").toString());
                GeoLocation location = new GeoLocation(lat, lng);
                this.circleQuery = geoFire.queryAtLocation(location, radius);
//                instance.circleQuery = this.circleQuery;
            }//otherwise it is already setup!
            result.success(true);
        } else if (call.method.equals("updateLocation")) {
            Double lat = Double.parseDouble(call.argument("lat").toString());
            Double lng = Double.parseDouble(call.argument("lng").toString());
            Double radius = Double.parseDouble(call.argument("radius").toString());
            GeoLocation location = new GeoLocation(lat, lng);
            this.circleQuery.setCenter(location);
            this.circleQuery.setRadius(radius);
//            instance.circleQuery.setCenter(location);
//            instance.circleQuery.setRadius(radius);
            result.success(true);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onListen(Object o, final EventChannel.EventSink events) {
        this.eventSink = events;
        if (!listening && circleQuery != null){
            listening = true;
            try {
                this.circleQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        Map<String,Object> data = new HashMap<String, Object>();
                        data.put("key", key);
                        data.put("lat", location.latitude);
                        data.put("long", location.longitude);
                        data.put("event", "ENTERED");

                        byte[] jsonData = new JSONObject(data).toString().getBytes();
                        eventSink.success(jsonData);
                    }

                    @Override
                    public void onKeyExited(String key) {
                        Map<String,Object> data = new HashMap<String, Object>();
                        data.put("key", key);
                        data.put("event", "EXITED");

                        byte[] jsonData = new JSONObject(data).toString().getBytes();
                        eventSink.success(jsonData);
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                    }

                    @Override
                    public void onGeoQueryReady() {
                        
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                        Map<String,Object> data = new HashMap<String, Object>();
                        data.put("error", error);
                        data.put("event", "ERROR");
                        byte[] jsonData = new JSONObject(data).toString().getBytes();
                        eventSink.success(jsonData);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Map<String,Object> data = new HashMap<String, Object>();
                data.put("error", e);
                data.put("event", "ERROR");
                String jsonData = new JSONObject(data).toString();
                System.out.printf(jsonData);
                eventSink.success(jsonData);
            }
        }
    }

    @Override
    public void onCancel(Object o) {
        this.eventSink = null;
        this.circleQuery.removeAllListeners();
        listening = false;
    }
}