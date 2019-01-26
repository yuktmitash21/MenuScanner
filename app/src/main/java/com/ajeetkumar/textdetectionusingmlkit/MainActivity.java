package com.ajeetkumar.textdetectionusingmlkit;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ajeetkumar.textdetectionusingmlkit.camera.CameraSource;
import com.ajeetkumar.textdetectionusingmlkit.camera.CameraSourcePreview;
import com.ajeetkumar.textdetectionusingmlkit.others.GraphicOverlay;
import com.ajeetkumar.textdetectionusingmlkit.text_detection.TextRecognitionProcessor;
import com.google.firebase.FirebaseApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

	//region ----- Instance Variables -----

	private CameraSource cameraSource = null;
	private CameraSourcePreview preview;
	private GraphicOverlay graphicOverlay;

	private static String TAG = MainActivity.class.getSimpleName().toString().trim();

	private HashSet<String> dietRestrictions;
	private HashSet<String> dietHabits;

	private ArrayList<String> rest;
	private ArrayList<String> hab;

	//endregion

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		hab = getIntent().getStringArrayListExtra("habits");
		rest = getIntent().getStringArrayListExtra("restrictions");




		dietHabits = new HashSet<>(hab);
		dietRestrictions = new HashSet<>(rest);




		/*final String[] restrictions = {"Vegan", "Vegetarian", "Gluten-Free", "Kosher", "Nut Allergy", "None"};
		AlertDialog.Builder builder;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
		} else {
			builder = new AlertDialog.Builder(MainActivity.this);
		}
		builder.setTitle("Pick any restrictions").
				setMultiChoiceItems(restrictions, null, new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i, boolean b) {
						dietRestrictions.add(restrictions[i]);


					}
				}).setPositiveButton("Done", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				AlertDialog.Builder builder2;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					builder2 = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
				} else {
					builder2 = new AlertDialog.Builder(MainActivity.this);
				}

				final String[] diet = {"Low-Carb", "Low-Fat", "None"};
				builder2.setTitle("Pick a dieting style").setMultiChoiceItems(diet, null, new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i, boolean b) {
						dietHabits.add(diet[i]);
					}
				}).setPositiveButton("Done", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {

					}
				}).show();

			}
		}).show();*/






		preview = (CameraSourcePreview) findViewById(R.id.camera_source_preview);
		if (preview == null) {
			Log.d(TAG, "Preview is null");
		}
		graphicOverlay = (GraphicOverlay) findViewById(R.id.graphics_overlay);
		if (graphicOverlay == null) {
			Log.d(TAG, "graphicOverlay is null");
		}

		createCameraSource();
		startCameraSource();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		startCameraSource();
	}

	/** Stops the camera. */
	@Override
	protected void onPause() {
		super.onPause();
		preview.stop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (cameraSource != null) {
			cameraSource.release();
		}
	}

	private void createCameraSource() {

		if (cameraSource == null) {
			cameraSource = new CameraSource(this, graphicOverlay);
			cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
		}

		cameraSource.setMachineLearningFrameProcessor(new TextRecognitionProcessor(dietRestrictions, dietHabits));
	}

	private void startCameraSource() {
		if (cameraSource != null) {
			try {
				if (preview == null) {
					Log.d(TAG, "resume: Preview is null");
				}
				if (graphicOverlay == null) {
					Log.d(TAG, "resume: graphOverlay is null");
				}
				preview.start(cameraSource, graphicOverlay);
			} catch (IOException e) {
				Log.e(TAG, "Unable to start camera source.", e);
				cameraSource.release();
				cameraSource = null;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		takeScreenshot();
		return true;
	}

	private void takeScreenshot() {
		Date now = new Date();
		android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

		try {
			// image naming and path  to include sd card  appending name you choose for file
			String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

			// create bitmap screen capture
			View v1 = getWindow().getDecorView().getRootView();
			v1.setDrawingCacheEnabled(true);
			Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
			v1.setDrawingCacheEnabled(false);

			File imageFile = new File(mPath);

			FileOutputStream outputStream = new FileOutputStream(imageFile);
			int quality = 100;
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
			outputStream.flush();
			outputStream.close();

			openScreenshot(imageFile);
		} catch (Throwable e) {
			// Several error may come out with file handling or DOM
			e.printStackTrace();
		}
	}

	private void openScreenshot(File imageFile) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(imageFile);
		intent.setDataAndType(uri, "image/*");
		startActivity(intent);
	}
}
