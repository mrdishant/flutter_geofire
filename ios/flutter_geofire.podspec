#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'flutter_geofire'
  s.version          = '0.0.1'
  s.summary          = 'A Flutter plugin to get the realtime updates of places nearby.'
  s.description      = <<-DESC
A Flutter plugin to get the realtime updates of places nearby.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'

  s.dependency 'Flutter'
  s.ios.dependency 'Firebase/Database'
  s.ios.dependency 'Firebase'
  s.ios.dependency 'GeoFire'
  s.static_framework = true
  s.ios.deployment_target = '8.0'

  

  
end
