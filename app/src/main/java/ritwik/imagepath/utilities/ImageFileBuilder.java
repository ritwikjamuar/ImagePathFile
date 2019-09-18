package ritwik.imagepath.utilities;

import android.os.Environment;

import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

public class ImageFileBuilder {
	/**Creates an Image File.
	 * @param filePath {@link String} denoting Path to Directory where the File will be created. If passed {@code
	 * null}, then default File Path will be taken.
	 * @param fileName {@link String} denoting Name of the File. If passed {@code null}, then default File Name will
	 * be taken.
	 * @param fileExtension {@link String} specifying the Image File Format. If passed {@code null}, then default
	 * Image File Extension (.jpg) will be taken.
	 * @return Image {@link File}.
	 * @throws IOException Exception to occur when IO Fails. */
	public static File create (
		@Nullable String filePath,
		@Nullable String fileName,
		@Nullable String fileExtension
	) throws IOException {
		boolean isStorageDirCreated;
		File image = null;

		/* Create File object pointing to directory, and create the Directory if it does not exist */
		File storageDir = new File ( filePath == null ? getDefaultStorageDirectory () : filePath );
		if ( ! storageDir.isDirectory () ) {
			isStorageDirCreated = storageDir.mkdirs ();
		} else {
			isStorageDirCreated = true;
		}

		/* Create Image File with following parameters:
		 * 1. Prefix : File Name
		 * 2. Suffix : File Type Extension (ex- .jpg)
		 * 3. Directory. */
		if ( isStorageDirCreated ) {
			image = File.createTempFile (
				fileName == null ? getDefaultName () : fileName,
				fileExtension == null ? getDefaultFileFormat () : fileExtension,
				storageDir
			);
		}

		// Return the File.
		return image;
	}

	private static String getDefaultName () {
		String timeStamp = new SimpleDateFormat ( "yyyyMMdd", Locale.getDefault () ).format ( new Date () );
		return "Image_" + timeStamp + "_";
	}

	private static String getDefaultStorageDirectory () { return Environment.getExternalStorageDirectory () + "/ImagePath"; }

	private static String getDefaultFileFormat () { return ".jpg"; }
}