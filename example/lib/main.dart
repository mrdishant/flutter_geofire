import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_geofire/flutter_geofire.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<String> keysRetrieved = [];

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String pathToReference = "Sites";

    //Intializing geoFire
    Geofire.initialize(pathToReference);

    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      Geofire.queryAtLocation(30.730743, 76.774948, 5)?.listen((map) {
        print(map);
        if (map != null) {
          var callBack = map['callBack'];

          //latitude will be retrieved from map['latitude']
          //longitude will be retrieved from map['longitude']

          switch (callBack) {
            case Geofire.onKeyEntered:
              keysRetrieved.add(map["key"]);
              break;

            case Geofire.onKeyExited:
              keysRetrieved.remove(map["key"]);
              break;

            case Geofire.onKeyMoved:
//              keysRetrieved.add(map[callBack]);
              break;

            case Geofire.onGeoQueryReady:
//              map["result"].forEach((key){
//                keysRetrieved.add(key);
//              });

              break;
          }
        }

        setState(() {});
      }).onError((error) {
        print(error);
      });
    } on PlatformException {
//      response = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Plugin example app'),
          ),
          body: Column(
            children: <Widget>[
              Padding(
                padding: EdgeInsets.all(20.0),
              ),
              Center(
                child: keysRetrieved.length > 0
                    ? Text("First key is " +
                        keysRetrieved.first.toString() +
                        "\nTotal Keys " +
                        keysRetrieved.length.toString())
                    : CircularProgressIndicator(),
              ),
              Padding(
                padding: EdgeInsets.all(10.0),
              ),
              Center(
                child: TextButton(
                  style: TextButton.styleFrom(
                    backgroundColor: Colors.blueAccent,
                  ),
                  onPressed: () {
                    setLocation();
                  },
                  child: Text(
                    "Set Location",
                    style: TextStyle(color: Colors.white),
                  ),
                ),
              ),
              Padding(
                padding: EdgeInsets.all(10.0),
              ),
              Center(
                child: TextButton(
                  style: TextButton.styleFrom(
                    backgroundColor: Colors.blueAccent,
                  ),
                  onPressed: () {
                    setLocationFirst();
                  },
                  child: Text(
                    "Set Location AsH28LWk8MXfwRLfVxgx",
                    style: TextStyle(color: Colors.white),
                  ),
                ),
              ),
              Padding(
                padding: EdgeInsets.all(10.0),
              ),
              Center(
                child: TextButton(
                  onPressed: () {
                    getLocation();
                  },
                  style: TextButton.styleFrom(
                    backgroundColor: Colors.blueAccent,
                  ),
                  child: Text(
                    "Get Location AsH28LWk8MXfwRLfVxgx",
                    style: TextStyle(color: Colors.white),
                  ),
                ),
              ),
              Padding(
                padding: EdgeInsets.all(10.0),
              ),
              Center(
                child: TextButton(
                  onPressed: () {
                    removeLocation();
                  },
                  style: TextButton.styleFrom(
                    backgroundColor: Colors.blueAccent,
                  ),
                  child: Text(
                    "Remove Location AsH28LWk8MXfwRLfVxgx",
                    style: TextStyle(color: Colors.white),
                  ),
                ),
              ),
              Padding(
                padding: EdgeInsets.all(10.0),
              ),
              // Center(
              //   child: RaisedButton(
              //     onPressed: () {
              //       initPlatformState();
              //     },
              //     color: Colors.blueAccent,
              //     child: Text(
              //       "Register Listener",
              //       style: TextStyle(color: Colors.white),
              //     ),
              //   ),
              // ),
              // Padding(
              //   padding: EdgeInsets.all(10.0),
              // ),
              Center(
                child: TextButton(
                  onPressed: () {
                    removeQueryListener();
                  },
                  style: TextButton.styleFrom(
                    backgroundColor: Colors.blueAccent,
                  ),
                  child: Text(
                    "Remove Query Listener",
                    style: TextStyle(color: Colors.white),
                  ),
                ),
              ),
            ],
          )),
    );
  }

  void setLocation() async {
    bool? response = await Geofire.setLocation(
        new DateTime.now().millisecondsSinceEpoch.toString(),
        30.730743,
        76.774948);

    print(response);
  }

  void setLocationFirst() async {
    bool? response =
        await Geofire.setLocation("AsH28LWk8MXfwRLfVxgx", 30.730743, 76.774948);

    print(response);
  }

  void removeLocation() async {
    bool? response = await Geofire.removeLocation("AsH28LWk8MXfwRLfVxgx");

    print(response);
  }

  void removeQueryListener() async {
    bool? response = await Geofire.stopListener();

    keysRetrieved.clear();
    setState(() {});

    print(response);
  }

  void getLocation() async {
    Map<String, dynamic> response =
        await Geofire.getLocation("AsH28LWk8MXfwRLfVxgx");

    print(response);
  }
}
