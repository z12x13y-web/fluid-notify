package io.github.fluidnotify;

import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public final class ClearCardList {
    private ClearCardList() {
    }

    public static <T> List<T> withFooter(List<? extends T> list, T t) {
        ArrayList arrayList = new ArrayList(list.size() + 1);
        for (T t2 : list) {
            if (t2 != t) {
                arrayList.add(t2);
            }
        }
        if (arrayList.size() > 4) {
            arrayList.add(t);
        }
        return arrayList;
    }
}
