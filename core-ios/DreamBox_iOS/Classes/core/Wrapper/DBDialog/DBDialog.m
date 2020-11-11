//
//  DBDialog.m
//  DreamBox-iOS
//
//  Created by didi on 2020/5/17.
//  Copyright © 2020 didi. All rights reserved.
//

#import "DBDialog.h"
#import "DBHelper.h"

UIViewController *DBRootViewController()
{
    id<UIApplicationDelegate> delegate= [UIApplication sharedApplication].delegate;
    if([delegate respondsToSelector:@selector(window)]){
        UIWindow *window = [(NSObject *)delegate valueForKey:@"window"];
        UIViewController *controller = window.rootViewController;
        while (controller.presentedViewController) {
            controller = controller.presentedViewController;
        }
        return controller;
    }
    return nil;
}


@implementation DBDialog

+ (void)showAlertViewWithTitle:(NSString *)title message:(NSString *)message cancelButtonTitle:(NSString *)cancelButtonTitle otherButtonTitle:(NSString *)otherButtonTitle cancelButtonBlock:(DBAlertClickBlock)cancelBlock otherButtonBlock:(DBAlertClickBlock)otherBlock {
    
    if (![DBValidJudge isValidString:title]) {
        title = @"";
    }
    if (![DBValidJudge isValidString:message]) {
        message = @"";
    }
    
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];

    if ([DBValidJudge isValidString:cancelButtonTitle]) {
        UIAlertAction *action = [UIAlertAction actionWithTitle:cancelButtonTitle style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            if (cancelBlock) {
                cancelBlock();
            }
        }];
        [alertController addAction:action];
    }
    if ([DBValidJudge isValidString:otherButtonTitle]) {
        UIAlertAction *action = [UIAlertAction actionWithTitle:otherButtonTitle style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            if (cancelBlock) {
                cancelBlock();
            }
        }];
        [alertController addAction:action];
    }
    [[UIApplication sharedApplication].keyWindow.rootViewController presentViewController:alertController animated:YES completion:nil];
}




@end
