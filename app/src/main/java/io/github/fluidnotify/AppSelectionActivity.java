package io.github.fluidnotify;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import io.github.fluidnotify.AppSelectionActivity;
import io.github.fluidnotify.GlassStyle;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* loaded from: classes.dex */
public final class AppSelectionActivity extends Activity {
    private AppAdapter adapter;
    private TextView empty;
    private boolean loadFailed;
    private boolean onlySelected;
    private SharedPreferences prefs;
    private EditText search;
    private Button selectAll;
    private CheckBox selectedFilter;
    private TextView summary;
    private final Set<String> selected = new HashSet();
    private final List<App> apps = new ArrayList();
    private final List<App> visible = new ArrayList();
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final LruCache<String, Drawable> icons = new LruCache<>(48);
    private final Set<String> loadingIcons = new HashSet();
    private boolean loading = true;

    /* JADX INFO: Access modifiers changed from: private */
    static final class App {
        final boolean installed;
        final String label;
        final String packageName;
        final boolean system;

        App(String str, String str2, boolean z, boolean z2) {
            this.packageName = str;
            this.label = str2;
            this.system = z;
            this.installed = z2;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    final class AppAdapter extends BaseAdapter {
        private AppAdapter() {
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return AppSelectionActivity.this.visible.size();
        }

        @Override // android.widget.Adapter
        public App getItem(int i) {
            return (App) AppSelectionActivity.this.visible.get(i);
        }

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            AppRow appRow = view instanceof AppRow ? (AppRow) view : AppSelectionActivity.this.new AppRow();
            final App item = getItem(i);
            appRow.label.setText(item.label);
            appRow.detail.setText(!item.installed ? AppSelectionActivity.this.getString(R.string.app_unavailable, new Object[]{item.packageName}) : item.system ? AppSelectionActivity.this.getString(R.string.app_system, new Object[]{item.packageName}) : item.packageName);
            appRow.checked.setChecked(AppSelectionActivity.this.selected.contains(item.packageName));
            appRow.setContentDescription(item.label + "，" + item.packageName + (AppSelectionActivity.this.selected.contains(item.packageName) ? "，已选择" : "，未选择"));
            Drawable drawable = (Drawable) AppSelectionActivity.this.icons.get(item.packageName);
            if (drawable != null) {
                appRow.icon.setImageDrawable(drawable);
            } else {
                appRow.icon.setImageResource(android.R.drawable.sym_def_app_icon);
                if (item.installed && AppSelectionActivity.this.loadingIcons.add(item.packageName)) {
                    AppSelectionActivity.this.worker.execute(new Runnable() { // from class: io.github.fluidnotify.AppSelectionActivity$AppAdapter$$ExternalSyntheticLambda0
                        @Override // java.lang.Runnable
                        public final void run() {
                            AppSelectionActivity.AppAdapter.this.m22x5d4f2982(item);
                        }
                    });
                }
            }
            return appRow;
        }

        /* renamed from: lambda$getView$0$io-github-fluidnotify-AppSelectionActivity$AppAdapter, reason: not valid java name */
        /* synthetic */ void m21x33fad441(App app, Drawable drawable) {
            if (AppSelectionActivity.this.isDestroyed() || AppSelectionActivity.this.isFinishing()) {
                return;
            }
            AppSelectionActivity.this.loadingIcons.remove(app.packageName);
            AppSelectionActivity.this.icons.put(app.packageName, drawable);
            notifyDataSetChanged();
        }

        /* renamed from: lambda$getView$1$io-github-fluidnotify-AppSelectionActivity$AppAdapter, reason: not valid java name */
        /* synthetic */ void m22x5d4f2982(final App app) {
            final Drawable defaultActivityIcon;
            try {
                defaultActivityIcon = AppSelectionActivity.this.getPackageManager().getApplicationIcon(app.packageName);
            } catch (PackageManager.NameNotFoundException | RuntimeException unused) {
                defaultActivityIcon = AppSelectionActivity.this.getPackageManager().getDefaultActivityIcon();
            }
            AppSelectionActivity.this.ui.post(new Runnable() { // from class: io.github.fluidnotify.AppSelectionActivity$AppAdapter$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    AppSelectionActivity.AppAdapter.this.m21x33fad441(app, defaultActivityIcon);
                }
            });
        }
    }

    private final class AppRow extends LinearLayout {
        final CheckBox checked;
        final TextView detail;
        final ImageView icon;
        final TextView label;

        AppRow() {
            super(AppSelectionActivity.this);
            ImageView imageView = new ImageView(AppSelectionActivity.this);
            this.icon = imageView;
            TextView text = AppSelectionActivity.this.text("", 15, -15189184);
            this.label = text;
            TextView text2 = AppSelectionActivity.this.text("", 11, -10520708);
            this.detail = text2;
            CheckBox checkBox = new CheckBox(AppSelectionActivity.this);
            this.checked = checkBox;
            setGravity(16);
            setPadding(AppSelectionActivity.this.dp(8.0f), AppSelectionActivity.this.dp(10.0f), AppSelectionActivity.this.dp(4.0f), AppSelectionActivity.this.dp(10.0f));
            setMinimumHeight(AppSelectionActivity.this.dp(72.0f));
            addView(imageView, new LinearLayout.LayoutParams(AppSelectionActivity.this.dp(40.0f), AppSelectionActivity.this.dp(40.0f)));
            LinearLayout column = AppSelectionActivity.this.column();
            column.setPadding(AppSelectionActivity.this.dp(12.0f), 0, AppSelectionActivity.this.dp(4.0f), 0);
            column.addView(text);
            column.addView(text2);
            addView(column, new LinearLayout.LayoutParams(0, -2, 1.0f));
            checkBox.setClickable(false);
            checkBox.setFocusable(false);
            checkBox.setImportantForAccessibility(2);
            column.setImportantForAccessibility(4);
            imageView.setImportantForAccessibility(2);
            addView(checkBox);
        }
    }

    private Button button(String str, boolean z) {
        Button button = new Button(this);
        button.setText(str);
        button.setTextSize(14.0f);
        button.setAllCaps(false);
        button.setTextColor(z ? -1 : -15303564);
        button.setMinHeight(dp(50.0f));
        GlassStyle.pressable(button, z ? -15303564 : -1379342, 16);
        return button;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public LinearLayout column() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        return linearLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int dp(float f) {
        return GlassStyle.dp(this, f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void filter() {
        String lowerCase = this.search.getText().toString().trim().toLowerCase(Locale.ROOT);
        this.visible.clear();
        for (App app : this.apps) {
            if (!this.selectedFilter.isChecked() || this.selected.contains(app.packageName)) {
                if (app.label.toLowerCase(Locale.ROOT).contains(lowerCase) || app.packageName.toLowerCase(Locale.ROOT).contains(lowerCase)) {
                    this.visible.add(app);
                }
            }
        }
        this.empty.setText(this.loading ? "正在读取应用列表…" : this.loadFailed ? "应用列表读取失败，点击重试" : (this.selectedFilter.isChecked() && this.selected.isEmpty()) ? "尚未选择应用" : "没有匹配的应用");
        this.adapter.notifyDataSetChanged();
        this.selectAll.setEnabled((this.loading || this.loadFailed || this.apps.isEmpty()) ? false : true);
        updateSummary();
    }

    static /* synthetic */ int lambda$loadApps$9(Set set, Collator collator, App app, App app2) {
        int compare = Boolean.compare(set.contains(app2.packageName), set.contains(app.packageName));
        if (compare == 0) {
            compare = Boolean.compare(app.system, app2.system);
        }
        if (compare == 0) {
            compare = collator.compare(app.label, app2.label);
        }
        return compare == 0 ? app.packageName.compareTo(app2.packageName) : compare;
    }

    private void loadApps() {
        this.loading = true;
        this.loadFailed = false;
        filter();
        final HashSet hashSet = new HashSet(this.selected);
        this.worker.execute(new Runnable() { // from class: io.github.fluidnotify.AppSelectionActivity$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                AppSelectionActivity.this.m11lambda$loadApps$11$iogithubfluidnotifyAppSelectionActivity(hashSet);
            }
        });
    }

    private void save() {
        AppSelection appSelection = new AppSelection(this.onlySelected, this.selected);
        AppSelectionSettings.save(this.prefs, appSelection);
        sendBroadcast(new Intent("io.github.fluidnotify.CONFIG_CHANGED").setPackage("com.android.systemui").addFlags(268435456).putExtras(AppSelectionSettings.bundle(appSelection)));
        Toast.makeText(this, "已保存应用上岛设置", 0).show();
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public TextView text(String str, int i, int i2) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(i);
        textView.setTextColor(i2);
        return textView;
    }

    private void updateSummary() {
        TextView textView = this.summary;
        if (textView == null) {
            return;
        }
        textView.setText(this.onlySelected ? this.selected.isEmpty() ? "未选择应用：保存后不转换任何普通通知" : "已选择 " + this.selected.size() + " 个应用 · 保存后生效" : "全部应用的普通通知上岛 · 已保留 " + this.selected.size() + " 个自定义选择");
    }

    /* renamed from: lambda$loadApps$10$io-github-fluidnotify-AppSelectionActivity, reason: not valid java name */
    /* synthetic */ void m10lambda$loadApps$10$iogithubfluidnotifyAppSelectionActivity(boolean z, List list) {
        if (isDestroyed() || isFinishing()) {
            return;
        }
        this.loading = false;
        this.loadFailed = z;
        this.apps.clear();
        if (!z) {
            this.apps.addAll(list);
        }
        filter();
    }

    /* renamed from: lambda$loadApps$11$io-github-fluidnotify-AppSelectionActivity, reason: not valid java name */
    /* synthetic */ void m11lambda$loadApps$11$iogithubfluidnotifyAppSelectionActivity(final Set set) {
        final ArrayList arrayList = new ArrayList();
        final boolean z = true;
        try {
            PackageManager packageManager = getPackageManager();
            HashSet hashSet = new HashSet();
            for (ApplicationInfo applicationInfo : packageManager.getInstalledApplications(0)) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                String str = applicationInfo.packageName;
                try {
                    str = applicationInfo.loadLabel(packageManager).toString();
                } catch (RuntimeException unused) {
                }
                arrayList.add(new App(applicationInfo.packageName, str, (applicationInfo.flags & 1) != 0, true));
                hashSet.add(applicationInfo.packageName);
            }
            Iterator it = set.iterator();
            while (it.hasNext()) {
                String str2 = (String) it.next();
                if (!hashSet.contains(str2)) {
                    arrayList.add(new App(str2, str2, false, false));
                }
            }
            final Collator collator = Collator.getInstance(Locale.getDefault());
            arrayList.sort(new Comparator() { // from class: io.github.fluidnotify.AppSelectionActivity$$ExternalSyntheticLambda0
                @Override // java.util.Comparator
                public final int compare(Object obj, Object obj2) {
                    return AppSelectionActivity.lambda$loadApps$9(set, collator, (AppSelectionActivity.App) obj, (AppSelectionActivity.App) obj2);
                }
            });
            z = false;
        } catch (RuntimeException unused2) {
        }
        this.ui.post(new Runnable() { // from class: io.github.fluidnotify.AppSelectionActivity$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                AppSelectionActivity.this.m10lambda$loadApps$10$iogithubfluidnotifyAppSelectionActivity(z, arrayList);
            }
        });
    }

    /* renamed from: lambda$onCreate$0$io-github-fluidnotify-AppSelectionActivity, reason: not valid java name */
    /* synthetic */ WindowInsets m12lambda$onCreate$0$iogithubfluidnotifyAppSelectionActivity(View view, WindowInsets windowInsets) {
        Insets insets = windowInsets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
        view.setPadding(insets.left + dp(20.0f), insets.top + dp(12.0f), insets.right + dp(20.0f), insets.bottom + dp(12.0f));
        return windowInsets;
    }

    /* renamed from: lambda$onCreate$1$io-github-fluidnotify-AppSelectionActivity, reason: not valid java name */
    /* synthetic */ void m13lambda$onCreate$1$iogithubfluidnotifyAppSelectionActivity(RadioButton radioButton, RadioGroup radioGroup, int i) {
        this.onlySelected = i == radioButton.getId();
        updateSummary();
    }

    /* renamed from: lambda$onCreate$2$io-github-fluidnotify-AppSelectionActivity, reason: not valid java name */
    /* synthetic */ void m14lambda$onCreate$2$iogithubfluidnotifyAppSelectionActivity(RadioGroup radioGroup, RadioButton radioButton, View view) {
        Iterator<App> it = this.apps.iterator();
        while (it.hasNext()) {
            this.selected.add(it.next().packageName);
        }
        radioGroup.check(radioButton.getId());
        filter();
    }

    /* renamed from: lambda$onCreate$3$io-github-fluidnotify-AppSelectionActivity, reason: not valid java name */
    /* synthetic */ void m15lambda$onCreate$3$iogithubfluidnotifyAppSelectionActivity(RadioGroup radioGroup, RadioButton radioButton, View view) {
        this.selected.clear();
        radioGroup.check(radioButton.getId());
        filter();
    }

    /* renamed from: lambda$onCreate$4$io-github-fluidnotify-AppSelectionActivity, reason: not valid java name */
    /* synthetic */ void m16lambda$onCreate$4$iogithubfluidnotifyAppSelectionActivity(RadioGroup radioGroup, RadioButton radioButton, AdapterView adapterView, View view, int i, long j) {
        String str = this.visible.get(i).packageName;
        if (!this.selected.remove(str)) {
            this.selected.add(str);
        }
        radioGroup.check(radioButton.getId());
        filter();
    }

    /* renamed from: lambda$onCreate$5$io-github-fluidnotify-AppSelectionActivity, reason: not valid java name */
    /* synthetic */ void m17lambda$onCreate$5$iogithubfluidnotifyAppSelectionActivity(View view) {
        if (this.loadFailed) {
            loadApps();
        }
    }

    /* renamed from: lambda$onCreate$6$io-github-fluidnotify-AppSelectionActivity, reason: not valid java name */
    /* synthetic */ void m18lambda$onCreate$6$iogithubfluidnotifyAppSelectionActivity(View view) {
        finish();
    }

    /* renamed from: lambda$onCreate$7$io-github-fluidnotify-AppSelectionActivity, reason: not valid java name */
    /* synthetic */ void m19lambda$onCreate$7$iogithubfluidnotifyAppSelectionActivity(View view) {
        save();
    }

    /* renamed from: lambda$onCreate$8$io-github-fluidnotify-AppSelectionActivity, reason: not valid java name */
    /* synthetic */ void m20lambda$onCreate$8$iogithubfluidnotifyAppSelectionActivity(CompoundButton compoundButton, boolean z) {
        filter();
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        SharedPreferences open = ConfigStore.open(this);
        this.prefs = open;
        AppSelection read = AppSelectionSettings.read(open);
        if (bundle != null) {
            read = AppSelectionSettings.fromBundle(bundle, read);
        }
        this.onlySelected = read.onlySelected;
        this.selected.addAll(read.packages);
        LinearLayout column = column();
        column.setBackground(new GlassStyle.Backdrop());
        column.setPadding(dp(20.0f), dp(12.0f), dp(20.0f), dp(12.0f));
        column.setFocusableInTouchMode(true);
        getWindow().setDecorFitsSystemWindows(false);
        column.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: io.github.fluidnotify.AppSelectionActivity$$ExternalSyntheticLambda4
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return AppSelectionActivity.this.m12lambda$onCreate$0$iogithubfluidnotifyAppSelectionActivity(view, windowInsets);
            }
        });
        TextView text = text("自定义应用上岛", 24, -15189184);
        text.setTypeface(Typeface.DEFAULT, 1);
        column.addView(text);
        TextView text2 = text("选择哪些应用的普通通知接入流体云。未选应用保留系统原生行为，常驻通知仍不转换。", 13, -10520708);
        text2.setPadding(0, dp(8.0f), 0, dp(8.0f));
        column.addView(text2);
        final RadioGroup radioGroup = new RadioGroup(this);
        RadioButton radioButton = new RadioButton(this);
        radioButton.setId(View.generateViewId());
        radioButton.setText("全部应用（默认）");
        final RadioButton radioButton2 = new RadioButton(this);
        radioButton2.setId(View.generateViewId());
        radioButton2.setText("仅选中应用");
        radioGroup.addView(radioButton);
        radioGroup.addView(radioButton2);
        radioGroup.check(this.onlySelected ? radioButton2.getId() : radioButton.getId());
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { // from class: io.github.fluidnotify.AppSelectionActivity$$ExternalSyntheticLambda5
            @Override // android.widget.RadioGroup.OnCheckedChangeListener
            public final void onCheckedChanged(RadioGroup radioGroup2, int i) {
                AppSelectionActivity.this.m13lambda$onCreate$1$iogithubfluidnotifyAppSelectionActivity(radioButton2, radioGroup2, i);
            }
        });
        column.addView(radioGroup);
        EditText editText = new EditText(this);
        this.search = editText;
        editText.setSingleLine(true);
        this.search.setTextSize(15.0f);
        this.search.setHint("搜索应用名称或包名");
        this.search.setContentDescription("搜索应用名称或包名");
        this.search.setMinHeight(dp(48.0f));
        column.addView(this.search, new LinearLayout.LayoutParams(-1, -2));
        CheckBox checkBox = new CheckBox(this);
        this.selectedFilter = checkBox;
        checkBox.setText("仅看已选应用");
        this.selectedFilter.setTextSize(13.0f);
        this.selectedFilter.setMinHeight(dp(48.0f));
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setGravity(16);
        linearLayout.addView(this.selectedFilter, new LinearLayout.LayoutParams(0, -2, 1.0f));
        Button button = button("全选", false);
        this.selectAll = button;
        button.setContentDescription("选择全部已加载应用，不受搜索条件限制");
        this.selectAll.setEnabled(false);
        this.selectAll.setOnClickListener(new View.OnClickListener() { // from class: io.github.fluidnotify.AppSelectionActivity$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AppSelectionActivity.this.m14lambda$onCreate$2$iogithubfluidnotifyAppSelectionActivity(radioGroup, radioButton2, view);
            }
        });
        linearLayout.addView(this.selectAll);
        Button button2 = button("清空", false);
        button2.setContentDescription("清空所有已选应用");
        button2.setOnClickListener(new View.OnClickListener() { // from class: io.github.fluidnotify.AppSelectionActivity$$ExternalSyntheticLambda7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AppSelectionActivity.this.m15lambda$onCreate$3$iogithubfluidnotifyAppSelectionActivity(radioGroup, radioButton2, view);
            }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.leftMargin = dp(6.0f);
        linearLayout.addView(button2, layoutParams);
        column.addView(linearLayout);
        TextView text3 = text("", 13, -15303564);
        this.summary = text3;
        text3.setPadding(0, dp(4.0f), 0, dp(8.0f));
        this.summary.setAccessibilityLiveRegion(1);
        column.addView(this.summary);
        FrameLayout frameLayout = new FrameLayout(this);
        ListView listView = new ListView(this);
        listView.setDividerHeight(0);
        AppAdapter appAdapter = new AppAdapter();
        this.adapter = appAdapter;
        listView.setAdapter((ListAdapter) appAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: io.github.fluidnotify.AppSelectionActivity$$ExternalSyntheticLambda8
            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                AppSelectionActivity.this.m16lambda$onCreate$4$iogithubfluidnotifyAppSelectionActivity(radioGroup, radioButton2, adapterView, view, i, j);
            }
        });
        frameLayout.addView(listView, new FrameLayout.LayoutParams(-1, -1));
        TextView text4 = text("正在读取应用列表…", 14, -10520708);
        this.empty = text4;
        text4.setGravity(17);
        this.empty.setOnClickListener(new View.OnClickListener() { // from class: io.github.fluidnotify.AppSelectionActivity$$ExternalSyntheticLambda9
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AppSelectionActivity.this.m17lambda$onCreate$5$iogithubfluidnotifyAppSelectionActivity(view);
            }
        });
        frameLayout.addView(this.empty, new FrameLayout.LayoutParams(-1, -1));
        listView.setEmptyView(this.empty);
        column.addView(frameLayout, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        LinearLayout linearLayout2 = new LinearLayout(this);
        Button button3 = button("取消", false);
        button3.setOnClickListener(new View.OnClickListener() { // from class: io.github.fluidnotify.AppSelectionActivity$$ExternalSyntheticLambda10
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AppSelectionActivity.this.m18lambda$onCreate$6$iogithubfluidnotifyAppSelectionActivity(view);
            }
        });
        linearLayout2.addView(button3, new LinearLayout.LayoutParams(0, -2, 1.0f));
        Button button4 = button("保存并应用", true);
        button4.setOnClickListener(new View.OnClickListener() { // from class: io.github.fluidnotify.AppSelectionActivity$$ExternalSyntheticLambda11
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AppSelectionActivity.this.m19lambda$onCreate$7$iogithubfluidnotifyAppSelectionActivity(view);
            }
        });
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, -2, 1.0f);
        layoutParams2.leftMargin = dp(12.0f);
        linearLayout2.addView(button4, layoutParams2);
        column.addView(linearLayout2);
        setContentView(column);
        this.selectedFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: io.github.fluidnotify.AppSelectionActivity$$ExternalSyntheticLambda1
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                AppSelectionActivity.this.m20lambda$onCreate$8$iogithubfluidnotifyAppSelectionActivity(compoundButton, z);
            }
        });
        this.search.addTextChangedListener(new TextWatcher() { // from class: io.github.fluidnotify.AppSelectionActivity.1
            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
            }

            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                AppSelectionActivity.this.filter();
            }
        });
        if (bundle != null) {
            this.search.setText(bundle.getString("query", ""));
            this.selectedFilter.setChecked(bundle.getBoolean("selected_filter", false));
        }
        updateSummary();
        loadApps();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        this.worker.shutdownNow();
        this.ui.removeCallbacksAndMessages(null);
        this.icons.evictAll();
        super.onDestroy();
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putAll(AppSelectionSettings.bundle(new AppSelection(this.onlySelected, this.selected)));
        bundle.putString("query", this.search.getText().toString());
        bundle.putBoolean("selected_filter", this.selectedFilter.isChecked());
        super.onSaveInstanceState(bundle);
    }
}
