package in.appyflow.geofire;

import android.util.Log;
import androidx.annotation.NonNull;

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

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** GeofirePlugin */
public class GeofirePlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {

    private GeoFire geoFire;
    private DatabaseReference databaseReference;
    private GeoQuery geoQuery;

    private static MethodChannel channel;
    private static EventChannel eventChannel;
    private EventChannel.EventSink events;

    private final HashMap<String, Object> hashMap = new HashMap<>();

    // --- INIT CHANNELS ---
    public static void pluginInit(BinaryMessenger messenger) {
        GeofirePlugin geofirePlugin = new GeofirePlugin();

        channel = new MethodChannel(messenger, "geofire");
        channel.setMethodCallHandler(geofirePlugin);

        eventChannel = new EventChannel(messenger, "geofireStream");
        eventChannel.setStreamHandler(geofirePlugin);
    }

    // --- HANDLE METHOD CALLS ---
    @Override
    public void onMethodCall(MethodCall call, final Result result) {
        Log.i("GeofirePlugin", "Method Called: " + call.method);

        switch (call.method) {
            case "GeoFire.start":
                databaseReference = FirebaseDatabase.getInstance().getReference(call.argument("path").toString());
                geoFire = new GeoFire(databaseReference);
                result.success(geoFire.getDatabaseReference() != null);
                break;

            case "setLocation":
                geoFire.setLocation(
                        call.argument("id").toString(),
                        new GeoLocation(
                                Double.parseDouble(call.argument("lat").toString()),
                                Double.parseDouble(call.argument("lng").toString())
                        ),
                        (key, error) -> result.success(error == null)
                );
                break;

            case "removeLocation":
                geoFire.removeLocation(
                        call.argument("id").toString(),
                        (key, error) -> result.success(error == null)
                );
                break;

            case "getLocation":
                geoFire.getLocation(call.argument("id").toString(), new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        HashMap<String, Object> map = new HashMap<>();
                        if (location != null) {
                            map.put("lat", location.latitude);
                            map.put("lng", location.longitude);
                            map.put("error", null);
                        } else {
                            map.put("error", "No location for key " + key);
                        }
                        result.success(map);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("error", "Error getting location: " + error.getMessage());
                        result.success(map);
                    }
                });
                break;

            case "queryAtLocation":
                geoFireArea(
                        Double.parseDouble(call.argument("lat").toString()),
                        Double.parseDouble(call.argument("lng").toString()),
                        result,
                        Double.parseDouble(call.argument("radius").toString())
                );
                break;

            case "stopListener":
                if (geoQuery != null) geoQuery.removeAllListeners();
                result.success(true);
                break;

            default:
                result.notImplemented();
                break;
        }
    }

    // --- GEO FIRE LISTENER ---
    private void geoFireArea(final double latitude, double longitude, final Result result, double radius) {
        try {
            final ArrayList<String> keys = new ArrayList<>();

            if (geoQuery != null) {
                geoQuery.setLocation(new GeoLocation(latitude, longitude), radius);
            } else {
                geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), radius);
            }

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    keys.add(key);
                    sendEvent("onKeyEntered", key, location);
                }

                @Override
                public void onKeyExited(String key) {
                    keys.remove(key);
                    sendEvent("onKeyExited", key, null);
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    sendEvent("onKeyMoved", key, location);
                }

                @Override
                public void onGeoQueryReady() {
                    hashMap.clear();
                    hashMap.put("callBack", "onGeoQueryReady");
                    hashMap.put("result", keys);
                    sendToStream(hashMap);
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    if (events != null) {
                        events.error("GeoQueryError", error.getMessage(), null);
                    } else if (geoQuery != null) {
                        geoQuery.removeAllListeners();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            result.error("Exception", e.getMessage(), null);
        }
    }

    private void sendEvent(String callback, String key, GeoLocation location) {
        if (events != null) {
            hashMap.clear();
            hashMap.put("callBack", callback);
            hashMap.put("key", key);
            if (location != null) {
                hashMap.put("latitude", location.latitude);
                hashMap.put("longitude", location.longitude);
            }
            sendToStream(hashMap);
        } else if (geoQuery != null) {
            geoQuery.removeAllListeners();
        }
    }

    private void sendToStream(HashMap<String, Object> data) {
        if (events != null) {
            events.success(data);
        }
    }

    // --- STREAM HANDLER ---
    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        this.events = eventSink;
    }

    @Override
    public void onCancel(Object o) {
        if (geoQuery != null) geoQuery.removeAllListeners();
        this.events = null;
    }

    // --- FLUTTER PLUGIN ATTACH/DETACH ---
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        pluginInit(binding.getBinaryMessenger());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        if (channel != null) channel.setMethodCallHandler(null);
        if (eventChannel != null) eventChannel.setStreamHandler(null);
    }
}
