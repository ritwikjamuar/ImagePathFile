package ritwik.imagepath.ui;

import android.Manifest;

import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;

import android.graphics.BitmapFactory;

import android.net.Uri;

import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;

import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.view.View;

import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import ritwik.imagepath.BuildConfig;
import ritwik.imagepath.R;

import ritwik.imagepath.utilities.ImageFileBuilder;
import ritwik.imagepath.utilities.ImageFilePath;
import ritwik.imagepath.utilities.MethodUtils;

public class MainActivity
	extends AppCompatActivity {
	// Views.
	private ImageView mImgView;

	// Variables.
	private String mCurrentPhotoPath;

	// Constants.
	private static final int REQUEST_TAKE_PHOTO = 1;
	private static final int REQUEST_GALLERY = 2;
	private static final int REQUEST_PERMISSION = 3;

	private static final String [] PERMISSIONS = {
		Manifest.permission.READ_EXTERNAL_STORAGE,
		Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	/**---------------------------------------- {@link View.OnClickListener}s ---------------------------------------**/

	private View.OnClickListener mCameraListener = view -> {
		if ( ! ( checkPermission ( PERMISSIONS [ 0 ] ) && checkPermission ( PERMISSIONS [ 1 ] ) ) ) {
			requestPermissions ();
		} else {
			dispatchTakePictureIntent ();
		}
	};

	private View.OnClickListener mGalleryListener = view ->
		startActivityForResult (
			Intent.createChooser (
				new Intent ()
					.setType ( "image/*" )
					.setAction ( Intent.ACTION_GET_CONTENT ),
				"Select Picture"
			),
			REQUEST_GALLERY
		);

	private DialogInterface.OnClickListener mDialogClickListener = ( dialogInterface, i ) -> {
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
			requestPermissions ( PERMISSIONS, REQUEST_PERMISSION );
		}
	};

	/**----------------------------------- {@link android.app.Activity} Callbacks -----------------------------------**/

	@Override protected void onCreate ( Bundle savedInstanceState ) {
		super.onCreate ( savedInstanceState );
		setContentView ( R.layout.activity_main );
		initializeViews ();
	}

	@Override protected void onActivityResult ( int requestCode, int resultCode, Intent data ) {
		super.onActivityResult ( requestCode, resultCode, data );
		if ( resultCode == RESULT_OK ) {
			//For both the cases ( Camera and Gallery) we are displaying the image using Image Path.
			switch ( requestCode ) {
				case REQUEST_TAKE_PHOTO : {
					mImgView.setImageBitmap ( BitmapFactory.decodeFile ( mCurrentPhotoPath ) );
					MethodUtils.showToast ( MainActivity.this, mCurrentPhotoPath );
				} break;

				case REQUEST_GALLERY : {
					mCurrentPhotoPath = ImageFilePath.getPath( MainActivity.this, data.getData());
					mImgView.setImageBitmap ( BitmapFactory.decodeFile ( mCurrentPhotoPath ) );
					MethodUtils.showToast ( MainActivity.this, mCurrentPhotoPath );
				} break;
			}
		}
	}

	@Override public void onRequestPermissionsResult ( int requestCode, @NonNull String [] permissions, @NonNull int [] grantResults ) {
		super.onRequestPermissionsResult ( requestCode, permissions, grantResults );
		if ( requestCode == REQUEST_PERMISSION ) {
			if ( grantResults.length > 0 ) {
				/* Check if the permission is granted or not. */
				boolean readAccess  = grantResults [ 0 ] == PackageManager.PERMISSION_GRANTED;
				boolean writeAccess = grantResults [ 1 ] == PackageManager.PERMISSION_GRANTED;

				if ( readAccess && writeAccess ) {
					/* Start the Image Capture method calls. */
					MethodUtils.showToast ( MainActivity.this, "Permissions Granted." );
					dispatchTakePictureIntent ();
				} else {
					/* Request the required permission. */
					MethodUtils.showToast ( MainActivity.this, "Permissions Denied" );
					if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
						if ( shouldShowRequestPermissionRationale ( PERMISSIONS [ 0 ] ) ) {
							showMessageOKCancel ( "Permissions Required" );
						}
					}
				}
			}
		}
	}

	/**------------------------------------------- {@code private} Methods ------------------------------------------**/

	/**Initialize the {@link View}s of this {@link android.app.Activity}.*/
	private void initializeViews () {
		// Instantiate Views.
		Button btnCamera  = findViewById ( R.id.btn_camera );
		Button btnGallery = findViewById ( R.id.btn_gallery );
		mImgView = findViewById ( R.id.img_view );

		// Set On-Click Listeners to required views.
		btnCamera.setOnClickListener ( mCameraListener );
		btnGallery.setOnClickListener ( mGalleryListener );
	}

	/**Checks whether a given permission is granted by the User for this {@link android.app.Application}.
	 * @param permission {@link String} belonging to {@link Manifest.permission} in check for grant.
	 * @return {@code true} if the given {@link Manifest.permission}.* is granted, {@code false} otherwise.*/
	private boolean checkPermission ( @NonNull String permission ) {
		return ContextCompat.checkSelfPermission ( getApplicationContext (), permission ) == PackageManager.PERMISSION_GRANTED;
	}

	/**Requests Permission from User by showing Permission Request dialog from Android System.*/
	private void requestPermissions () {
		ActivityCompat.requestPermissions (
			MainActivity.this,
			PERMISSIONS,
			REQUEST_PERMISSION );
	}

	private void dispatchTakePictureIntent () {
		Intent takePictureIntent = new Intent ( MediaStore.ACTION_IMAGE_CAPTURE );

		/* Ensure that there's a camera activity to handle the intent. */
		if ( takePictureIntent.resolveActivity ( getPackageManager()) != null ) {
			/* Create the File where the photo should go. */
			File photoFile = null;
			try {
				photoFile = ImageFileBuilder.create ( null, null, null );
				mCurrentPhotoPath = photoFile.getAbsolutePath ();
			} catch ( IOException | NullPointerException e ) {
				mCurrentPhotoPath = "";
				e.printStackTrace ();
			}

			/*
			 * Once, the file is successfully created, Camera Intent will store the image data to the File provided.
			 * File will be
			 * Continue only if the File was successfully created.
 			 */
			if ( photoFile != null ) {
				Uri photoURI = FileProvider.getUriForFile (
					MainActivity.this,
					BuildConfig.APPLICATION_ID + ".provider",
					photoFile
				);

				takePictureIntent.putExtra ( MediaStore.EXTRA_OUTPUT, photoURI );
				startActivityForResult ( takePictureIntent, REQUEST_TAKE_PHOTO );
			}
		}
	}

	/**Shows an {@link AlertDialog} with some message.
	 * @param message {@link String} denoting the message.*/
	@SuppressWarnings ( "SameParameterValue" )
	private void showMessageOKCancel ( String message ) {
		new AlertDialog
			.Builder ( MainActivity.this ) /* Context */
			.setMessage ( message ) /* Message */
			.setPositiveButton ( "OK", mDialogClickListener ) /* Button Click Listener */
			.setNegativeButton ( "Cancel", null )
			.create ()
			.show ();
	}
}