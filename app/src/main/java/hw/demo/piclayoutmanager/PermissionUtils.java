
package hw.demo.piclayoutmanager;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    private static final String TAG = "PermissionUtils";

    private static final String[] RUNTIME_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private static final int REQUEST_CODE = 1;

    public static void requestMultiplePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> neededPermissions = new ArrayList<String>();
            for (String permission : RUNTIME_PERMISSIONS) {
                if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    neededPermissions.add(permission);
                }
            }
            if (!neededPermissions.isEmpty()) {
                String[] permissionArr = new String[neededPermissions.size()];
                neededPermissions.toArray(permissionArr);
                activity.requestPermissions(permissionArr, REQUEST_CODE);
            }
        }
    }

    public static boolean checkPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static boolean checkPermissions(Context context) {
        String[] permNames = RUNTIME_PERMISSIONS;
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> notGrantedPerms = new ArrayList<String>();
            for (String tmp : permNames) {
                if (PackageManager.PERMISSION_GRANTED != context.checkSelfPermission(tmp)) {
                    notGrantedPerms.add(tmp);
                }
            }
            return notGrantedPerms.size() == 0;
        }
        return true;
    }
}
