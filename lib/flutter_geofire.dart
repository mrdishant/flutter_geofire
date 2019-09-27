import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';


class Geofire {
  factory Geofire() {
    if (_instance == null) {
      final MethodChannel methodChannel =
          const MethodChannel('geofire');
      final EventChannel eventChannel =
          const EventChannel('geofirestream');
      _instance = Geofire.private(methodChannel, eventChannel);
    }
    return _instance;
  }
  
  Geofire.private(this._methodChannel, this._eventChannel);

  static Geofire _instance;

  final MethodChannel _methodChannel;
  final EventChannel _eventChannel;
  StreamController<dynamic> _onKeyEntered = new StreamController.broadcast();
  StreamController<dynamic> _onKeyExited = new StreamController.broadcast(); 
  StreamController<dynamic> _onObserveReady = new StreamController.broadcast(); 

  Future<bool> initialize(String path) async {
    final dynamic r = await _methodChannel.invokeMethod('GeoFire.start', <String, dynamic>{"path": path});
    return r ?? false;
  }

  Future<bool> setLocation(
      String id, double latitude, double longitude) async {
    final bool isSet = await _methodChannel.invokeMethod('setLocation',
        <String, dynamic>{"id": id, "lat": latitude, "lng": longitude});
    return isSet;
  }

  Future<bool> removeLocation(String id) async {
    final bool isSet = await _methodChannel
        .invokeMethod('removeLocation', <String, dynamic>{"id": id});
    return isSet;
  }

  Future<Map<String, dynamic>> getLocation(String id) async {
    final Map<dynamic, dynamic> response =
        await _methodChannel.invokeMethod('getLocation', <String, dynamic>{"id": id});

    Map<String, dynamic> location = new Map();

    response.forEach((key, value) {
      location[key] = value;
    });
    return location;
  }

  Future<String> queryAtLocation(
    double lat, double lng, double radius) async {
    final dynamic response = await _methodChannel.invokeMethod('queryAtLocation', {"lat": lat, "lng": lng, "radius": radius});
    _eventChannel.receiveBroadcastStream().listen((dynamic event) => _parseStream(event));
    String r = response.toString();
    return r;
  }

  Future<String> updateLocation(
    double lat, double lng, double radius) async {
    final dynamic response = await _methodChannel.invokeMethod('updateLocation', {"lat": lat, "lng": lng, "radius": radius});
    String r = response.toString();
    return r;
  }

  Stream<dynamic> get onKeyEntered {
    return _onKeyEntered.stream;
  }

  Stream<dynamic> get onKeyExited {
    return _onKeyExited.stream;
  }

  Stream<dynamic> get onObserveReady {
    return _onObserveReady.stream;
  }

  dynamic _parseStream(Object event) {
    var jsonData = new String.fromCharCodes(event);
    var data = json.decode(jsonData);
    if (data["event"] == "ENTERED" ){
      _onKeyEntered.add(data);
    }else if (data["event"] == "EXITED"){
      _onKeyExited.add(data);
    }else if (data["event"] == "GEOQUERY_READY"){
      _onObserveReady.add(true);
    }
  }
}
