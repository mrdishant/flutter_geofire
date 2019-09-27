import Flutter
import UIKit
import GeoFire
import FirebaseDatabase


public class SwiftGeofirePlugin: NSObject, FlutterPlugin, FlutterStreamHandler {


    var geoFireRef:DatabaseReference?
    var geoFire:GeoFire? 
    var circleQuery: GFCircleQuery!
    var listening = false
    private var eventSink: FlutterEventSink?
    
    public static func register(with registrar: FlutterPluginRegistrar) {//This registers our streams and main messenger
        let channel = FlutterMethodChannel(name: "geofire", binaryMessenger: registrar.messenger())
        let stream_onEvent = FlutterEventChannel(name: "geofirestream", binaryMessenger: registrar.messenger())
        let instance = SwiftGeofirePlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
        stream_onEvent.setStreamHandler(instance)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    
        let arguements = call.arguments as? NSDictionary
        
        // Setup Geofire with path to locations database
        if(call.method.elementsEqual("GeoFire.start")){
            if geoFireRef == nil{
                let path = arguements!["path"] as! String
                
                geoFireRef = Database.database().reference().child(path)
                geoFire = GeoFire(firebaseRef: geoFireRef!)
            }

            result(true)
        }
        // Put location entry in database
        else if(call.method.elementsEqual("setLocation")){
            
            let id = arguements!["id"] as! String
            let lat = arguements!["lat"] as! Double
            let lng = arguements!["lng"] as! Double
            
            geoFire?.setLocation(CLLocation(latitude: lat, longitude: lng), forKey: id ) { (error) in
                if (error != nil) {
                    print("An error occured: \(String(describing: error))")
                    result("An error occured: \(String(describing: error))")
                    
                } else {
                    print("Saved location successfully!")
                    result(true)
                }
            }
        
        }
        // Remove location entry in database
        else if(call.method.elementsEqual("removeLocation")){
            
            let id = arguements!["id"] as! String
            

            geoFire?.removeKey(id) { (error) in
                if (error != nil) {
                    print("An error occured: \(String(describing: error))")
                    result("An error occured: \(String(describing: error))")
                    
                } else {
                    print("Removed location successfully!")
                    result(true)
                }
            }
            
        }
        // Retrieve location of entry by entry id
        else if(call.method.elementsEqual("getLocation")){
            
            let id = arguements!["id"] as! String
            
            
            geoFire?.getLocationForKey(id) { (location, error) in
                if (error != nil) {
                    print("An error occurred getting the location for \(id): \(String(describing: error?.localizedDescription))")
                } else if (location != nil) {
                    print("Location for \(id) is [\(String(describing: location?.coordinate.latitude)), \(location?.coordinate.longitude)]")
                    
                    var param=[String:AnyObject]()
                    param["lat"]=location?.coordinate.latitude as AnyObject
                    param["lng"]=location?.coordinate.longitude as AnyObject
                    
                    result(param)
                    
                } else {
                    
                    var param=[String:AnyObject]()
                    param["error"] = "GeoFire does not contain a location for \(id)" as AnyObject
                
                    
                    result(param)
                    
                    print("GeoFire does not contain a location for \(id)")
                }
            }
            
        }
        //setup Query with passed in location and a radius
        if(call.method.elementsEqual("queryAtLocation")){
            if circleQuery == nil{
                let lat = arguements!["lat"] as! Double
                let lng = arguements!["lng"] as! Double
                let radius = arguements!["radius"] as! Double
                
                let location:CLLocation = CLLocation(latitude: CLLocationDegrees(lat), longitude: CLLocationDegrees(lng))
                circleQuery = geoFire?.query(at: location, withRadius: radius)
            }//otherwise it is already setup!
            result(true)
        }
        // If Query exists, update query location and radius
        if(call.method.elementsEqual("updateLocation")){
            let lat = arguements!["lat"] as! Double
            let lng = arguements!["lng"] as! Double
            let radius = arguements!["radius"] as! Double
            let location:CLLocation = CLLocation(latitude: CLLocationDegrees(lat), longitude: CLLocationDegrees(lng))
            circleQuery!.center = location
            circleQuery!.radius = radius
            result(true)
        }
    }
    
    public func onListen(withArguments arguments: Any?, eventSink: @escaping FlutterEventSink) -> FlutterError? {
        self.eventSink = eventSink
        if !listening{
            listening = true
            circleQuery?.observe(.keyEntered, with: { (key, location) in
                do{
                    let data: [String:Any] = ["key": key, "lat": location.coordinate.latitude, "long": location.coordinate.longitude, "event": "ENTERED"]
                    let jsonData = try JSONSerialization.data(withJSONObject: data, options: .prettyPrinted)
                    self.eventSink?(jsonData)
                }catch{
                    self.eventSink?("ERROR")
                }
            })
            circleQuery?.observe(.keyExited, with: { (key, location) in
                do{
                    let data: [String:Any] = ["key": key, "event": "EXITED"]//"lat" :location.coordinate.latitude, "long": location.coordinate.longitude, "event": "EXITED"]
                    let jsonData = try JSONSerialization.data(withJSONObject: data, options: .prettyPrinted)
                    self.eventSink?(jsonData)
                }catch{
                    self.eventSink?("ERROR")
                }
            })
            circleQuery.observeReady({
                do{
                    print("All initial data has been loaded and events have been fired!")
                    let data: [String:Any] = ["event": "GEOQUERY_READY"]//location.coordinate.latitude, "long": location.coordinate.longitude, "event": "EXITED"]
                    let jsonData = try JSONSerialization.data(withJSONObject: data, options: .prettyPrinted)
                    self.eventSink?(jsonData)
                }catch{
                    self.eventSink?("ERROR")
                }
                
            })
        }
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        eventSink = nil
        circleQuery?.removeAllObservers()
        listening = false
        return nil
    }
}
