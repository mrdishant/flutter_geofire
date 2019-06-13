#import "GeofirePlugin.h"
#import <flutter_geofire/flutter_geofire-Swift.h>


@implementation GeofirePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftGeofirePlugin registerWithRegistrar:registrar];
}
@end
