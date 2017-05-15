#import "Goby.h"

void CPLog(NSString *formatString, ...) {
    va_list args;
    va_start(args, formatString);
    NSString *prependedFormatString = [NSString stringWithFormat:@"\n[Goby] %@", formatString];
    NSLogv(prependedFormatString, args);
    va_end(args);
}