# 通知流体云 · FluidNotify

把普通通知接入 **ColorOS 原生流体云**（灵动胶囊）的 LSPosed 模块，适配 ColorOS 16 / 17。

- 包名：`io.github.fluidnotify`
- 版本：`0.3.20`（versionCode 27）
- 作用域：只 Hook `com.android.systemui`
- 最低系统：Android 12（API 31），编译 SDK 34

> 本仓库的源码由 APK `流体云通知0.3.20.apk` 反编译重建而来，用于学习与研究。原始 APK 与模块作者的一切权利归原作者所有。

## 它能做什么

| 能力 | 说明 |
| --- | --- |
| 普通通知上岛 | 把系统界面里的普通消息通知转换成原生流体云胶囊，点击展开卡片，下拉通知栏仍保留原通知 |
| 应用选择 | 「全部应用」或「仅选中应用」两种模式，可按名称/包名搜索，支持全选、清空 |
| 常驻通知过滤 | USB、VPN、后台服务等常驻通知不转换，保持系统原生行为 |
| 自动展开时长 | 收到通知先展开卡片，到时收起为应用图标 + 滚动文字胶囊（0–999 秒，0 为官方默认） |
| 缩小时间 | 文字胶囊停留后按原生动效缩成小括号（0–999 秒） |
| 卡片滑动 | 整张卡片或仅左右边缘起手；短滑回弹，滑过阈值松手清除（距离可调） |
| 下拉小窗 | 下拉展开的流体云，以小窗打开应用；过滤不支持小窗的应用并给出提示 |
| 浮窗回复同步 | 展开卡片中的「浮窗回复」可替换为底部小白条，下拉小白条以小窗打开应用 |
| 液态玻璃样式 | 卡片/胶囊的玻璃质感渲染（AGSL 着色器 `liquid_glass.agsl`） |
| 通知实验室 | 在模块内直接发送聊天、长文本、表情混排、下载进度、常驻通知等测试通知，并可一键撤销 |

## 目录结构

```
.
├── app/src/main/AndroidManifest.xml        # 模块声明、xposed meta-data、Provider/Receiver/Activity
├── app/src/main/assets/xposed_init         # 入口类 io.github.fluidnotify.FluidHook
├── app/src/main/java/io/github/fluidnotify # 36 个类（约 6000 行）
│   ├── FluidHook.java                      # IXposedHookLoadPackage 入口，安装全部 Hook
│   ├── ConfigStore / ConfigProvider / ConfigAccessReceiver / ConfigReload  # 配置读写与跨进程同步
│   ├── MainActivity / AppSelectionActivity / AppSelectionSettings          # 设置界面（纯代码构建 UI）
│   ├── NotificationPolicy / NotificationTiming / NotificationExpansion     # 上岛策略与时长
│   ├── CapsuleTicker / CapsuleMarquee                                      # 胶囊文字滚动
│   ├── CardSwipe / SwipePreview / SwipeRules / ClearCardList / ClearAllCards  # 滑动清除
│   ├── PullDownWindow / PullDownGesture / PullDownConfig                   # 下拉小窗
│   ├── SmallWindow* / FloatingReplySync / ReplyActions                     # 小窗启动与回复同步
│   └── GlassStyle / GlassScene / PluginProfile / DurationConfig ...        # 玻璃样式与配置项
└── app/src/main/res                        # 图标、字符串、主题、作用域数组、AGSL 着色器
```

## 编译

需要 JDK 17+ 与 Android SDK 34。

```bash
# 用 Android Studio 直接打开本仓库，或：
./gradlew :app:assembleRelease
```

`app/build.gradle.kts` 里通过 `compileOnly "de.robv.android.xposed:api:82"`
引入 Xposed API（仓库地址 `https://api.xposed.info/`），运行时由 LSPosed 提供，
不会打进 APK。Release 目前复用 debug 签名，方便直接刷入测试。

## 使用

1. 在 LSPosed 中启用「通知流体云」，作用域只勾选「系统界面」。
2. 安装或升级模块后重启系统界面（或重启手机）。
3. 打开模块，在「通知实验室」发送测试通知确认效果；需要通知权限。
4. 在「选择上岛应用」里决定是全部应用还是仅选中应用。

> 设置页顶部的开关只代表保存的配置，不代表 LSPosed 模块实际的激活状态。

## 说明

- Hook 目标均为 ColorOS SystemUI 的私有类（`com.oplus.systemui...`），随系统版本变化，
  换版本后可能需要调整类名与方法名。
- 源码由反编译结果整理：保留了原有类名与主要结构，`R.java`、`public.xml` 属于构建产物已移除，
  改由 `res/values/ids.xml` 声明代码中用到的两个 id；Manifest 中构建期写入的属性也已清理。
- 本项目未在真机上编译/运行验证，仅供阅读与二次开发参考。
