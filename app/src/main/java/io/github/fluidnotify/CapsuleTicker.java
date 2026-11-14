package io.github.fluidnotify;

import android.app.Notification;
import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/* loaded from: classes.dex */
final class CapsuleTicker {
    private static final Set<ClassLoader> LOADERS = Collections.newSetFromMap(new WeakHashMap());
    static final String SOURCE = "io.github.fluidnotify.cardSource";
    private static final String VIEW = "io.github.fluidnotify.tickerView";

    CapsuleTicker() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void attach(FrameLayout frameLayout, LinearLayout linearLayout) {
        for (int i = 0; i < frameLayout.getChildCount(); i++) {
            View childAt = frameLayout.getChildAt(i);
            if (childAt != linearLayout) {
                childAt.setVisibility(8);
            }
        }
        if (linearLayout.getParent() == null) {
            frameLayout.addView(linearLayout, new FrameLayout.LayoutParams(linearLayout.getLayoutParams().width, linearLayout.getLayoutParams().height, 17));
        }
    }

    static StatusBarNotification capsuleSource(Object obj) {
        return source(XposedHelpers.getObjectField(XposedHelpers.getObjectField(XposedHelpers.getObjectField(obj, "m"), "a"), "a"));
    }

    static StatusBarNotification cardCreatorSource(Object obj) {
        return source(XposedHelpers.getObjectField(XposedHelpers.getObjectField(obj, "a"), "a"));
    }

    static StatusBarNotification cardSource(Object obj) {
        return cardCreatorSource(XposedHelpers.getObjectField(obj, "i"));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LinearLayout create(Context context, StatusBarNotification statusBarNotification) {
        float f = context.getResources().getDisplayMetrics().density;
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(16);
        linearLayout.setOrientation(0);
        linearLayout.setPadding(dp(f, 6), 0, dp(f, 6), 0);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(dp(f, 156), dp(f, 28)));
        linearLayout.setMinimumWidth(dp(f, 156));
        ImageView imageView = new ImageView(context);
        try {
            imageView.setImageDrawable(context.getPackageManager().getApplicationIcon(statusBarNotification.getPackageName()));
        } catch (Throwable unused) {
            imageView.setImageDrawable(statusBarNotification.getNotification().getSmallIcon().loadDrawable(context));
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(f, 18), dp(f, 18));
        layoutParams.setMarginEnd(dp(f, 5));
        linearLayout.addView(imageView, layoutParams);
        linearLayout.addView(new CapsuleMarquee(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        return linearLayout;
    }

    static void dismiss(ClassLoader classLoader, StatusBarNotification statusBarNotification) {
        XposedHelpers.callMethod(XposedHelpers.callMethod(XposedHelpers.getStaticObjectField(XposedHelpers.findClass("com.oplus.systemui.plugins.seedling.plugin.process.CallHandler", classLoader), "Companion"), "getInstance", new Object[0]), "dismissNotificationLiveAlert", new Object[]{statusBarNotification.getKey()});
    }

    private static int dp(float f, int i) {
        return Math.round(f * i);
    }

    static synchronized void install(final ClassLoader classLoader) {
        synchronized (CapsuleTicker.class) {
            Set<ClassLoader> set = LOADERS;
            if (set.contains(classLoader)) {
                return;
            }
            final PluginProfile detect = PluginProfile.detect(classLoader);
            if (detect == null) {
                return;
            }
            Class findClass = XposedHelpers.findClass(detect.wrapper, classLoader);
            set.add(classLoader);
            try {
                NotificationTiming.install(classLoader, detect);
            } catch (Throwable th) {
                report("notification timing install", th);
            }
            try {
                NotificationExpansion.install(classLoader, detect);
            } catch (Throwable th2) {
                report("notification expansion install", th2);
            }
            try {
                ClearAllCards.install(classLoader, detect);
            } catch (Throwable th3) {
                report("clear-card install", th3);
            }
            try {
                CardSwipe.install(classLoader, detect);
            } catch (Throwable th4) {
                report("card-swipe install", th4);
            }
            try {
                PullDownWindow.install(classLoader, detect);
            } catch (Throwable th5) {
                report("pull-down window install", th5);
            }
            XposedBridge.hookAllMethods(XposedHelpers.findClass(detect.liveModel, classLoader), detect.strongReminder, new XC_MethodHook() { // from class: io.github.fluidnotify.CapsuleTicker.1
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    StatusBarNotification source = CapsuleTicker.source(XposedHelpers.getObjectField(methodHookParam.thisObject, "a"));
                    if (source == null || source.getNotification().extras.getInt("io.github.fluidnotify.ticker.expandSeconds", 0) != 0) {
                        return;
                    }
                    methodHookParam.setResult(false);
                }
            });
            Class findClass2 = XposedHelpers.findClass(detect.viewModel, classLoader);
            XposedBridge.hookAllMethods(findClass2, "a", new XC_MethodHook() { // from class: io.github.fluidnotify.CapsuleTicker.2
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    try {
                        Object obj = null;
                        long j = Long.MIN_VALUE;
                        for (Object obj2 : (List) XposedHelpers.getObjectField(methodHookParam.thisObject, PluginProfile.this.capsuleList)) {
                            StatusBarNotification capsuleSource = CapsuleTicker.capsuleSource(obj2);
                            if (capsuleSource != null && capsuleSource.getPostTime() > j) {
                                j = capsuleSource.getPostTime();
                                obj = obj2;
                            }
                        }
                        if (obj != null) {
                            String str = (String) XposedHelpers.getObjectField(obj, "a");
                            if (str.equals(XposedHelpers.getObjectField(methodHookParam.thisObject, PluginProfile.this.selectedKey))) {
                                return;
                            }
                            XposedHelpers.callMethod(methodHookParam.thisObject, "G", new Object[]{str, XposedHelpers.getObjectField(obj, "v"), false, true});
                            XposedHelpers.callMethod(methodHookParam.thisObject, "A", new Object[]{1});
                        }
                    } catch (Throwable th6) {
                        CapsuleTicker.report("selection", th6);
                    }
                }
            });
            XposedBridge.hookAllMethods(findClass2, "p", new XC_MethodHook() { // from class: io.github.fluidnotify.CapsuleTicker.3
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    try {
                        String str = (String) (Boolean.TRUE.equals(methodHookParam.args[1]) ? XposedHelpers.getObjectField(methodHookParam.thisObject, PluginProfile.this.selectedKey) : methodHookParam.args[0]);
                        for (Object obj : (List) XposedHelpers.getObjectField(methodHookParam.thisObject, PluginProfile.this.capsuleList)) {
                            if (str.equals(XposedHelpers.getObjectField(obj, "a")) && CapsuleTicker.capsuleSource(obj) != null) {
                                XposedHelpers.callMethod(methodHookParam.thisObject, "w", new Object[]{str});
                                methodHookParam.setResult((Object) null);
                                return;
                            }
                        }
                    } catch (Throwable th6) {
                        CapsuleTicker.report("expand", th6);
                    }
                }
            });
            XposedBridge.hookAllMethods(XposedHelpers.findClass(detect.template, classLoader), detect.templateRefresh, new XC_MethodHook() { // from class: io.github.fluidnotify.CapsuleTicker.4
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    FrameLayout frameLayout = (FrameLayout) XposedHelpers.getObjectField(methodHookParam.thisObject, PluginProfile.this.templateHost);
                    LinearLayout linearLayout = (LinearLayout) XposedHelpers.getAdditionalInstanceField(frameLayout, CapsuleTicker.VIEW);
                    if (linearLayout != null) {
                        CapsuleTicker.attach(frameLayout, linearLayout);
                    }
                    PullDownWindow.refresh(frameLayout);
                }
            });
            XC_MethodHook xC_MethodHook = new XC_MethodHook() { // from class: io.github.fluidnotify.CapsuleTicker.5
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    try {
                        if (methodHookParam.getResult() instanceof FrameLayout) {
                            FrameLayout frameLayout = (FrameLayout) methodHookParam.getResult();
                            StatusBarNotification source = CapsuleTicker.source(XposedHelpers.getObjectField(methodHookParam.thisObject, PluginProfile.this.wrapperSource));
                            if (source == null) {
                                XposedHelpers.removeAdditionalInstanceField(frameLayout, CapsuleTicker.SOURCE);
                                PullDownWindow.bind(frameLayout, null, classLoader, PluginProfile.this);
                                LinearLayout linearLayout = (LinearLayout) XposedHelpers.getAdditionalInstanceField(frameLayout, CapsuleTicker.VIEW);
                                if (linearLayout != null) {
                                    frameLayout.removeView(linearLayout);
                                    for (int i = 0; i < frameLayout.getChildCount(); i++) {
                                        frameLayout.getChildAt(i).setVisibility(0);
                                    }
                                    XposedHelpers.removeAdditionalInstanceField(frameLayout, CapsuleTicker.VIEW);
                                    XposedHelpers.removeAdditionalInstanceField(methodHookParam.thisObject, CapsuleTicker.VIEW);
                                    return;
                                }
                                return;
                            }
                            Object objectField = XposedHelpers.getObjectField(methodHookParam.thisObject, PluginProfile.this.wrapperSize);
                            if ("VIEW_SIZE_LG".equals(((Enum) objectField).name())) {
                                XposedHelpers.setAdditionalInstanceField(frameLayout, CapsuleTicker.SOURCE, source);
                                PullDownWindow.bind(frameLayout, source, classLoader, PluginProfile.this);
                                return;
                            }
                            PullDownWindow.bind(frameLayout, null, classLoader, PluginProfile.this);
                            if ("VIEW_SIZE_SM".equals(((Enum) objectField).name())) {
                                LinearLayout linearLayout2 = (LinearLayout) XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, CapsuleTicker.VIEW);
                                if (linearLayout2 == null) {
                                    linearLayout2 = CapsuleTicker.create(frameLayout.getContext(), source);
                                    XposedHelpers.setAdditionalInstanceField(methodHookParam.thisObject, CapsuleTicker.VIEW, linearLayout2);
                                    XposedHelpers.setAdditionalInstanceField(frameLayout, CapsuleTicker.VIEW, linearLayout2);
                                    if ("io.github.fluidnotify".equals(source.getPackageName())) {
                                        XposedBridge.log("FluidNotify: test ticker view created; id=" + source.getId());
                                    }
                                }
                                CapsuleTicker.attach(frameLayout, linearLayout2);
                                CapsuleMarquee capsuleMarquee = (CapsuleMarquee) linearLayout2.getChildAt(1);
                                String string = source.getNotification().extras.getString("io.github.fluidnotify.ticker.text");
                                if (string == null) {
                                    string = CapsuleTicker.notificationText(source.getNotification(), "新通知");
                                }
                                capsuleMarquee.setText(string);
                                linearLayout2.setContentDescription(string);
                            }
                        }
                    } catch (Throwable th6) {
                        CapsuleTicker.report("view", th6);
                    }
                }
            };
            XposedBridge.hookAllMethods(findClass, detect.viewMethod, xC_MethodHook);
            Class findClassIfExists = XposedHelpers.findClassIfExists(detect.notificationWrapper, classLoader);
            if (findClassIfExists != null) {
                XposedBridge.hookAllMethods(findClassIfExists, detect.viewMethod, xC_MethodHook);
            }
            XposedBridge.log("FluidNotify: collapsed ticker plugin hooks installed; ColorOS=" + (detect.modern ? 17 : 16));
        }
    }

    static String notificationText(Notification notification, String str) {
        CharSequence charSequence = notification.extras.getCharSequence("android.title");
        CharSequence charSequence2 = notification.extras.getCharSequence("android.bigText");
        if (TextUtils.isEmpty(charSequence2)) {
            charSequence2 = notification.extras.getCharSequence("android.text");
        }
        String trim = charSequence == null ? "" : charSequence.toString().trim();
        String trim2 = charSequence2 != null ? charSequence2.toString().trim() : "";
        if (trim.isEmpty()) {
            trim = trim2;
        } else if (!trim2.isEmpty() && !trim.equals(trim2)) {
            trim = trim + " · " + trim2;
        }
        return trim.isEmpty() ? str : trim.replace('\n', ' ').replace('\r', ' ');
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void report(String str, Throwable th) {
        XposedBridge.log("FluidNotify: ticker " + str + " failed: " + th.getClass().getSimpleName());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static StatusBarNotification source(Object obj) {
        if (obj == null) {
            return null;
        }
        String name = obj.getClass().getName();
        if (!"t3.d".equals(name) && !"w3.b".equals(name) && !"e4.a".equals(name) && !"h3.c".equals(name) && !"k3.b".equals(name) && !"com.oplus.systemui.livealert.classic.origin.e".equals(name) && !"com.oplus.systemui.livealert.notification.origin.d".equals(name) && !"com.oplus.systemui.livealert.template.origin.b".equals(name)) {
            return null;
        }
        StatusBarNotification statusBarNotification = (StatusBarNotification) XposedHelpers.getObjectField(obj, "a");
        if (statusBarNotification.getNotification().extras.getBoolean("io.github.fluidnotify.ticker", false)) {
            return statusBarNotification;
        }
        return null;
    }
}
