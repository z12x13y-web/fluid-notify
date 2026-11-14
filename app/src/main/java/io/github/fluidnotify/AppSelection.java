package io.github.fluidnotify;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/* loaded from: classes.dex */
final class AppSelection {
    static final String ONLY_SELECTED = "only_selected_apps";
    static final String PACKAGES = "selected_app_packages";
    final boolean onlySelected;
    final Set<String> packages;

    AppSelection(boolean z, Collection<String> collection) {
        this.onlySelected = z;
        HashSet hashSet = new HashSet();
        if (collection != null) {
            for (String str : collection) {
                if (str != null && !str.isEmpty()) {
                    hashSet.add(str);
                }
            }
        }
        this.packages = Collections.unmodifiableSet(hashSet);
    }

    boolean allows(String str) {
        return (str == null || str.isEmpty() || (this.onlySelected && !this.packages.contains(str))) ? false : true;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AppSelection)) {
            return false;
        }
        AppSelection appSelection = (AppSelection) obj;
        return this.onlySelected == appSelection.onlySelected && this.packages.equals(appSelection.packages);
    }

    public int hashCode() {
        return (Boolean.hashCode(this.onlySelected) * 31) + this.packages.hashCode();
    }
}
