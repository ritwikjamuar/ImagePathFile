package ritwik.imagepath.utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

public class MethodUtils {
	public static void showToast ( @NonNull Context context, @NonNull String message ) {
		Toast.makeText ( context, message, Toast.LENGTH_SHORT ).show ();
	}


}