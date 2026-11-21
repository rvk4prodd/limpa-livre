# Preserve AccessibilityService entry point and callbacks
-keep public class com.limpalivre.optimizer.service.CacheCleanerAccessibilityService extends android.accessibilityservice.AccessibilityService {
    public *;
}

# Preserve data models for internal inspection / reflection safety
-keep class com.limpalivre.optimizer.data.model.** { *; }

# Keep MainActivity entry point
-keep public class com.limpalivre.optimizer.MainActivity extends androidx.activity.ComponentActivity {
    public *;
}
