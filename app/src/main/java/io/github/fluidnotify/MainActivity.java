package io.github.fluidnotify;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import io.github.fluidnotify.GlassStyle;

/* loaded from: classes.dex */
public class MainActivity extends Activity {
    private static final String CHANNEL = "fluid_hook_test";
    private TextView appSelectionSummary;
    private boolean awaitingTest;
    private LinearLayout content;
    private EditText durationInput;
    private EditText expandInput;
    private String pendingTest;
    private SharedPreferences prefs;
    private SwipePreview preview;
    private ScrollView scroll;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Runnable syncSwipe = new Runnable() { // from class: io.github.fluidnotify.MainActivity$$ExternalSyntheticLambda2
        @Override // java.lang.Runnable
        public final void run() {
            MainActivity.this.m68lambda$new$0$iogithubfluidnotifyMainActivity();
        }
    };

    private void addCard(LinearLayout linearLayout) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.bottomMargin = dp(14.0f);
        this.content.addView(linearLayout, layoutParams);
    }

    private void addSlider(LinearLayout linearLayout, String str, int i, final int i2, int i3, final boolean z) {
        LinearLayout row = row();
        row.setPadding(0, dp(8.0f), 0, dp(2.0f));
        row.addView(title(str, 16), new LinearLayout.LayoutParams(0, -2, 1.0f));
        final TextView title = title(i + "%", 19);
        title.setTextColor(-15303564);
        row.addView(title);
        linearLayout.addView(row);
        final TextView text = text(sliderHint(z, i), 12, -10520708);
        linearLayout.addView(text);
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(i3 - i2);
        seekBar.setProgress(i - i2);
        seekBar.setProgressTintList(ColorStateList.valueOf(-15303564));
        seekBar.setProgressBackgroundTintList(ColorStateList.valueOf(-2299672));
        seekBar.setThumbTintList(ColorStateList.valueOf(-15303564));
        seekBar.setContentDescription(str + "，" + i2 + "%到" + i3 + "%");
        linearLayout.addView(seekBar, new LinearLayout.LayoutParams(-1, dp(48.0f)));
        LinearLayout row2 = row();
        row2.setPadding(dp(5.0f), 0, dp(5.0f), dp(16.0f));
        row2.addView(text(i2 + "%", 11, -10520708), new LinearLayout.LayoutParams(0, -2, 1.0f));
        row2.addView(text(i3 + "%", 11, -10520708));
        linearLayout.addView(row2);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: io.github.fluidnotify.MainActivity.1
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar2, int i4, boolean z2) {
                int i5 = i4 + i2;
                title.setText(MainActivity.this.getString(R.string.percent_value, new Object[]{Integer.valueOf(i5)}));
                text.setText(MainActivity.this.sliderHint(z, i5));
                if (z) {
                    MainActivity.this.preview.setBoundary(i5);
                } else {
                    MainActivity.this.preview.setDistance(i5);
                }
                if (z2) {
                    if (z) {
                        SwipeConfig.set(i5);
                    } else {
                        SwipeConfig.setDistance(i5);
                    }
                    MainActivity.this.prefs.edit().putInt(z ? "swipe_active_edge" : "swipe_dismiss_distance", i5).apply();
                    if (MainActivity.this.ui.hasCallbacks(MainActivity.this.syncSwipe)) {
                        return;
                    }
                    MainActivity.this.ui.postDelayed(MainActivity.this.syncSwipe, 64L);
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar2) {
                if (z) {
                    MainActivity.this.preview.setBoundary(seekBar2.getProgress() + i2);
                } else {
                    MainActivity.this.preview.setDistance(seekBar2.getProgress() + i2);
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar2) {
                MainActivity.this.ui.removeCallbacks(MainActivity.this.syncSwipe);
                MainActivity.this.syncSwipe.run();
            }
        });
    }

    private void buildAppSelection() {
        LinearLayout card = card(false);
        card.addView(title("自定义应用上岛", 17));
        card.addView(text("全部应用或仅选中的应用，由你决定。", 13, -10520708));
        TextView text = text("", 13, -15303564);
        this.appSelectionSummary = text;
        text.setPadding(0, dp(12.0f), 0, dp(8.0f));
        card.addView(this.appSelectionSummary);
        Button button = button("选择上岛应用  ›", false);
        button.setOnClickListener(new View.OnClickListener() { // from class: io.github.fluidnotify.MainActivity$$ExternalSyntheticLambda7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m60lambda$buildAppSelection$3$iogithubfluidnotifyMainActivity(view);
            }
        });
        card.addView(button, new LinearLayout.LayoutParams(-1, -2));
        addCard(card);
        updateAppSelectionSummary();
    }

    private void buildGuide() {
        final LinearLayout card = card(false);
        final Button button = button("使用指南与说明  ＋", false);
        button.setGravity(8388627);
        card.addView(button, new LinearLayout.LayoutParams(-1, -2));
        final TextView text = text("01  在 LSPosed 中启用本模块。\n02  作用域只选择「系统界面」。\n03  安装或升级后重启系统界面或手机。\n\n普通消息显示为原生胶囊，点击展开卡片；下拉通知栏仍保留原通知。USB、VPN、后台服务等常驻通知不转换。\n\n此处开关表示保存的设置，不代表 LSPosed 模块实际激活状态。", 13, -10520708);
        text.setPadding(dp(4.0f), dp(14.0f), dp(4.0f), dp(4.0f));
        text.setVisibility(8);
        card.addView(text);
        button.setOnClickListener(new View.OnClickListener() { // from class: io.github.fluidnotify.MainActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.lambda$buildGuide$10(text, card, button, view);
            }
        });
        addCard(card);
    }

    private void buildHeader() {
        LinearLayout row = row();
        LinearLayout column = column();
        TextView text = text("FLUID NOTIFY", 11, -15303564);
        text.setLetterSpacing(0.2f);
        column.addView(text);
        column.addView(title("通知流体云", 30));
        row.addView(column, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView text2 = text("浅色 · 玻璃", 11, -15303564);
        text2.setPadding(dp(12.0f), dp(8.0f), dp(12.0f), dp(8.0f));
        text2.setBackground(GlassStyle.surface(this, false));
        row.addView(text2);
        this.content.addView(row);
        TextView text3 = text("让每一条消息，轻盈浮现。", 14, -10520708);
        text3.setPadding(0, dp(8.0f), 0, dp(22.0f));
        this.content.addView(text3);
    }

    private void buildHero() {
        LinearLayout card = card(true);
        LinearLayout row = row();
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.ic_cloud);
        imageView.setImageTintList(ColorStateList.valueOf(-15303564));
        imageView.setPadding(dp(11.0f), dp(11.0f), dp(11.0f), dp(11.0f));
        imageView.setBackground(GlassStyle.rounded(this, -855638017, 18));
        row.addView(imageView, new LinearLayout.LayoutParams(dp(48.0f), dp(48.0f)));
        LinearLayout column = column();
        column.setPadding(dp(12.0f), 0, 0, 0);
        column.addView(title("消息，恰好在眼前", 19));
        column.addView(text("原生胶囊 · 轻触展开 · 滑动清除", 12, -10520708));
        row.addView(column, new LinearLayout.LayoutParams(0, -2, 1.0f));
        card.addView(row);
        final TextView text = text("●   通知流体云    一切，轻盈有序", 13, -15189184);
        text.setGravity(17);
        text.setPadding(dp(10.0f), dp(16.0f), dp(10.0f), dp(16.0f));
        text.setBackground(GlassStyle.surface(this, false));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(dp(4.0f), dp(24.0f), dp(4.0f), dp(22.0f));
        card.addView(text, layoutParams);
        divider(card);
        LinearLayout row2 = row();
        row2.setPadding(0, dp(16.0f), 0, 0);
        LinearLayout column2 = column();
        column2.addView(title("接入普通通知", 16));
        final TextView text2 = text("", 12, -10520708);
        text2.setAccessibilityLiveRegion(1);
        column2.addView(text2);
        row2.addView(column2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        Switch r5 = new Switch(this);
        r5.setContentDescription("将配置范围内的应用普通消息接入流体云");
        r5.setMinHeight(dp(48.0f));
        r5.setSwitchMinWidth(dp(50.0f));
        r5.setThumbTintList(ColorStateList.valueOf(-1));
        r5.setTrackTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked}, new int[0]}, new int[]{-15303564, -4666933}));
        r5.setChecked(this.prefs.getBoolean("enabled", true));
        text2.setText(r5.isChecked() ? "已开启 · 保存后即时同步" : "已暂停 · 使用系统默认显示");
        r5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: io.github.fluidnotify.MainActivity$$ExternalSyntheticLambda4
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                MainActivity.this.m61lambda$buildHero$2$iogithubfluidnotifyMainActivity(text2, text, compoundButton, z);
            }
        });
        text.setAlpha(r5.isChecked() ? 1.0f : 0.45f);
        row2.addView(r5);
        card.addView(row2);
        addCard(card);
    }

    private void buildNotificationSettings(Bundle bundle) {
        section("通知设置", "01");
        buildAppSelection();
        buildTimeSetting(bundle, true);
        buildTimeSetting(bundle, false);
    }

    private void buildPullDownSetting() {
        LinearLayout card = card(false);
        LinearLayout row = row();
        LinearLayout column = column();
        column.addView(title("下拉以小窗打开", 17));
        final TextView text = text("", 12, -10520708);
        text.setAccessibilityLiveRegion(1);
        column.addView(text);
        row.addView(column, new LinearLayout.LayoutParams(0, -2, 1.0f));
        Switch r3 = new Switch(this);
        r3.setId(R.id.pull_down_small_window);
        r3.setContentDescription("下拉展开的流体云，以小窗打开应用");
        r3.setMinHeight(dp(48.0f));
        r3.setSwitchMinWidth(dp(50.0f));
        r3.setThumbTintList(ColorStateList.valueOf(-1));
        r3.setTrackTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked}, new int[0]}, new int[]{-15303564, -4666933}));
        r3.setChecked(this.prefs.getBoolean("pull_down_small_window", false));
        text.setText(r3.isChecked() ? "已开启 · 下拉底部小白条" : "已关闭 · 保留浮窗回复按钮");
        r3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: io.github.fluidnotify.MainActivity$$ExternalSyntheticLambda8
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                MainActivity.this.m62lambda$buildPullDownSetting$6$iogithubfluidnotifyMainActivity(text, compoundButton, z);
            }
        });
        row.addView(r3);
        card.addView(row);
        TextView text2 = text("开启后，展开卡片中的「浮窗回复」替换为底部小白条。向下拖动小白条并松手，以小窗打开应用；短拉或轻点不会打开。仅对支持小窗且有打开入口的应用生效。", 13, -10520708);
        text2.setPadding(0, dp(10.0f), 0, 0);
        card.addView(text2);
        addCard(card);
    }

    private void buildSwipeSettings() {
        section("手势与交互", "02");
        buildPullDownSetting();
        LinearLayout card = card(false);
        int edgePercent = SwipeRules.edgePercent(this.prefs.getInt("swipe_active_edge", 15));
        int dismissPercent = SwipeRules.dismissPercent(this.prefs.getInt("swipe_dismiss_distance", 50));
        SwipeConfig.set(edgePercent);
        SwipeConfig.setDistance(dismissPercent);
        SwipePreview swipePreview = new SwipePreview(this);
        this.preview = swipePreview;
        swipePreview.setBoundary(edgePercent);
        this.preview.setDistance(dismissPercent);
        addSlider(card, "左右边缘起手范围", edgePercent, 0, 50, true);
        divider(card);
        addSlider(card, "滑动清除距离", dismissPercent, 20, 60, false);
        TextView text = text("液态玻璃 · 通知预览", 12, -15303564);
        text.setPadding(0, dp(8.0f), 0, dp(10.0f));
        card.addView(text);
        card.addView(this.preview, new LinearLayout.LayoutParams(-1, dp(304.0f)));
        TextView text2 = text("蓝色细边标记起手范围，下方红色刻度表示清除距离。短滑回弹，达到阈值后松手清除。", 12, -10520708);
        text2.setPadding(0, dp(12.0f), 0, 0);
        card.addView(text2);
        addCard(card);
    }

    private void buildTests() {
        int i;
        section("通知实验室", "03");
        LinearLayout card = card(false);
        card.addView(title("发一条消息，感受一下", 17));
        card.addView(text("发送真实测试通知，查看原生流体云效果。仅选中应用模式下，需选中「通知流体云」才能测试上岛。", 13, -10520708));
        Button button = button("发送普通测试通知  ↗", true);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, dp(16.0f), 0, dp(8.0f));
        card.addView(button, layoutParams);
        button.setOnClickListener(new View.OnClickListener() { // from class: io.github.fluidnotify.MainActivity$$ExternalSyntheticLambda9
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m63lambda$buildTests$7$iogithubfluidnotifyMainActivity(view);
            }
        });
        String[] strArr = {"long", "chat", "progress", "emoji", "batch", "ongoing"};
        String[] strArr2 = {"长文本", "聊天消息", "下载进度", "表情通知", "8 张展开卡片", "常驻通知"};
        int i2 = 0;
        for (int i3 = 6; i2 < i3; i3 = 6) {
            LinearLayout row = row();
            int i4 = i2;
            while (true) {
                i = i2 + 2;
                if (i4 < i) {
                    final String str = strArr[i4];
                    Button button2 = button(strArr2[i4], false);
                    button2.setOnClickListener(new View.OnClickListener() { // from class: io.github.fluidnotify.MainActivity$$ExternalSyntheticLambda10
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            MainActivity.this.m64lambda$buildTests$8$iogithubfluidnotifyMainActivity(str, view);
                        }
                    });
                    LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, -2, 1.0f);
                    layoutParams2.setMargins(i4 == i2 ? 0 : dp(4.0f), dp(4.0f), i4 == i2 ? dp(4.0f) : 0, dp(4.0f));
                    row.addView(button2, layoutParams2);
                    i4++;
                }
            }
            card.addView(row);
            i2 = i;
        }
        Button button3 = button("撤销测试通知", false);
        button3.setTextColor(-10520708);
        button3.setOnClickListener(new View.OnClickListener() { // from class: io.github.fluidnotify.MainActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m65lambda$buildTests$9$iogithubfluidnotifyMainActivity(view);
            }
        });
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.topMargin = dp(12.0f);
        card.addView(button3, layoutParams3);
        addCard(card);
    }

    private void buildTimeSetting(Bundle bundle, final boolean z) {
        String valueOf;
        LinearLayout card = card(false);
        card.addView(title(z ? "通知自动展开时长" : "通知缩小时间", 17));
        card.addView(text(z ? "收到通知先展开卡片，到时收起为应用图标和滚动文字胶囊。" : "文字胶囊停留后，按原生动效缩成小括号。", 13, -10520708));
        LinearLayout row = row();
        row.setPadding(0, dp(16.0f), 0, dp(10.0f));
        LinearLayout row2 = row();
        row2.setPadding(dp(14.0f), 0, dp(14.0f), 0);
        row2.setBackground(GlassStyle.rounded(this, -1379598, 16));
        final EditText editText = new EditText(this);
        if (z) {
            this.expandInput = editText;
        } else {
            this.durationInput = editText;
        }
        String str = z ? "notification_auto_expand_seconds" : "notification_shrink_seconds";
        editText.setId(z ? 102 : 101);
        editText.setSingleLine(true);
        editText.setTextSize(25.0f);
        editText.setTextColor(-15189184);
        editText.setBackground(null);
        editText.setSelectAllOnFocus(true);
        editText.setInputType(2);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        editText.setImeOptions(6);
        if (bundle != null) {
            valueOf = bundle.getString(z ? "expandDraft" : "durationDraft", String.valueOf(DurationConfig.normalize(this.prefs.getInt(str, 0))));
        } else {
            valueOf = String.valueOf(DurationConfig.normalize(this.prefs.getInt(str, 0)));
        }
        editText.setText(valueOf);
        editText.setContentDescription(z ? "通知自动展开时长，0到999秒，0为关闭自动展开" : "通知缩小时间，0到999秒，0为官方默认时间");
        row2.addView(editText, new LinearLayout.LayoutParams(0, dp(56.0f), 1.0f));
        row2.addView(text("秒", 13, -10520708));
        row.addView(row2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        Button button = button("保存时间", true);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, dp(56.0f));
        layoutParams.setMarginStart(dp(12.0f));
        row.addView(button, layoutParams);
        card.addView(row);
        final TextView text = text(z ? "0 秒关闭自动展开 · 可设置 0–999 秒" : "0 秒使用系统默认时间 · 可设置 0–999 秒", 12, -10520708);
        text.setAccessibilityLiveRegion(1);
        card.addView(text);
        button.setOnClickListener(new View.OnClickListener() { // from class: io.github.fluidnotify.MainActivity$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m66lambda$buildTimeSetting$4$iogithubfluidnotifyMainActivity(editText, text, z, view);
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() { // from class: io.github.fluidnotify.MainActivity$$ExternalSyntheticLambda6
            @Override // android.widget.TextView.OnEditorActionListener
            public final boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return MainActivity.this.m67lambda$buildTimeSetting$5$iogithubfluidnotifyMainActivity(editText, text, z, textView, i, keyEvent);
            }
        });
        addCard(card);
    }

    private Button button(String str, boolean z) {
        Button button = new Button(this);
        button.setText(str);
        button.setTextSize(13.0f);
        button.setAllCaps(false);
        button.setTypeface(Typeface.create("sans-serif-medium", 0));
        button.setTextColor(z ? -1 : -15303564);
        button.setMinHeight(dp(50.0f));
        button.setMinimumHeight(dp(50.0f));
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(dp(14.0f), dp(10.0f), dp(14.0f), dp(10.0f));
        GlassStyle.pressable(button, z ? -15303564 : -1379342, 16);
        return button;
    }

    private LinearLayout card(boolean z) {
        LinearLayout column = column();
        column.setPadding(dp(20.0f), dp(20.0f), dp(20.0f), dp(20.0f));
        column.setBackground(GlassStyle.surface(this, z));
        column.setElevation(dp(2.0f));
        column.setOutlineAmbientShadowColor(-9791855);
        column.setOutlineSpotShadowColor(-9791855);
        return column;
    }

    private LinearLayout column() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        return linearLayout;
    }

    private Intent configIntent() {
        return new Intent("io.github.fluidnotify.CONFIG_CHANGED").setPackage("com.android.systemui").addFlags(268435456);
    }

    private void divider(LinearLayout linearLayout) {
        View view = new View(this);
        view.setBackgroundColor(407991925);
        linearLayout.addView(view, new LinearLayout.LayoutParams(-1, dp(1.0f)));
    }

    private int dp(float f) {
        return GlassStyle.dp(this, f);
    }

    private void handleIntent(Intent intent) {
        if (intent.getBooleanExtra("clear", false)) {
            ((NotificationManager) getSystemService(NotificationManager.class)).cancelAll();
        }
        if (intent.getBooleanExtra("test", false)) {
            sendTest(intent.getStringExtra("style"));
        }
        intent.removeExtra("clear");
        intent.removeExtra("test");
    }

    static /* synthetic */ void lambda$buildGuide$10(TextView textView, LinearLayout linearLayout, Button button, View view) {
        boolean z = textView.getVisibility() != 0;
        if (ValueAnimator.areAnimatorsEnabled()) {
            AutoTransition autoTransition = new AutoTransition();
            autoTransition.setDuration(240L);
            autoTransition.setInterpolator((TimeInterpolator) GlassStyle.EASE);
            TransitionManager.beginDelayedTransition(linearLayout, autoTransition);
        }
        textView.setVisibility(z ? 0 : 8);
        button.setText(z ? "使用指南与说明  −" : "使用指南与说明  ＋");
    }

    static /* synthetic */ WindowInsets lambda$onCreate$1(View view, WindowInsets windowInsets) {
        Insets insets = windowInsets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
        view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        return windowInsets;
    }

    private LinearLayout row() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setGravity(16);
        return linearLayout;
    }

    private void saveTime(EditText editText, TextView textView, boolean z) {
        try {
            int parseInt = Integer.parseInt(editText.getText().toString().trim());
            if (parseInt < 0 || parseInt > 999) {
                editText.setError("请输入 0–999 秒");
                return;
            }
            editText.setError(null);
            String str = z ? "notification_auto_expand_seconds" : "notification_shrink_seconds";
            this.prefs.edit().putInt(str, parseInt).apply();
            if (z) {
                AutoExpandConfig.set(parseInt);
            } else {
                DurationConfig.set(parseInt);
            }
            sendBroadcast(configIntent().putExtra(str, parseInt));
            editText.clearFocus();
            ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(editText.getWindowToken(), 0);
            textView.setText(z ? parseInt == 0 ? "已保存 · 已关闭自动展开" : "已保存 · 展开 " + parseInt + " 秒后收起为文字胶囊" : parseInt == 0 ? "已保存 · 使用系统默认时间" : "已保存 · " + parseInt + " 秒后缩小");
            textView.setTextColor(-15303564);
            GlassStyle.enter(textView, 0L);
        } catch (NumberFormatException unused) {
            editText.setError("请输入 0–999 秒");
        }
    }

    private void section(String str, String str2) {
        LinearLayout row = row();
        row.setPadding(dp(4.0f), dp(12.0f), dp(4.0f), dp(12.0f));
        row.addView(title(str, 18), new LinearLayout.LayoutParams(0, -2, 1.0f));
        row.addView(text(str2, 12, -10520708));
        this.content.addView(row);
    }

    private void sendTest(String str) {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
            this.pendingTest = str;
            if (this.awaitingTest) {
                return;
            }
            this.awaitingTest = true;
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
            return;
        }
        if (!"batch".equals(str)) {
            sendTest(str, 0);
            return;
        }
        for (int i = 1; i <= 8; i++) {
            sendTest("batchItem", i + 2000);
        }
    }

    private void sendTest(String str, int i) {
        int i2;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(new NotificationChannel("fluid_hook_test_high", "流体云 Hook 测试", 4));
        Notification.Builder autoCancel = new Notification.Builder(this, "fluid_hook_test_high").setSmallIcon(R.drawable.ic_cloud).setContentTitle("普通通知已接入流体云").setContentText("这是 LSP Hook 测试，点击可返回模块设置").setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, (Class<?>) MainActivity.class), 201326592)).setAutoCancel(true);
        if ("long".equals(str)) {
            autoCancel.setContentTitle("长文本通知流体云测试").setStyle(new Notification.BigTextStyle().bigText("这是一条长文本普通通知，用于验证应用图标、横向滚动和保持收起的状态栏胶囊。"));
            i2 = 1002;
        } else if ("chat".equals(str)) {
            autoCancel.setStyle(new Notification.MessagingStyle(new Person.Builder().setName("本机").build()).addMessage("聊天通知流体云测试", System.currentTimeMillis(), new Person.Builder().setName("测试联系人").build()));
            i2 = 1003;
        } else if ("progress".equals(str)) {
            autoCancel.setContentTitle("下载进度流体云测试").setContentText("正在下载 · 50%").setProgress(100, 50, false);
            i2 = 1004;
        } else if ("ongoing".equals(str)) {
            autoCancel.setContentTitle("常驻通知过滤测试").setContentText("这条通知应只在原生通知栏显示").setOngoing(true);
            i2 = 1005;
        } else if ("emoji".equals(str)) {
            autoCancel.setContentTitle("😀❤️👍🏽").setContentText("👨\u200d👩\u200d👧\u200d👦🇨🇳🎉 中文表情混排，完整循环滚动。");
            i2 = 1006;
        } else {
            i2 = 1001;
        }
        if (i > 0) {
            int i3 = i - 2000;
            autoCancel.setContentTitle("展开卡片测试 " + i3).setContentText("第 " + i3 + " 张普通通知，展开列表不限制四张。");
            i2 = i;
        }
        notificationManager.notify(i2, autoCancel.build());
        if (i == 0 || i == 2008) {
            Toast.makeText(this, i == 2008 ? "已发送 8 条测试通知" : "测试通知已发送", 0).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String sliderHint(boolean z, int i) {
        return !z ? "滑过卡片宽度的 " + i + "% 后松手清除" : i == 0 ? "已关闭左右滑动清除" : i == 50 ? "整张卡片都可以起手滑动" : "左右各 " + i + "% 可起手，中间区域不响应";
    }

    private TextView text(String str, int i, int i2) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(i);
        textView.setTextColor(i2);
        textView.setFontFeatureSettings("kern");
        textView.setLineSpacing(dp(3.0f), 1.0f);
        return textView;
    }

    private TextView title(String str, int i) {
        TextView text = text(str, i, -15189184);
        text.setTypeface(Typeface.create("sans-serif-medium", 0));
        return text;
    }

    private void updateAppSelectionSummary() {
        if (this.appSelectionSummary == null) {
            return;
        }
        AppSelection read = AppSelectionSettings.read(this.prefs);
        this.appSelectionSummary.setText(!read.onlySelected ? "当前：全部应用" : read.packages.isEmpty() ? "当前：仅选中应用 · 暂未选择，不转换普通通知" : "当前：仅选中的 " + read.packages.size() + " 个应用");
    }

    /* renamed from: lambda$buildAppSelection$3$io-github-fluidnotify-MainActivity, reason: not valid java name */
    /* synthetic */ void m60lambda$buildAppSelection$3$iogithubfluidnotifyMainActivity(View view) {
        startActivity(new Intent(this, (Class<?>) AppSelectionActivity.class));
    }

    /* renamed from: lambda$buildHero$2$io-github-fluidnotify-MainActivity, reason: not valid java name */
    /* synthetic */ void m61lambda$buildHero$2$iogithubfluidnotifyMainActivity(TextView textView, TextView textView2, CompoundButton compoundButton, boolean z) {
        this.prefs.edit().putBoolean("enabled", z).apply();
        sendBroadcast(configIntent().putExtra("enabled", z));
        textView.setText(z ? "已开启 · 保存后即时同步" : "已暂停 · 使用系统默认显示");
        textView2.animate().cancel();
        textView2.animate().alpha(z ? 1.0f : 0.45f).setDuration(220L).setStartDelay(0L).start();
    }

    /* renamed from: lambda$buildPullDownSetting$6$io-github-fluidnotify-MainActivity, reason: not valid java name */
    /* synthetic */ void m62lambda$buildPullDownSetting$6$iogithubfluidnotifyMainActivity(TextView textView, CompoundButton compoundButton, boolean z) {
        this.prefs.edit().putBoolean("pull_down_small_window", z).apply();
        sendBroadcast(configIntent().putExtra("pull_down_small_window", z));
        textView.setText(z ? "已开启 · 下拉底部小白条" : "已关闭 · 保留浮窗回复按钮");
    }

    /* renamed from: lambda$buildTests$7$io-github-fluidnotify-MainActivity, reason: not valid java name */
    /* synthetic */ void m63lambda$buildTests$7$iogithubfluidnotifyMainActivity(View view) {
        sendTest(null);
    }

    /* renamed from: lambda$buildTests$8$io-github-fluidnotify-MainActivity, reason: not valid java name */
    /* synthetic */ void m64lambda$buildTests$8$iogithubfluidnotifyMainActivity(String str, View view) {
        sendTest(str);
    }

    /* renamed from: lambda$buildTests$9$io-github-fluidnotify-MainActivity, reason: not valid java name */
    /* synthetic */ void m65lambda$buildTests$9$iogithubfluidnotifyMainActivity(View view) {
        ((NotificationManager) getSystemService(NotificationManager.class)).cancelAll();
        Toast.makeText(this, "已撤销本模块的测试通知", 0).show();
    }

    /* renamed from: lambda$buildTimeSetting$4$io-github-fluidnotify-MainActivity, reason: not valid java name */
    /* synthetic */ void m66lambda$buildTimeSetting$4$iogithubfluidnotifyMainActivity(EditText editText, TextView textView, boolean z, View view) {
        saveTime(editText, textView, z);
    }

    /* renamed from: lambda$buildTimeSetting$5$io-github-fluidnotify-MainActivity, reason: not valid java name */
    /* synthetic */ boolean m67lambda$buildTimeSetting$5$iogithubfluidnotifyMainActivity(EditText editText, TextView textView, boolean z, TextView textView2, int i, KeyEvent keyEvent) {
        if (i != 6) {
            return false;
        }
        saveTime(editText, textView, z);
        return true;
    }

    /* renamed from: lambda$new$0$io-github-fluidnotify-MainActivity, reason: not valid java name */
    /* synthetic */ void m68lambda$new$0$iogithubfluidnotifyMainActivity() {
        if (this.prefs == null) {
            return;
        }
        sendBroadcast(configIntent().putExtra("swipe_active_edge", this.prefs.getInt("swipe_active_edge", 15)).putExtra("swipe_dismiss_distance", this.prefs.getInt("swipe_dismiss_distance", 50)));
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.prefs = ConfigStore.open(this);
        getWindow().setStatusBarColor(0);
        getWindow().setNavigationBarColor(0);
        getWindow().setNavigationBarContrastEnforced(false);
        getWindow().setDecorFitsSystemWindows(false);
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackground(new GlassStyle.Backdrop());
        frameLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: io.github.fluidnotify.MainActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return MainActivity.lambda$onCreate$1(view, windowInsets);
            }
        });
        ScrollView scrollView = new ScrollView(this);
        this.scroll = scrollView;
        scrollView.setId(R.id.settings_scroll);
        this.scroll.setFillViewport(true);
        this.scroll.setClipToPadding(false);
        this.scroll.setVerticalScrollBarEnabled(false);
        LinearLayout column = column();
        this.content = column;
        column.setPadding(dp(22.0f), dp(22.0f), dp(22.0f), dp(30.0f));
        this.content.setFocusableInTouchMode(true);
        this.scroll.addView(this.content);
        frameLayout.addView(this.scroll);
        buildHeader();
        buildHero();
        buildNotificationSettings(bundle);
        buildSwipeSettings();
        buildTests();
        buildGuide();
        TextView text = text("通知流体云  /  ColorOS 16 · 17", 12, -10520708);
        text.setGravity(17);
        text.setPadding(0, dp(22.0f), 0, 0);
        this.content.addView(text);
        setContentView(frameLayout);
        WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null) {
            insetsController.setSystemBarsAppearance(24, 24);
        }
        frameLayout.requestApplyInsets();
        if (bundle != null) {
            this.pendingTest = bundle.getString("pendingTest");
            this.awaitingTest = bundle.getBoolean("awaitingTest");
        } else {
            for (int i = 0; i < Math.min(4, this.content.getChildCount()); i++) {
                GlassStyle.enter(this.content.getChildAt(i), i * 55);
            }
            handleIntent(getIntent());
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        this.ui.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override // android.app.Activity
    protected void onPause() {
        this.ui.removeCallbacks(this.syncSwipe);
        this.syncSwipe.run();
        for (int i = 0; i < this.content.getChildCount(); i++) {
            View childAt = this.content.getChildAt(i);
            childAt.animate().cancel();
            childAt.setAlpha(1.0f);
            childAt.setTranslationY(0.0f);
        }
        this.preview.stopMotion();
        super.onPause();
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 1 && this.awaitingTest) {
            this.awaitingTest = false;
            if (iArr.length <= 0 || iArr[0] != 0) {
                Toast.makeText(this, "允许通知权限后即可发送测试通知", 0).show();
            } else {
                sendTest(this.pendingTest);
            }
        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        sendBroadcast(configIntent().putExtra("enabled", this.prefs.getBoolean("enabled", true)).putExtra("pull_down_small_window", this.prefs.getBoolean("pull_down_small_window", false)).putExtras(AppSelectionSettings.bundle(AppSelectionSettings.read(this.prefs))).putExtra("notification_shrink_seconds", DurationConfig.normalize(this.prefs.getInt("notification_shrink_seconds", 0))).putExtra("notification_auto_expand_seconds", AutoExpandConfig.normalize(this.prefs.getInt("notification_auto_expand_seconds", 0))).putExtra("swipe_active_edge", SwipeRules.edgePercent(this.prefs.getInt("swipe_active_edge", 15))).putExtra("swipe_dismiss_distance", SwipeRules.dismissPercent(this.prefs.getInt("swipe_dismiss_distance", 50))));
        updateAppSelectionSummary();
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putString("durationDraft", this.durationInput.getText().toString());
        bundle.putString("expandDraft", this.expandInput.getText().toString());
        bundle.putString("pendingTest", this.pendingTest);
        bundle.putBoolean("awaitingTest", this.awaitingTest);
        super.onSaveInstanceState(bundle);
    }
}
