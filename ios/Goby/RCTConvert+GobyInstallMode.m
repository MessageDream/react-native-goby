#import "Goby.h"

#if __has_include(<React/RCTConvert.h>)
#import <React/RCTConvert.h>
#else
#import "RCTConvert.h"
#endif

// Extending the RCTConvert class allows the React Native
// bridge to handle args of type "GobyInstallMode"
@implementation RCTConvert (GobyInstallMode)

RCT_ENUM_CONVERTER(GobyInstallMode, (@{ @"gobyInstallModeImmediate": @(GobyInstallModeImmediate),
                                            @"gobyInstallModeOnNextRestart": @(GobyInstallModeOnNextRestart),
                                            @"gobyInstallModeOnNextResume": @(GobyInstallModeOnNextResume),
                                            @"gobyInstallModeOnNextSuspend": @(GobyInstallModeOnNextSuspend) }),
                   GobyInstallModeImmediate, // Default enum value
                   integerValue)

@end
