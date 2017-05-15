#import "Goby.h"

#if __has_include(<React/RCTConvert.h>)
#import <React/RCTConvert.h>
#else
#import "RCTConvert.h"
#endif

// Extending the RCTConvert class allows the React Native
// bridge to handle args of type "GobyUpdateState"
@implementation RCTConvert (GobyUpdateState)

RCT_ENUM_CONVERTER(GobyUpdateState, (@{ @"gobyUpdateStateRunning": @(GobyUpdateStateRunning),
                                            @"gobyUpdateStatePending": @(GobyUpdateStatePending),
                                            @"gobyUpdateStateLatest": @(GobyUpdateStateLatest)
                                          }),
                   GobyUpdateStateRunning, // Default enum value
                   integerValue)

@end
