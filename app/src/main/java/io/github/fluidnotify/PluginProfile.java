package io.github.fluidnotify;

import de.robv.android.xposed.XposedHelpers;

/* loaded from: classes.dex */
final class PluginProfile {
    final String capsuleList;
    final String[] cardConverters;
    final String cardDataModel;
    final String cardItem;
    final String cardModel;
    final String cardState;
    final boolean compact16;
    final String containerList;
    final String containerUpdate;
    final String hiddenState;
    final String liveModel;
    final String manualState;
    final boolean modern;
    final String notificationWrapper;
    final String selectCards;
    final String selectedKey;
    final String slideAction;
    final String slideSingleton;
    final String strongReminder;
    final String swipeHelper;
    final String template;
    final String templateHost;
    final String templateRefresh;
    final String timingDuration;
    final String timingRefresh;
    final String timingRegister;
    final String timingStrategy;
    final String updateAppearance;
    final String updateCards;
    final String viewMethod;
    final String viewModel;
    final String wrapper;
    final String wrapperSize;
    final String wrapperSource;

    private PluginProfile(boolean z) {
        this(z, false);
    }

    private PluginProfile(boolean z, boolean z2) {
        this.compact16 = z2;
        this.modern = z;
        this.viewModel = z ? "com.oplus.systemui.plugins.seedling.capsule.ui.viewmodel.u" : z2 ? "i4.q" : "a5.q";
        this.liveModel = z ? "com.oplus.systemui.plugins.seedling.data.interactor.n" : z2 ? "w4.i" : "o5.j";
        this.template = z ? "com.oplus.systemui.plugins.shared.template.wrapper.impl.u" : z2 ? "a6.i" : "h7.x";
        this.cardConverters = z ? new String[]{"com.oplus.systemui.plugins.shared.data.parse.c", "com.oplus.systemui.plugins.shared.data.parse.s"} : z2 ? new String[]{"v5.c"} : new String[]{"p6.c", "p6.r"};
        this.cardModel = z ? "com.oplus.systemui.plugins.seedling.card.ui.viewmodel.h" : z2 ? "s4.j" : "k5.j";
        this.cardDataModel = z ? "com.oplus.systemui.plugins.seedling.card.data.model.a" : z2 ? "k4.a" : "c5.a";
        this.cardItem = z ? "com.oplus.systemui.plugins.seedling.card.ui.model.o" : z2 ? "p4.o" : "h5.o";
        this.cardState = z ? "com.oplus.systemui.plugins.seedling.card.ui.model.k" : z2 ? "p4.k" : "h5.k";
        this.hiddenState = z ? "com.oplus.systemui.plugins.seedling.data.interactor.r" : z2 ? "w4.m" : "o5.n";
        this.wrapper = z ? "com.oplus.systemui.livealert.classic.origin.o" : z2 ? "h3.f" : "t3.g";
        this.notificationWrapper = z ? "com.oplus.systemui.livealert.notification.origin.e" : z2 ? "k3.c" : "w3.d";
        this.capsuleList = z ? "m" : "l";
        this.selectedKey = z ? "n" : "m";
        this.wrapperSource = z ? "b" : "a";
        this.wrapperSize = z ? "d" : "c";
        this.viewMethod = z ? "f" : z2 ? "getView" : "n";
        this.templateHost = (z || z2) ? "j" : "i";
        this.templateRefresh = (z || z2) ? "c" : "e";
        this.containerList = z ? "c" : "n";
        this.containerUpdate = z ? "i" : "g";
        this.selectCards = z ? "i" : "j";
        this.updateCards = z ? "r" : "s";
        this.manualState = z ? "n" : "p";
        this.updateAppearance = z ? "o" : "q";
        this.strongReminder = z2 ? "G" : "L";
        this.swipeHelper = z ? "com.oplus.systemui.plugins.seedling.card.ui.view.touch.b" : z2 ? "r4.b" : "j5.b";
        this.slideAction = z ? "com.oplus.systemui.plugins.seedling.card.ui.model.g" : z2 ? "p4.g" : "h5.g";
        this.slideSingleton = z ? "g" : z2 ? "j" : "v";
        this.timingStrategy = z ? "com.oplus.systemui.plugins.seedling.capsule.strategy.d" : z2 ? "f4.d" : "x4.d";
        this.timingDuration = z ? "j" : "h";
        this.timingRegister = z ? "n" : "m";
        this.timingRefresh = z ? "q" : "o";
    }

    static PluginProfile detect(ClassLoader classLoader) {
        if (XposedHelpers.findClassIfExists("com.oplus.systemui.livealert.classic.origin.o", classLoader) != null) {
            return new PluginProfile(true);
        }
        if (XposedHelpers.findClassIfExists("h3.f", classLoader) != null && XposedHelpers.findClassIfExists("i4.q", classLoader) != null) {
            return new PluginProfile(false, true);
        }
        if (XposedHelpers.findClassIfExists("t3.g", classLoader) != null) {
            return new PluginProfile(false);
        }
        return null;
    }
}
