package org.boorbootler.library;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.util.Log;

public class Utils {
	/**
	 * Check if the specified permission is authorized
	 * 
	 * @param context
	 *            : the context
	 * @param permissionURI
	 *            : the permission URI you want to check
	 * @return true if the permission is granted, false otherwise
	 */
	public static boolean checkPermissionString(Context context, String permissionURI) {
		final String packageName = context.getPackageName();
		final PackageManager pm = context.getPackageManager();
		if (pm.checkPermission(permissionURI, packageName) == PackageManager.PERMISSION_DENIED) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Getting the current connection state
	 * 
	 * @param context
	 *            : the context
	 * @return the current connection state
	 */
	public static boolean getConnection(Context context) {
		if (!checkPermissionString(context, android.Manifest.permission.ACCESS_NETWORK_STATE)) {
			Log.v("DoorBootler-Utils", "Missing permission " + android.Manifest.permission.ACCESS_NETWORK_STATE);
			return false;
		} else {
			final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

			if (conMgr != null && conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable()
					&& conMgr.getActiveNetworkInfo().isConnected()) {
				return true;
			} else {
				return false;
			}
		}
	}

}
