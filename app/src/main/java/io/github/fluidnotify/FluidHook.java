package io.github.fluidnotify;

import android.app.AndroidAppHelper;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.fluidnotify.ConfigReload;
import io.github.fluidnotify.FluidHook;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class FluidHook implements IXposedHookLoadPackage {
    private static final String CONVERTED = "io.github.fluidnotify.converted";
    private static final String ROOT = "com.oplus.systemui.statusbar.notification.livealert.data.";
    private static final String TAG = "FluidNotify";
    static final String TICKER = "io.github.fluidnotify.ticker";
    private volatile boolean configLoaded;
    private ConfigReload<Bundle> configReload;
    private int converted;
    private volatile boolean enabled = true;
    private volatile AppSelection appSelection = new AppSelection(false, null);

    /* renamed from: io.github.fluidnotify.FluidHook$5, reason: invalid class name */
    class AnonymousClass5 extends XC_MethodHook {
        final /* synthetic */ XC_LoadPackage.LoadPackageParam val$loaded;

        AnonymousClass5(XC_LoadPackage.LoadPackageParam loadPackageParam) {
            this.val$loaded = loadPackageParam;
        }

        protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
            if (methodHookParam.hasThrowable()) {
                return;
            }
            if (methodHookParam.args[0] instanceof Context) {
                Context context = (Context) methodHookParam.args[0];
                final Object obj = methodHookParam.thisObject;
                FloatingReplySync.attach(obj);
                SmallWindowLauncher.attach(obj, this.val$loaded.classLoader);
                if (FluidHook.this.configReload != null) {
                    return;
                }
                final Handler handler = new Handler(Looper.getMainLooper());
                HandlerThread handlerThread = new HandlerThread("FluidNotify-config");
                handlerThread.start();
                final Handler handler2 = new Handler(handlerThread.getLooper());
                final Context currentApplication = AndroidAppHelper.currentApplication();
                if (currentApplication == null) {
                    currentApplication = context;
                }
                FluidHook.this.configReload = new ConfigReload(new ConfigReload.Queue() { // from class: io.github.fluidnotify.FluidHook.5.1
                    @Override // io.github.fluidnotify.ConfigReload.Queue
                    public void post(Runnable runnable, long j) {
                        handler.postDelayed(runnable, j);
                    }

                    @Override // io.github.fluidnotify.ConfigReload.Queue
                    public void remove(Runnable runnable) {
                        handler.removeCallbacks(runnable);
                    }
                }, new Consumer() { // from class: io.github.fluidnotify.FluidHook$5$$ExternalSyntheticLambda0
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj2) {
                        handler2.post((Runnable) obj2);
                    }
                }, new Supplier() { // from class: io.github.fluidnotify.FluidHook$5$$ExternalSyntheticLambda1
                    @Override // java.util.function.Supplier
                    public final Object get() {
                        return FluidHook.AnonymousClass5.this.m53lambda$afterHookedMethod$1$iogithubfluidnotifyFluidHook$5(currentApplication);
                    }
                }, new Consumer() { // from class: io.github.fluidnotify.FluidHook$5$$ExternalSyntheticLambda2
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj2) {
                        FluidHook.AnonymousClass5.this.m54lambda$afterHookedMethod$2$iogithubfluidnotifyFluidHook$5(obj, (Bundle) obj2);
                    }
                }, new Consumer() { // from class: io.github.fluidnotify.FluidHook$5$$ExternalSyntheticLambda3
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj2) {
                        XposedBridge.log("FluidNotify: configuration read failed; retrying: " + ((RuntimeException) obj2).getClass().getSimpleName());
                    }
                });
                context.registerReceiver(new BroadcastReceiver() { // from class: io.github.fluidnotify.FluidHook.5.2
                    @Override // android.content.BroadcastReceiver
                    public void onReceive(Context context2, Intent intent) {
                        if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                            FluidHook.this.configReload.request();
                        }
                    }
                }, new IntentFilter("android.intent.action.USER_UNLOCKED"), null, handler, Build.VERSION.SDK_INT >= 33 ? 4 : 0);
                context.registerReceiver(new BroadcastReceiver() { // from class: io.github.fluidnotify.FluidHook.5.3
                    @Override // android.content.BroadcastReceiver
                    public void onReceive(Context context2, Intent intent) {
                        FluidHook.this.configReload.request();
                        FluidHook.this.applyConfig(intent.getExtras(), obj);
                    }
                }, new IntentFilter("io.github.fluidnotify.CONFIG_CHANGED"), "io.github.fluidnotify.permission.CONTROL", handler, Build.VERSION.SDK_INT >= 33 ? 2 : 0);
                final ConfigReload configReload = FluidHook.this.configReload;
                Objects.requireNonNull(configReload);
                handler.post(new Runnable() { // from class: io.github.fluidnotify.FluidHook$5$$ExternalSyntheticLambda4
                    @Override // java.lang.Runnable
                    public final void run() {
                        ConfigReload.this.request();
                    }
                });
            }
        }

        /* renamed from: lambda$afterHookedMethod$1$io-github-fluidnotify-FluidHook$5, reason: not valid java name */
        /* synthetic */ Bundle m53lambda$afterHookedMethod$1$iogithubfluidnotifyFluidHook$5(Context context) {
            return FluidHook.this.readConfig(context);
        }

        /* renamed from: lambda$afterHookedMethod$2$io-github-fluidnotify-FluidHook$5, reason: not valid java name */
        /* synthetic */ void m54lambda$afterHookedMethod$2$iogithubfluidnotifyFluidHook$5(Object obj, Bundle bundle) {
            FluidHook.this.applyConfig(bundle, obj);
            XposedBridge.log("FluidNotify: saved configuration loaded; enabled=" + FluidHook.this.enabled + " edge=" + SwipeConfig.percent() + " distance=" + SwipeConfig.distance() + " shrinkSeconds=" + DurationConfig.seconds() + " expandSeconds=" + AutoExpandConfig.seconds() + " pullDownWindow=" + PullDownConfig.enabled() + " selectedOnly=" + FluidHook.this.appSelection.onlySelected + " selectedCount=" + FluidHook.this.appSelection.packages.size());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyConfig(Bundle bundle, Object obj) {
        if (bundle == null) {
            return;
        }
        boolean isEnabled = isEnabled();
        AppSelection appSelection = this.appSelection;
        int seconds = DurationConfig.seconds();
        boolean enabled = PullDownConfig.enabled();
        if (bundle.containsKey("enabled")) {
            this.enabled = bundle.getBoolean("enabled", true);
        }
        this.appSelection = AppSelectionSettings.fromBundle(bundle, this.appSelection);
        if (bundle.containsKey("enabled") && bundle.containsKey("only_selected_apps") && bundle.containsKey("selected_app_packages")) {
            this.configLoaded = true;
        }
        NotificationTiming.setEnabled(isEnabled());
        PullDownConfig.apply(bundle, isEnabled());
        if (enabled != PullDownConfig.enabled()) {
            PullDownWindow.configurationChanged();
        }
        if (bundle.containsKey("notification_shrink_seconds")) {
            DurationConfig.set(bundle.getInt("notification_shrink_seconds", 0));
        }
        if (bundle.containsKey("notification_auto_expand_seconds")) {
            AutoExpandConfig.set(bundle.getInt("notification_auto_expand_seconds", 0));
        }
        if (bundle.containsKey("swipe_active_edge")) {
            SwipeConfig.set(bundle.getInt("swipe_active_edge", 15));
        }
        if (bundle.containsKey("swipe_dismiss_distance")) {
            SwipeConfig.setDistance(bundle.getInt("swipe_dismiss_distance", 50));
        }
        if (isEnabled != isEnabled() || seconds != DurationConfig.seconds()) {
            NotificationTiming.refresh();
        }
        if (isEnabled != isEnabled() || !appSelection.equals(this.appSelection)) {
            try {
                XposedHelpers.callMethod(obj, "reInitLiveAlert", new Object[0]);
            } catch (Throwable th) {
                XposedBridge.log("FluidNotify: configuration refresh failed: " + th.getClass().getSimpleName());
            }
        }
        if (enabled != PullDownConfig.enabled()) {
            FloatingReplySync.refreshAll();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void clearConversionExtras(Notification notification) {
        notification.extras.remove(TICKER);
        notification.extras.remove("io.github.fluidnotify.ticker.text");
        notification.extras.remove("io.github.fluidnotify.ticker.expandSeconds");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isEnabled() {
        return this.configLoaded && this.enabled;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Bundle readConfig(Context context) {
        try {
            return context.getContentResolver().call(Uri.parse("content://io.github.fluidnotify.config"), "get_config", (String) null, (Bundle) null);
        } catch (IllegalArgumentException | SecurityException e) {
            try {
                context.sendBroadcast(new Intent("io.github.fluidnotify.REQUEST_CONFIG_ACCESS").setComponent(new ComponentName("io.github.fluidnotify", "io.github.fluidnotify.ConfigAccessReceiver")).addFlags(268435488));
            } catch (RuntimeException e2) {
                e.addSuppressed(e2);
            }
            throw e;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static StatusBarNotification sbn(Object obj) {
        return (StatusBarNotification) XposedHelpers.callMethod(obj, "getSbn", new Object[0]);
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if ("com.android.systemui".equals(loadPackageParam.packageName) && "com.android.systemui".equals(loadPackageParam.processName)) {
            Class findClassIfExists = XposedHelpers.findClassIfExists("com.oplus.systemui.statusbar.seeding.SeedlingPluginManager", loadPackageParam.classLoader);
            if (findClassIfExists != null) {
                XposedBridge.hookAllMethods(findClassIfExists, "onPluginConnected", new XC_MethodHook() { // from class: io.github.fluidnotify.FluidHook.1
                    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                        if (methodHookParam.args.length <= 0 || methodHookParam.args[0] == null) {
                            return;
                        }
                        CapsuleTicker.install(methodHookParam.args[0].getClass().getClassLoader());
                    }
                });
            }
            Class findClassIfExists2 = XposedHelpers.findClassIfExists("com.oplus.systemui.capsule.CapsulePluginClassLoader", loadPackageParam.classLoader);
            if (findClassIfExists2 != null) {
                XposedBridge.hookAllMethods(findClassIfExists2, "init", new XC_MethodHook() { // from class: io.github.fluidnotify.FluidHook.2
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                        if (methodHookParam.hasThrowable() || !(methodHookParam.args[0] instanceof Context)) {
                            return;
                        }
                        CapsuleTicker.install(((Context) methodHookParam.args[0]).getClassLoader());
                    }
                });
            }
            Class findClassIfExists3 = XposedHelpers.findClassIfExists("com.oplus.systemui.statusbar.notification.livealert.data.repository.OplusLiveAlertFilterByPlugin", loadPackageParam.classLoader);
            Class findClassIfExists4 = XposedHelpers.findClassIfExists("com.oplus.systemui.statusbar.notification.livealert.data.repository.OplusLiveAlertNotificationsRepository", loadPackageParam.classLoader);
            if (findClassIfExists3 == null || findClassIfExists4 == null) {
                Log.e(TAG, "Unsupported firmware: native live-alert classes are absent; hooks disabled");
                return;
            }
            try {
                FloatingReplySync.install(loadPackageParam.classLoader, new BooleanSupplier() { // from class: io.github.fluidnotify.FluidHook$$ExternalSyntheticLambda0
                    @Override // java.util.function.BooleanSupplier
                    public final boolean getAsBoolean() {
                        boolean isEnabled;
                        isEnabled = FluidHook.this.isEnabled();
                        return isEnabled;
                    }
                });
            } catch (Throwable th) {
                XposedBridge.log("FluidNotify: floating reply hook install failed: " + th.getClass().getSimpleName());
            }
            try {
                SmallWindowTransition.install(loadPackageParam.classLoader);
            } catch (Throwable th2) {
                XposedBridge.log("FluidNotify: window transition install failed: " + th2.getClass().getSimpleName());
            }
            final boolean z = XposedHelpers.findClassIfExists("com.oplus.systemui.statusbar.notification.livealert.data.model.SbnModel", loadPackageParam.classLoader) != null;
            XposedBridge.hookAllMethods(findClassIfExists3, z ? "filterEntry" : "shouldFilter", new XC_MethodHook() { // from class: io.github.fluidnotify.FluidHook.3
                /* JADX WARN: Code restructure failed: missing block: B:42:0x00ab, code lost:
                
                    io.github.fluidnotify.FluidHook.clearConversionExtras(r3);
                 */
                /* JADX WARN: Code restructure failed: missing block: B:43:0x00ae, code lost:
                
                    return;
                 */
                /*
                    Code decompiled incorrectly, please refer to instructions dump.
                    To view partially-correct add '--show-bad-code' argument
                */
                protected void afterHookedMethod(de.robv.android.xposed.XC_MethodHook.MethodHookParam r7) {
                    /*
                        r6 = this;
                        boolean r0 = r7.hasThrowable()
                        if (r0 != 0) goto Lcc
                        java.lang.Object[] r0 = r7.args
                        int r0 = r0.length
                        r1 = 1
                        if (r0 != r1) goto Lcc
                        java.lang.Object[] r0 = r7.args
                        r2 = 0
                        r0 = r0[r2]
                        if (r0 != 0) goto L15
                        goto Lcc
                    L15:
                        java.lang.Object[] r0 = r7.args
                        r0 = r0[r2]
                        java.lang.Boolean r3 = java.lang.Boolean.FALSE
                        java.lang.String r4 = "io.github.fluidnotify.converted"
                        de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField(r0, r4, r3)
                        java.lang.Object[] r0 = r7.args     // Catch: java.lang.Throwable -> Laf
                        r0 = r0[r2]     // Catch: java.lang.Throwable -> Laf
                        android.service.notification.StatusBarNotification r0 = io.github.fluidnotify.FluidHook.m52$$Nest$smsbn(r0)     // Catch: java.lang.Throwable -> Laf
                        android.app.Notification r3 = r0.getNotification()     // Catch: java.lang.Throwable -> Laf
                        io.github.fluidnotify.FluidHook r5 = io.github.fluidnotify.FluidHook.this     // Catch: java.lang.Throwable -> Laf
                        boolean r5 = io.github.fluidnotify.FluidHook.m49$$Nest$misEnabled(r5)     // Catch: java.lang.Throwable -> Laf
                        if (r5 == 0) goto La9
                        io.github.fluidnotify.FluidHook r5 = io.github.fluidnotify.FluidHook.this     // Catch: java.lang.Throwable -> Laf
                        io.github.fluidnotify.AppSelection r5 = io.github.fluidnotify.FluidHook.m42$$Nest$fgetappSelection(r5)     // Catch: java.lang.Throwable -> Laf
                        java.lang.String r0 = r0.getPackageName()     // Catch: java.lang.Throwable -> Laf
                        boolean r0 = r5.allows(r0)     // Catch: java.lang.Throwable -> Laf
                        if (r0 != 0) goto L45
                        goto La9
                    L45:
                        if (r3 == 0) goto L62
                        int r0 = r3.flags     // Catch: java.lang.Throwable -> Laf
                        r0 = r0 & 512(0x200, float:7.175E-43)
                        if (r0 == 0) goto L62
                        io.github.fluidnotify.FluidHook.m51$$Nest$smclearConversionExtras(r3)     // Catch: java.lang.Throwable -> Laf
                        boolean r6 = r2     // Catch: java.lang.Throwable -> Laf
                        if (r6 == 0) goto L5c
                        java.lang.Integer r6 = java.lang.Integer.valueOf(r2)     // Catch: java.lang.Throwable -> Laf
                    L58:
                        r7.setResult(r6)     // Catch: java.lang.Throwable -> Laf
                        goto L61
                    L5c:
                        java.lang.Boolean r6 = java.lang.Boolean.valueOf(r2)     // Catch: java.lang.Throwable -> Laf
                        goto L58
                    L61:
                        return
                    L62:
                        if (r3 == 0) goto La3
                        int r0 = r3.flags     // Catch: java.lang.Throwable -> Laf
                        android.graphics.drawable.Icon r5 = r3.getSmallIcon()     // Catch: java.lang.Throwable -> Laf
                        if (r5 == 0) goto L6e
                        r5 = r1
                        goto L6f
                    L6e:
                        r5 = r2
                    L6f:
                        boolean r0 = io.github.fluidnotify.NotificationPolicy.shouldConvert(r0, r5)     // Catch: java.lang.Throwable -> Laf
                        if (r0 == 0) goto La3
                        java.lang.Object[] r0 = r7.args     // Catch: java.lang.Throwable -> Laf
                        r0 = r0[r2]     // Catch: java.lang.Throwable -> Laf
                        java.lang.Boolean r2 = java.lang.Boolean.TRUE     // Catch: java.lang.Throwable -> Laf
                        de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField(r0, r4, r2)     // Catch: java.lang.Throwable -> Laf
                        android.os.Bundle r0 = r3.extras     // Catch: java.lang.Throwable -> Laf
                        java.lang.String r2 = "io.github.fluidnotify.ticker"
                        r0.putBoolean(r2, r1)     // Catch: java.lang.Throwable -> Laf
                        android.os.Bundle r0 = r3.extras     // Catch: java.lang.Throwable -> Laf
                        java.lang.String r2 = "io.github.fluidnotify.ticker.text"
                        java.lang.String r4 = "新通知"
                        java.lang.String r3 = io.github.fluidnotify.CapsuleTicker.notificationText(r3, r4)     // Catch: java.lang.Throwable -> Laf
                        r0.putString(r2, r3)     // Catch: java.lang.Throwable -> Laf
                        boolean r6 = r2     // Catch: java.lang.Throwable -> Laf
                        if (r6 == 0) goto L9e
                        java.lang.Integer r6 = java.lang.Integer.valueOf(r1)     // Catch: java.lang.Throwable -> Laf
                    L9a:
                        r7.setResult(r6)     // Catch: java.lang.Throwable -> Laf
                        goto Lcc
                    L9e:
                        java.lang.Boolean r6 = java.lang.Boolean.valueOf(r1)     // Catch: java.lang.Throwable -> Laf
                        goto L9a
                    La3:
                        if (r3 == 0) goto Lcc
                        io.github.fluidnotify.FluidHook.m51$$Nest$smclearConversionExtras(r3)     // Catch: java.lang.Throwable -> Laf
                        goto Lcc
                    La9:
                        if (r3 == 0) goto Lae
                        io.github.fluidnotify.FluidHook.m51$$Nest$smclearConversionExtras(r3)     // Catch: java.lang.Throwable -> Laf
                    Lae:
                        return
                    Laf:
                        r6 = move-exception
                        java.lang.StringBuilder r7 = new java.lang.StringBuilder
                        java.lang.String r0 = "Admission hook failed: "
                        r7.<init>(r0)
                        java.lang.Class r6 = r6.getClass()
                        java.lang.String r6 = r6.getSimpleName()
                        java.lang.StringBuilder r6 = r7.append(r6)
                        java.lang.String r6 = r6.toString()
                        java.lang.String r7 = "FluidNotify"
                        android.util.Log.e(r7, r6)
                    Lcc:
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: io.github.fluidnotify.FluidHook.AnonymousClass3.afterHookedMethod(de.robv.android.xposed.XC_MethodHook$MethodHookParam):void");
                }
            });
            Class findClassIfExists5 = XposedHelpers.findClassIfExists("com.oplus.systemui.statusbar.notification.livealert.data.interactor.OplusLiveAlertNotificationsInteractor$preGroupFilter$1", loadPackageParam.classLoader);
            if (findClassIfExists5 != null) {
                XposedBridge.hookAllMethods(findClassIfExists5, "shouldFilterOut", new XC_MethodHook() { // from class: io.github.fluidnotify.FluidHook.4
                    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                        if (FluidHook.this.isEnabled() && Boolean.TRUE.equals(XposedHelpers.getAdditionalInstanceField(methodHookParam.args[0], FluidHook.CONVERTED))) {
                            methodHookParam.setResult(false);
                        }
                    }
                });
            }
            XposedBridge.hookAllConstructors(findClassIfExists4, new AnonymousClass5(loadPackageParam));
            XposedBridge.hookAllMethods(findClassIfExists4, "addLiveAlert", new XC_MethodHook() { // from class: io.github.fluidnotify.FluidHook.6
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    if (FluidHook.this.isEnabled()) {
                        if (methodHookParam.args.length == (z ? 4 : 3) && Boolean.TRUE.equals(XposedHelpers.getAdditionalInstanceField(methodHookParam.args[0], FluidHook.CONVERTED))) {
                            methodHookParam.args[z ? (char) 2 : (char) 1] = Integer.valueOf(AutoExpandConfig.remindType());
                            if (AutoExpandConfig.seconds() > 0) {
                                methodHookParam.args[z ? (char) 3 : (char) 2] = true;
                            }
                        }
                    }
                }
            });
            XposedBridge.hookAllMethods(findClassIfExists4, "access$updateLiveAlert", new XC_MethodHook() { // from class: io.github.fluidnotify.FluidHook.7
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    if (FluidHook.this.isEnabled()) {
                        if (methodHookParam.args.length == (z ? 5 : 4) && Boolean.TRUE.equals(XposedHelpers.getAdditionalInstanceField(methodHookParam.args[1], FluidHook.CONVERTED))) {
                            methodHookParam.args[z ? (char) 3 : (char) 0] = Integer.valueOf(AutoExpandConfig.remindType());
                            if (AutoExpandConfig.seconds() > 0) {
                                methodHookParam.args[z ? (char) 4 : (char) 3] = true;
                            }
                        }
                    }
                }
            });
            XposedBridge.hookAllMethods(findClassIfExists4, "entryToLiveAlert", new XC_MethodHook() { // from class: io.github.fluidnotify.FluidHook.8
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    if (methodHookParam.hasThrowable() || methodHookParam.getResult() == null) {
                        return;
                    }
                    int i = 0;
                    Object callMethod = z ? XposedHelpers.callMethod(methodHookParam.getResult(), "getSbn", new Object[0]) : methodHookParam.getResult();
                    if (callMethod instanceof StatusBarNotification) {
                        StatusBarNotification statusBarNotification = (StatusBarNotification) callMethod;
                        if (FluidHook.this.isEnabled() && Boolean.TRUE.equals(XposedHelpers.getAdditionalInstanceField(methodHookParam.args[0], FluidHook.CONVERTED))) {
                            try {
                                JSONObject jSONObject = new JSONObject(statusBarNotification.getNotification().extras.getString("oplusLiveAlertOptions", "{}"));
                                jSONObject.put("showHostMap", new JSONArray().put(8));
                                jSONObject.put("lockScreenShowHostMap", new JSONArray());
                                int seconds = ((Number) methodHookParam.args[z ? (char) 2 : (char) 1]).intValue() == 0 ? 0 : AutoExpandConfig.seconds();
                                if (seconds != 0) {
                                    i = 11;
                                }
                                jSONObject.put("remindType", i);
                                statusBarNotification.getNotification().extras.putString("oplusLiveAlertOptions", jSONObject.toString());
                                statusBarNotification.getNotification().extras.putBoolean(FluidHook.TICKER, true);
                                statusBarNotification.getNotification().extras.putInt("io.github.fluidnotify.ticker.expandSeconds", seconds);
                                statusBarNotification.getNotification().extras.putString("io.github.fluidnotify.ticker.text", CapsuleTicker.notificationText(statusBarNotification.getNotification(), "新通知"));
                                FloatingReplySync.record(statusBarNotification);
                            } catch (Throwable th3) {
                                XposedBridge.log("FluidNotify: status-bar options failed: " + th3.getClass().getSimpleName());
                            }
                        }
                        FluidHook.this.converted++;
                        if ("io.github.fluidnotify".equals(statusBarNotification.getPackageName())) {
                            XposedBridge.log("FluidNotify: test id=" + statusBarNotification.getId() + " reached native entryToLiveAlert; conversions=" + FluidHook.this.converted + " options=" + statusBarNotification.getNotification().extras.containsKey("oplusLiveAlertOptions") + " remindType=" + methodHookParam.args[z ? (char) 2 : (char) 1]);
                        }
                    }
                }
            });
            Log.i(TAG, "Native admission and conversion hooks installed; enabled=" + isEnabled());
            XposedBridge.log("FluidNotify: native ColorOS live-alert hooks installed");
        }
    }
}
