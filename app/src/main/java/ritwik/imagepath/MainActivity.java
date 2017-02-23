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

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
	String mCurrentPhotoPath;
	static final int REQUEST_TAKE_PHOTO = 1;
	Button btnCamera;
	ImageView imgView;

	@Override
	protected void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate ( savedInstanceState );
		setContentView ( R.layout.activity_main );

		initializeViews();
	}

	public void initializeViews()
	{
		btnCamera = (Button) findViewById ( R.id.btn_camera );
		imgView = (ImageView) findViewById ( R.id.img_view );

		btnCamera.setOnClickListener ( MainActivity.this );
	}

	@Override
	public void onClick ( View v )
	{
		if( v.getId () == R.id.btn_camera )
		{
			if ( !(
					ContextCompat.checkSelfPermission ( getApplicationContext (), Manifest.permission.READ_EXTERNAL_STORAGE )
							==
							PackageManager.PERMISSION_GRANTED
							&&
					ContextCompat.checkSelfPermission ( getApplicationContext (), Manifest.permission.WRITE_EXTERNAL_STORAGE )
							==
							PackageManager.PERMISSION_GRANTED
			))
				ActivityCompat.requestPermissions (
						MainActivity.this,
						new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
						200 );
			else
				dispatchTakePictureIntent ();
		}
	}

	@Override
	protected void onActivityResult ( int requestCode, int resultCode, Intent data )
	{
		super.onActivityResult ( requestCode, resultCode, data );
		imgView.setImageBitmap ( BitmapFactory.decodeFile ( mCurrentPhotoPath ) );
	}

	private File createImageFile() throws IOException
	{
		// Create an image file name
		String timeStamp = new SimpleDateFormat ( "yyyyMMdd" ).format ( new Date () );
		String imageFileName = "Image_" + timeStamp + "_";

		File storageDir = new File ( Environment.getExternalStorageDirectory ()+"/ImagePath" );
		if( ! storageDir.isDirectory () )
			storageDir.mkdirs();
		File image = File.createTempFile( imageFileName/* prefix */,".jpg"/* suffix */,storageDir/* directory */);

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = image.getAbsolutePath();
		Log.e( "mCurrentPhotoPath", mCurrentPhotoPath );
		return image;
	}

	private void dispatchTakePictureIntent()
	{
		Intent takePictureIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE);

		// Ensure that there's a camera activity to handle the intent
		if ( takePictureIntent.resolveActivity(getPackageManager()) != null )
		{
			// Create the File where the photo should go
			File photoFile = null;
			try
			{ photoFile = createImageFile(); }
			catch (IOException e)
			{
				Toast.makeText ( MainActivity.this, String.valueOf ( e.getMessage () ), Toast.LENGTH_SHORT ).show ();
				e.printStackTrace ();
			}

			// Continue only if the File was successfully created
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

					boolean readAccess = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					boolean writeAccess = grantResults[1] == PackageManager.PERMISSION_GRANTED;

					if (readAccess && writeAccess)
					{
						Toast.makeText ( MainActivity.this, "Permissions Granted.", Toast.LENGTH_SHORT ).show ();
						dispatchTakePictureIntent ();
					}
					else
					{
						Toast.makeText ( MainActivity.this, "Permissions Denied.", Toast.LENGTH_SHORT ).show ();

						if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
						{
							if ( shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) )
							{
								showMessageOKCancel( "Permissions Required", new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
											requestPermissions(
													new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
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
		new AlertDialog.Builder( MainActivity.this)
				.setMessage(message)
				.setPositiveButton("OK", okListener)
				.setNegativeButton("Cancel", null)
				.create()
				.show();
	}
}