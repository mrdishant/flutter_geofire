import 'dart:async';

import 'package:flutter/services.dart';

class Geofire {
  static const MethodChannel _channel = const MethodChannel('geofire');

  static const EventChannel _stream = const EventChannel('geofireStream');

  static const onKeyEntered = "onKeyEntered";
  static const onGeoQueryReady = "onGeoQueryReady";
  static const onKeyMoved = "onKeyMoved";
  static const onKeyExited = "onKeyExited";

  static Stream<dynamic>? _queryAtLocation;

  static Future<bool> initialize(String path) async {
    final dynamic r = await _channel
        .invokeMethod('GeoFire.start', <String, dynamic>{"path": path});
    return r ?? false;
  }

  static Future<bool?> setLocation(
      String id, double latitude, double longitude) async {
    final bool? isSet = await _channel.invokeMethod('setLocation',
        <String, dynamic>{"id": id, "lat": latitude, "lng": longitude});
    return isSet;
  }

  static Future<bool?> removeLocation(String id) async {
    final bool? isSet = await _channel
        .invokeMethod('removeLocation', <String, dynamic>{"id": id});
    return isSet;
  }

  static Future<bool?> stopListener() async {
    final bool? isSet =
        await _channel.invokeMethod('stopListener', <String, dynamic>{});
    return isSet;
  }

  static Future<Map<String, dynamic>> getLocation(String id) async {
    final Map<dynamic, dynamic> response = await (_channel
        .invokeMethod('getLocation', <String, dynamic>{"id": id}));

    Map<String, dynamic> location = new Map();

    response.forEach((key, value) {
      location[key] = value;
    });

    // print(location);

    return location;
  }

  static Stream<dynamic>? queryAtLocation(
      double lat, double lng, double radius) {
    _channel.invokeMethod('queryAtLocation',
        {"lat": lat, "lng": lng, "radius": radius}).then((result) {
      // print("result" + result);
    }).catchError((error) {
      // print("Error " + error);
    });

    if (_queryAtLocation == null) {
      _queryAtLocation = _stream.receiveBroadcastStream();
    }
    return _queryAtLocation;
  }
}
