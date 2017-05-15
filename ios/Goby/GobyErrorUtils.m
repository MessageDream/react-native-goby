#import "Goby.h"

@implementation GobyErrorUtils

static NSString *const GobyErrorDomain = @"GobyError";
static const int GobyErrorCode = -1;

+ (NSError *)errorWithMessage:(NSString *)errorMessage
{
    return [NSError errorWithDomain:GobyErrorDomain
                               code:GobyErrorCode
                           userInfo:@{ NSLocalizedDescriptionKey: NSLocalizedString(errorMessage, nil) }];
}

+ (BOOL)isGobyError:(NSError *)err
{
    return err != nil && [GobyErrorDomain isEqualToString:err.domain];
}

@end