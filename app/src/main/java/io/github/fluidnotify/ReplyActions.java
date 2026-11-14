package io.github.fluidnotify;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import java.util.ArrayList;

/* loaded from: classes.dex */
final class ReplyActions {
    ReplyActions() {
    }

    static Notification.Action find(StatusBarNotification statusBarNotification) {
        Notification.Action[] actionArr = statusBarNotification.getNotification().actions;
        if (actionArr == null) {
            return null;
        }
        for (Notification.Action action : actionArr) {
            if (isFloatingReply(action)) {
                return action;
            }
        }
        return null;
    }

    static StatusBarNotification forCard(StatusBarNotification statusBarNotification, boolean z) {
        if (!z || !statusBarNotification.getNotification().extras.getBoolean("io.github.fluidnotify.ticker", false) || find(statusBarNotification) == null) {
            return statusBarNotification;
        }
        ArrayList arrayList = new ArrayList();
        for (Notification.Action action : statusBarNotification.getNotification().actions) {
            if (!isFloatingReply(action)) {
                arrayList.add(action);
            }
        }
        StatusBarNotification clone = statusBarNotification.clone();
        clone.getNotification().actions = (Notification.Action[]) arrayList.toArray(new Notification.Action[0]);
        return clone;
    }

    static boolean isFloatingReply(Notification.Action action) {
        return action != null && action.actionIntent != null && action.getExtras().getInt("tag_smart_reply", 0) == 1 && "com.android.systemui".equals(action.actionIntent.getCreatorPackage());
    }
}
