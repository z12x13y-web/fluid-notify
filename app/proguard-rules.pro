# Xposed 模块入口由 assets/xposed_init 指定，不能被混淆改名
-keep class io.github.fluidnotify.FluidHook { *; }
-keepclassmembers class io.github.fluidnotify.** { *; }
-keep class io.github.fluidnotify.ConfigProvider { *; }
-keep class io.github.fluidnotify.ConfigAccessReceiver { *; }
-keep class io.github.fluidnotify.MainActivity { *; }
-keep class io.github.fluidnotify.AppSelectionActivity { *; }
-dontwarn de.robv.android.xposed.**
