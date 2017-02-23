package ritwik.imagepath;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
			dispatchTakePictureIntent ();
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
		String timeStamp = new SimpleDateFormat ( "yyyyMMdd" ).format( new Date ());
		String imageFileName = "Image_" + timeStamp + "_";
		File storageDir = getExternalFilesDir( Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile( imageFileName/* prefix */,".jpg"/* suffix */,storageDir/* directory */ );

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = image.getAbsolutePath();
		Log.e( "mCurrentPhotoPath", mCurrentPhotoPath );
		Log.e ( "storageDir", storageDir.getAbsolutePath () );
		Log.e ( "image", image.getAbsolutePath () );
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
			catch (IOException e) { e.printStackTrace (); }

			// Continue only if the File was successfully created
			if ( photoFile != null )
			{
				Uri photoURI = FileProvider.getUriForFile( MainActivity.this, BuildConfig.APPLICATION_ID +".provider", photoFile );
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}
}