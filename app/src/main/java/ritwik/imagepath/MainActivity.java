package ritwik.imagepath;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
	static final int REQUEST_TAKE_PHOTO = 1, REQUEST_GALLERY = 2;
	String mCurrentPhotoPath;
	Button mBtnCamera, mBtnGallery;
	ImageView mImgView;

	@Override
	protected void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate ( savedInstanceState );
		setContentView ( R.layout.activity_main );

		initializeViews();
	}

	public void initializeViews()
	{
		mBtnCamera = (Button) findViewById ( R.id.btn_camera );
		mBtnGallery = (Button) findViewById ( R.id.btn_gallery );
		mImgView = (ImageView) findViewById ( R.id.img_view );

		mBtnCamera.setOnClickListener ( MainActivity.this );
		mBtnGallery.setOnClickListener ( MainActivity.this );
	}

	@RequiresApi (api = Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onClick ( View v )
	{
		switch( v.getId () )
		{
			case R.id.btn_camera:

				/**
				 * Check whether permission to Read and Write to External Storage is grated or not..
				 */

				if ( ! (
					ContextCompat.checkSelfPermission ( getApplicationContext (), Manifest.permission.READ_EXTERNAL_STORAGE )
							==
							PackageManager.PERMISSION_GRANTED
							&&
					ContextCompat.checkSelfPermission ( getApplicationContext (), Manifest.permission.WRITE_EXTERNAL_STORAGE )
							==
							PackageManager.PERMISSION_GRANTED
				) )
					/**
					 * If permission is not granted, request permission at runtime.
					 */
					ActivityCompat.requestPermissions (
						MainActivity.this,
						new String[] {
								Manifest.permission.READ_EXTERNAL_STORAGE,
								Manifest.permission.WRITE_EXTERNAL_STORAGE
						},
						200 );
				else
					dispatchTakePictureIntent ();
				break;

			case R.id.btn_gallery:
				startActivityForResult (
					Intent.createChooser (
							new Intent()
									.setType ( "image/*" )
									.setAction ( Intent.ACTION_GET_CONTENT ),
							"Select Picture" ),
					REQUEST_GALLERY );
				break;
		}
	}

	@Override
	protected void onActivityResult ( int requestCode, int resultCode, Intent data )
	{
		super.onActivityResult ( requestCode, resultCode, data );

		if( resultCode == RESULT_OK )
		{
			/**
			 * For both the cases ( Camera and Gallery) we are displaying the image using Image Path.
			 */
			switch ( requestCode )
			{
				case REQUEST_TAKE_PHOTO:
					mImgView.setImageBitmap ( BitmapFactory.decodeFile ( mCurrentPhotoPath ) );
					Toast.makeText ( MainActivity.this, mCurrentPhotoPath, Toast.LENGTH_SHORT ).show ();
					break;

				case REQUEST_GALLERY:
					mCurrentPhotoPath = ImageFilePath.getPath( MainActivity.this, data.getData());
					mImgView.setImageBitmap ( BitmapFactory.decodeFile ( mCurrentPhotoPath ) );
					Toast.makeText ( MainActivity.this, mCurrentPhotoPath, Toast.LENGTH_SHORT ).show ();
					break;
			}
		}
	}

	private File createImageFile() throws IOException
	{
		/**
		 * Create an image file name
		 */
		String timeStamp = new SimpleDateFormat ( "yyyyMMdd" ).format ( new Date () );
		String imageFileName = "Image_" + timeStamp + "_";

		/**
		 * Create File object pointing to directory, and create the Directory if it does not exist
		 */
		File storageDir = new File ( Environment.getExternalStorageDirectory ()+"/ImagePath" );
		if( ! storageDir.isDirectory () )
			storageDir.mkdirs();

		/**
		 * Create Image File with following parameters:
		 * 1. Prefix : File Name
		 * 2. Suffix : File Type Extension (ex- .jpg)
		 * 3. Directory
		 */
		File image = File.createTempFile(
				imageFileName,
				".jpg",
				storageDir
		);

		/**
		 * Save a file: path for use with ACTION_VIEW intents
		 */
		mCurrentPhotoPath = image.getAbsolutePath();
		return image;
	}

	private void dispatchTakePictureIntent()
	{
		Intent takePictureIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE);

		/**
		 * Ensure that there's a camera activity to handle the intent
 		 */
		if ( takePictureIntent.resolveActivity(getPackageManager()) != null )
		{
			/**
			 * Create the File where the photo should go
			 */

			File photoFile = null;
			try { photoFile = createImageFile(); }
			catch (IOException e)
			{
				Toast.makeText ( MainActivity.this, String.valueOf ( e.getMessage () ), Toast.LENGTH_SHORT ).show ();
				e.printStackTrace ();
			}

			/**
			 * Once, the file is successfully created, Camera Intent will store the image data to the File provided.
			 * File will be
			 * Continue only if the File was successfully created.
 			 */
			if ( photoFile != null )
			{
				Uri photoURI = FileProvider.getUriForFile(
						MainActivity.this,
						BuildConfig.APPLICATION_ID +".provider",
						photoFile );

				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults )
	{
		super.onRequestPermissionsResult ( requestCode, permissions, grantResults );
		switch ( requestCode )
		{
			case 200:
				if ( grantResults.length > 0 )
				{
					/**
					 * Check if the permission is granted or not.
					 */
					boolean readAccess = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					boolean writeAccess = grantResults[1] == PackageManager.PERMISSION_GRANTED;

					if (readAccess && writeAccess)
					{
						/**
						 * Start the Image Capture method calls.
						 */
						Toast.makeText ( MainActivity.this, "Permissions Granted.", Toast.LENGTH_SHORT ).show ();
						dispatchTakePictureIntent ();
					}
					else
					{
						/**
						 * Request the required permission.
						 */
						Toast.makeText ( MainActivity.this, "Permissions Denied.", Toast.LENGTH_SHORT ).show ();

						if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
						{
							if ( shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE) )
							{
								showMessageOKCancel( "Permissions Required", new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
											requestPermissions(
													new String[]{
															Manifest.permission.READ_EXTERNAL_STORAGE,
															Manifest.permission.WRITE_EXTERNAL_STORAGE},
													200
											);
									}
								});
								return;
							}
						}
					}
				}
				break;
		}
	}

	private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener)
	{
		/**
		 * Create the Alert Dialog to request the required permission.
		 */
		new AlertDialog.Builder( MainActivity.this)
				.setMessage( message )
				.setPositiveButton( "OK", okListener )
				.setNegativeButton( "Cancel", null )
				.create()
				.show();
	}
}