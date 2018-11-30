# flutter_geofire

A Flutter plugin to use the [GeoFire Api](https://github.com/googlearchive/geofire)

For Flutter plugins for other products, see [mrdishant@github](https://github.com/mrdishant)

Note: This plugin is still under development, and some APIs might not be available yet. Feedback and Pull Requests are most welcome!

## Usage

GeoFire  — Realtime location queries with Firebase.

GeoFire is an open-source library that allows you to store and query a set of keys based on their geographic location.

At its heart, GeoFire simply stores locations with string keys. Its main benefit however, is the possibility of querying keys within a given geographic area - all in realtime.

GeoFire uses the Firebase database for data storage, allowing query results to be updated in realtime as they change. GeoFire selectively loads only the data near certain locations, keeping your applications light and responsive, even with extremely large datasets.

###Quickstart

 Initalize GeoFire with path to keys in Realtime Database
    
    String pathToReference = "Sites";
    Intializing geoFire
    Geofire.initialize(pathToReference);
    
####Setting location data

Here setLocation method is used and first is the unique id of the place and other two parameters are latitude and longitude of that place.

    bool response = await Geofire.setLocation(
            new DateTime.now().millisecondsSinceEpoch.toString(),
            30.730743,
            76.774948)
            
####Retrieving a location

Retrieving a location for a single key in GeoFire happens like below:

    Map<String, dynamic> response =
            await Geofire.getLocation("AsH28LWk8MXfwRLfVxgx");
    
    print(response);
            
####Geo Queries

GeoFire allows you to query all keys within a geographic area using GeoQuery objects. As the locations for keys change, the query is updated in realtime and fires events letting you know if any relevant keys have moved. GeoQuery parameters can be updated later to change the size and center of the queried area.

    response = await Geofire.queryAtLocation(30.730743, 76.774948, 5);


####Removing a location
To remove a location and delete it from the database simply pass the location's key to removeLocation:

    bool response = await Geofire.removeLocation("AsH28LWk8MXfwRLfVxgx");

    print(response);                



#####This plugin is in development suggestions are welcome. Happy Coding!!!

