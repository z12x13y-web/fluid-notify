package io.github.fluidnotify;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import io.github.fluidnotify.ClearAllCards;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
final class ClearAllCards {
    private static final String FOOTER = "io.github.fluidnotify.footerView";
    private static final String KEY = "io.github.fluidnotify.clearAll";
    private static final String RAW = "io.github.fluidnotify.realCards";
    private static final String STATE = "io.github.fluidnotify.clearState";
    private static final String TO_START = "io.github.fluidnotify.clearToStart";

    /* JADX INFO: Access modifiers changed from: private */
    static final class State {
        boolean busy;
        final TextView button;
        Object closingModel;
        final View container;
        boolean freeze;
        final Object item;
        final ClassLoader loader;
        final PluginProfile profile;

        State(View view, Class<?> cls, ClassLoader classLoader, PluginProfile pluginProfile) {
            this.container = view;
            this.loader = classLoader;
            this.profile = pluginProfile;
            Context context = view.getContext();
            float f = context.getResources().getDisplayMetrics().density;
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, Math.round(64.0f * f)));
            TextView textView = new TextView(context);
            this.button = textView;
            textView.setText("清空");
            textView.setTextColor(-1);
            textView.setTextSize(16.0f);
            textView.setTypeface(Typeface.DEFAULT, 1);
            textView.setGravity(17);
            textView.setContentDescription("清空可清除的通知卡片");
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, ClearAllCards.rounded(-263367335, f));
            stateListDrawable.addState(new int[0], ClearAllCards.rounded(-433376199, f));
            textView.setBackground(stateListDrawable);
            textView.setOnClickListener(new View.OnClickListener() { // from class: io.github.fluidnotify.ClearAllCards$State$$ExternalSyntheticLambda1
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    ClearAllCards.State.this.m35lambda$new$0$iogithubfluidnotifyClearAllCards$State(view2);
                }
            });
            frameLayout.addView(textView, new FrameLayout.LayoutParams(-1, -1));
            this.item = XposedHelpers.newInstance(cls, new Object[]{ClearAllCards.KEY, ClearAllCards.KEY, frameLayout, null, null, false});
        }

        void clear() {
            if (this.busy) {
                return;
            }
            try {
                List list = (List) XposedHelpers.getObjectField(this.container, this.profile.containerList);
                final Map targets = ClearAllCards.targets(list);
                if (targets.isEmpty()) {
                    return;
                }
                this.busy = true;
                this.freeze = true;
                this.button.setEnabled(false);
                final Object objectField = XposedHelpers.getObjectField(this.container, this.profile.modern ? "b" : "m");
                this.closingModel = objectField;
                if ((this.profile.modern || this.profile.compact16) && targets.size() == list.size()) {
                    XposedHelpers.setAdditionalInstanceField(objectField, ClearAllCards.TO_START, Boolean.TRUE);
                    XposedBridge.log("FluidNotify: clear-card uses CARD_TO_START");
                }
                Object staticObjectField = XposedHelpers.getStaticObjectField(XposedHelpers.findClass(this.profile.slideAction, this.loader), this.profile.slideSingleton);
                XposedHelpers.callMethod(this.container, "setOverScrollEnable", new Object[]{false});
                XposedHelpers.callMethod(objectField, this.profile.modern ? "f" : "g", new Object[]{staticObjectField});
                final Handler handler = new Handler(Looper.getMainLooper());
                final long uptimeMillis = SystemClock.uptimeMillis() + 2000;
                this.container.postOnAnimation(new Runnable() { // from class: io.github.fluidnotify.ClearAllCards$State$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        ClearAllCards.State.this.m34lambda$clear$1$iogithubfluidnotifyClearAllCards$State(targets);
                    }
                });
                handler.post(new Runnable() { // from class: io.github.fluidnotify.ClearAllCards.State.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (State.this.container.isShown() && SystemClock.uptimeMillis() < uptimeMillis) {
                            handler.postDelayed(this, 16L);
                            return;
                        }
                        State.this.freeze = false;
                        State.this.busy = false;
                        XposedHelpers.removeAdditionalInstanceField(objectField, ClearAllCards.TO_START);
                        State.this.closingModel = null;
                        try {
                            XposedHelpers.callMethod(State.this.container, State.this.profile.containerUpdate, new Object[0]);
                            State state = State.this;
                            state.update((List) XposedHelpers.getObjectField(state.container, State.this.profile.containerList));
                        } catch (Throwable th) {
                            ClearAllCards.report("refresh", th);
                        }
                    }
                });
            } catch (Throwable th) {
                this.busy = false;
                this.freeze = false;
                Object obj = this.closingModel;
                if (obj != null) {
                    XposedHelpers.removeAdditionalInstanceField(obj, ClearAllCards.TO_START);
                    this.closingModel = null;
                }
                ClearAllCards.report("click", th);
            }
        }

        /* renamed from: lambda$clear$1$io-github-fluidnotify-ClearAllCards$State, reason: not valid java name */
        /* synthetic */ void m34lambda$clear$1$iogithubfluidnotifyClearAllCards$State(Map map) {
            XposedBridge.log("FluidNotify: clear-card clearing during collapse; count=" + map.size());
            Iterator it = map.values().iterator();
            while (it.hasNext()) {
                try {
                    CapsuleTicker.dismiss(this.loader, (StatusBarNotification) it.next());
                } catch (Throwable th) {
                    ClearAllCards.report("dismiss", th);
                }
            }
        }

        /* renamed from: lambda$new$0$io-github-fluidnotify-ClearAllCards$State, reason: not valid java name */
        /* synthetic */ void m35lambda$new$0$iogithubfluidnotifyClearAllCards$State(View view) {
            clear();
        }

        boolean update(List<?> list) {
            boolean isEmpty = ClearAllCards.targets(list).isEmpty();
            boolean z = !isEmpty;
            boolean z2 = (this.busy || isEmpty) ? false : true;
            this.button.setEnabled(z2);
            this.button.setAlpha(z2 ? 1.0f : 0.5f);
            return z;
        }
    }

    ClearAllCards() {
    }

    private static boolean canClear(StatusBarNotification statusBarNotification) {
        if (statusBarNotification == null || !statusBarNotification.getNotification().extras.getBoolean("io.github.fluidnotify.ticker", false)) {
            return false;
        }
        return NotificationPolicy.shouldConvert(statusBarNotification.getNotification().flags, statusBarNotification.getNotification().getSmallIcon() != null);
    }

    private static void expandCards(ClassLoader classLoader, final Class<?> cls, final PluginProfile pluginProfile) {
        Class findClass = XposedHelpers.findClass(pluginProfile.cardModel, classLoader);
        final Class findClass2 = XposedHelpers.findClass(pluginProfile.cardState, classLoader);
        final Object staticObjectField = XposedHelpers.getStaticObjectField(XposedHelpers.findClass(pluginProfile.hiddenState, classLoader), "e");
        XposedHelpers.findAndHookMethod(findClass, pluginProfile.selectCards, new Object[]{List.class, new XC_MethodHook() { // from class: io.github.fluidnotify.ClearAllCards.6
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                if (methodHookParam.hasThrowable()) {
                    return;
                }
                try {
                    if (Boolean.TRUE.equals(XposedHelpers.callMethod(methodHookParam.thisObject, PluginProfile.this.manualState, new Object[0]))) {
                        ArrayList arrayList = new ArrayList();
                        HashSet hashSet = new HashSet();
                        for (Object obj : (List) methodHookParam.args[0]) {
                            String str = (String) XposedHelpers.getObjectField(obj, "a");
                            if (!hashSet.contains(str)) {
                                Object objectField = XposedHelpers.getObjectField(obj, "u");
                                Object obj2 = staticObjectField;
                                if (!obj2.equals(XposedHelpers.callMethod(objectField, "a", new Object[]{obj2}))) {
                                    arrayList.add(obj);
                                    hashSet.add(str);
                                }
                            }
                        }
                        methodHookParam.setResult(arrayList);
                    }
                } catch (Throwable th) {
                    ClearAllCards.report("select", th);
                }
            }
        }});
        XposedHelpers.findAndHookMethod(findClass, pluginProfile.updateCards, new Object[]{List.class, new XC_MethodHook() { // from class: io.github.fluidnotify.ClearAllCards.7
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                View view;
                if (methodHookParam.hasThrowable()) {
                    return;
                }
                try {
                    boolean z = false;
                    List list = (List) methodHookParam.args[0];
                    if (list.size() <= 4) {
                        return;
                    }
                    ArrayList arrayList = new ArrayList((List) XposedHelpers.getObjectField(XposedHelpers.getObjectField(methodHookParam.thisObject, "f"), "a"));
                    ArrayList arrayList2 = new ArrayList((List) XposedHelpers.getObjectField(methodHookParam.thisObject, "e"));
                    Set modelKeys = ClearAllCards.modelKeys(arrayList2);
                    boolean z2 = false;
                    for (Object obj : list) {
                        String str = (String) XposedHelpers.getObjectField(obj, "a");
                        if (!modelKeys.contains(str) && (view = (View) XposedHelpers.callMethod(XposedHelpers.getObjectField(obj, "i"), "e", new Object[0])) != null) {
                            Object objectField = XposedHelpers.getObjectField(obj, "s");
                            arrayList.add(XposedHelpers.newInstance(cls, new Object[]{str, XposedHelpers.getObjectField(obj, "x"), view, XposedHelpers.getObjectField(obj, "j"), XposedHelpers.getObjectField(obj, "l"), Boolean.valueOf(PluginProfile.this.modern && (XposedHelpers.getIntField(obj, "C") == 3 || !(objectField == null || XposedHelpers.getIntField(objectField, "n") == 0)))}));
                            arrayList2.add(obj);
                            modelKeys.add(str);
                            z2 = true;
                        }
                    }
                    if (z2) {
                        Iterator it = arrayList2.iterator();
                        while (it.hasNext()) {
                            z |= XposedHelpers.getBooleanField(it.next(), "d");
                        }
                        Object newInstance = XposedHelpers.newInstance(findClass2, new Object[]{arrayList, Boolean.valueOf(z)});
                        XposedHelpers.setObjectField(methodHookParam.thisObject, "e", arrayList2);
                        XposedHelpers.setObjectField(methodHookParam.thisObject, "f", newInstance);
                        XposedHelpers.callMethod(methodHookParam.thisObject, PluginProfile.this.updateAppearance, new Object[]{Boolean.valueOf(XposedHelpers.getBooleanField(methodHookParam.thisObject, "h"))});
                        Object objectField2 = XposedHelpers.getObjectField(methodHookParam.thisObject, "l");
                        if (objectField2 != null) {
                            XposedHelpers.callMethod(objectField2, "a", new Object[]{newInstance});
                        }
                        methodHookParam.setResult(4);
                    }
                } catch (Throwable th) {
                    ClearAllCards.report("expand", th);
                }
            }
        }});
    }

    static void install(final ClassLoader classLoader, final PluginProfile pluginProfile) {
        Class findClass = XposedHelpers.findClass("com.oplus.systemui.plugins.seedling.card.ui.view.CardContainer", classLoader);
        final Class findClass2 = XposedHelpers.findClass(pluginProfile.cardItem, classLoader);
        if (pluginProfile.modern || pluginProfile.compact16) {
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass(pluginProfile.modern ? "com.oplus.systemui.plugins.seedling.card.ui.viewmodel.f" : "s4.d", classLoader), pluginProfile.modern ? "u" : "v", new Object[]{new XC_MethodHook() { // from class: io.github.fluidnotify.ClearAllCards.1
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    if (Boolean.TRUE.equals(XposedHelpers.getAdditionalInstanceField(XposedHelpers.getObjectField(methodHookParam.thisObject, "b"), ClearAllCards.TO_START))) {
                        methodHookParam.setResult(0);
                    }
                }
            }});
        }
        if (pluginProfile.modern) {
            installFooterBinding(classLoader, findClass2);
        }
        expandCards(classLoader, findClass2, pluginProfile);
        XposedHelpers.findAndHookMethod(findClass, pluginProfile.containerUpdate, new Object[]{new XC_MethodHook() { // from class: io.github.fluidnotify.ClearAllCards.2
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                Object objectExtra = methodHookParam.getObjectExtra(ClearAllCards.RAW);
                if (objectExtra != null) {
                    try {
                        XposedHelpers.setObjectField(methodHookParam.thisObject, pluginProfile.containerList, objectExtra);
                    } catch (Throwable th) {
                        ClearAllCards.report("restore", th);
                    }
                }
            }

            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                try {
                    View view = (View) methodHookParam.thisObject;
                    State state = (State) XposedHelpers.getAdditionalInstanceField(view, ClearAllCards.STATE);
                    if (state == null) {
                        state = new State(view, findClass2, classLoader, pluginProfile);
                        XposedHelpers.setAdditionalInstanceField(view, ClearAllCards.STATE, state);
                    }
                    if (state.freeze) {
                        methodHookParam.setResult((Object) null);
                        return;
                    }
                    List<?> list = (List) XposedHelpers.getObjectField(view, pluginProfile.containerList);
                    Object withFooter = state.update(list) ? ClearCardList.withFooter(list, state.item) : new ArrayList(list);
                    methodHookParam.setObjectExtra(ClearAllCards.RAW, list);
                    XposedHelpers.setObjectField(view, pluginProfile.containerList, withFooter);
                } catch (Throwable th) {
                    ClearAllCards.report("update", th);
                }
            }
        }});
    }

    private static void installFooterBinding(ClassLoader classLoader, Class<?> cls) {
        Class findClass = XposedHelpers.findClass("com.oplus.systemui.plugins.seedling.card.ui.view.CardView", classLoader);
        XposedHelpers.findAndHookMethod(findClass, "h", new Object[]{cls, new XC_MethodHook() { // from class: io.github.fluidnotify.ClearAllCards.3
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                Object obj = methodHookParam.args[0];
                if (!ClearAllCards.KEY.equals(XposedHelpers.getObjectField(obj, "a"))) {
                    XposedHelpers.removeAdditionalInstanceField(methodHookParam.thisObject, ClearAllCards.FOOTER);
                    return;
                }
                methodHookParam.setResult((Object) null);
                try {
                    FrameLayout frameLayout = (FrameLayout) methodHookParam.thisObject;
                    XposedHelpers.callMethod(frameLayout, "j", new Object[0]);
                    View view = (View) XposedHelpers.getObjectField(obj, "c");
                    if (view.getParent() instanceof ViewGroup) {
                        ((ViewGroup) view.getParent()).removeView(view);
                    }
                    frameLayout.addView(view, new FrameLayout.LayoutParams(-1, view.getLayoutParams().height));
                    XposedHelpers.setObjectField(frameLayout, "i", view);
                    XposedHelpers.setAdditionalInstanceField(frameLayout, ClearAllCards.FOOTER, Boolean.TRUE);
                    frameLayout.setAlpha(1.0f);
                    frameLayout.setScaleX(1.0f);
                    frameLayout.setScaleY(1.0f);
                } catch (Throwable th) {
                    ClearAllCards.report("footer bind", th);
                }
            }
        }});
        XposedHelpers.findAndHookMethod(findClass, "onInterceptTouchEvent", new Object[]{MotionEvent.class, new XC_MethodHook() { // from class: io.github.fluidnotify.ClearAllCards.4
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                if (Boolean.TRUE.equals(XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, ClearAllCards.FOOTER))) {
                    methodHookParam.setResult(false);
                }
            }
        }});
        XposedHelpers.findAndHookMethod(findClass, "j", new Object[]{new XC_MethodHook() { // from class: io.github.fluidnotify.ClearAllCards.5
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                XposedHelpers.removeAdditionalInstanceField(methodHookParam.thisObject, ClearAllCards.FOOTER);
            }
        }});
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Set<String> modelKeys(List<?> list) {
        HashSet hashSet = new HashSet();
        Iterator<?> it = list.iterator();
        while (it.hasNext()) {
            hashSet.add((String) XposedHelpers.getObjectField(it.next(), "a"));
        }
        return hashSet;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void report(String str, Throwable th) {
        XposedBridge.log("FluidNotify: clear-card " + str + " failed: " + th.getClass().getSimpleName());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static GradientDrawable rounded(int i, float f) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(f * 24.0f);
        return gradientDrawable;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Map<String, StatusBarNotification> targets(List<?> list) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        Iterator<?> it = list.iterator();
        while (it.hasNext()) {
            View view = (View) XposedHelpers.getObjectField(it.next(), "c");
            if (view != null) {
                StatusBarNotification statusBarNotification = (StatusBarNotification) XposedHelpers.getAdditionalInstanceField(view, "io.github.fluidnotify.cardSource");
                if (canClear(statusBarNotification)) {
                    linkedHashMap.put(statusBarNotification.getKey(), statusBarNotification);
                }
            }
        }
        return linkedHashMap;
    }
}
