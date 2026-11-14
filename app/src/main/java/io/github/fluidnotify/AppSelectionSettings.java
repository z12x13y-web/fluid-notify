package io.github.fluidnotify;

import android.content.SharedPreferences;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/* loaded from: classes.dex */
final class AppSelectionSettings {
    private AppSelectionSettings() {
    }

    static Bundle bundle(AppSelection appSelection) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("only_selected_apps", appSelection.onlySelected);
        ArrayList<String> arrayList = new ArrayList<>(appSelection.packages);
        Collections.sort(arrayList);
        bundle.putStringArrayList("selected_app_packages", arrayList);
        return bundle;
    }

    static AppSelection fromBundle(Bundle bundle, AppSelection appSelection) {
        return (bundle.containsKey("only_selected_apps") && bundle.containsKey("selected_app_packages")) ? new AppSelection(bundle.getBoolean("only_selected_apps", false), bundle.getStringArrayList("selected_app_packages")) : appSelection;
    }

    static AppSelection read(SharedPreferences sharedPreferences) {
        return new AppSelection(sharedPreferences.getBoolean("only_selected_apps", false), sharedPreferences.getStringSet("selected_app_packages", Collections.emptySet()));
    }

    static void save(SharedPreferences sharedPreferences, AppSelection appSelection) {
        sharedPreferences.edit().putBoolean("only_selected_apps", appSelection.onlySelected).putStringSet("selected_app_packages", new HashSet(appSelection.packages)).apply();
    }
}
