-keep class se.mediabuttonrelay.app.MainActivity { *; }
-keep class se.mediabuttonrelay.app.MediaRelayService { *; }
-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
