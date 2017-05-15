require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|

  s.name           = 'Goby'
  s.version        = package['version'].gsub(/v|-beta/, '')
  s.summary        = package['description']
  s.author         = package['author']
  s.license        = package['license']
  s.homepage       = package['homepage']
  s.source         = { :git => 'https://github.com/MessageDream/react-native-goby.git', :tag => "v#{s.version}-beta"}
  s.platform       = :ios, '7.0'
  s.preserve_paths = '*.js'
  s.library        = 'z'

  s.dependency 'React'

  s.subspec 'Core' do |ss|
    ss.source_files = 'ios/Goby/*.{h,m}'
    ss.public_header_files = ['ios/Goby/Goby.h']
  end

  s.subspec 'SSZipArchive' do |ss|
    ss.source_files = 'ios/Goby/SSZipArchive/*.{h,m}', 'ios/Goby/SSZipArchive/aes/*.{h,c}', 'ios/Goby/SSZipArchive/minizip/*.{h,c}'
    ss.private_header_files = 'ios/Goby/SSZipArchive/*.h', 'ios/Goby/SSZipArchive/aes/*.h', 'ios/Goby/SSZipArchive/minizip/*.h'
  end

  s.subspec 'DiffMatchPatch' do |ss|
    ss.source_files = 'ios/Goby/DiffMatchPatch/*.{h,m}'
    ss.private_header_files = 'ios/Goby/DiffMatchPatch/*.h'
  end

end