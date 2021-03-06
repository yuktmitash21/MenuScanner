// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.ajeetkumar.textdetectionusingmlkit.text_detection;

import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.ajeetkumar.textdetectionusingmlkit.Food;
import com.ajeetkumar.textdetectionusingmlkit.others.FrameMetadata;
import com.ajeetkumar.textdetectionusingmlkit.others.GraphicOverlay;
//import com.ajeetkumar.textdetectionusingmlkit.others.VisionProcessorBase;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Processor for the text recognition demo.
 */
public class TextRecognitionProcessor {




	private HashSet<String> restrictions;
	private HashSet<String> styles;
	private ArrayList<Food> allFoods;

	private int responseCalories;
	private int responseCarbs;
	private int resppnseFat;

	private String responseString;
	private String importance;
	private Food bestFood;

	private int numberOfLegitimateFoods = 0;
	private int lowest = 0;
	private RequestQueue mQueue;

	private FirebaseVisionText results;



	private static final String TAG = "TextRecProc";

	private final FirebaseVisionTextRecognizer detector;

	// Whether we should ignore process(). This is usually caused by feeding input data faster than
	// the model can handle.
	private final AtomicBoolean shouldThrottle = new AtomicBoolean(false);

	public TextRecognitionProcessor(HashSet<String> restrictions, HashSet<String> styles) {
		detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
		this.restrictions = restrictions;
		this.styles = styles;
		allFoods = new ArrayList<>();

	}



	//region ----- Exposed Methods -----


	public void stop() {
		try {
			detector.close();
		} catch (IOException e) {
			Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
		}
	}


	public void process(ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay) throws FirebaseMLException {

		if (shouldThrottle.get()) {
			return;
		}
		FirebaseVisionImageMetadata metadata =
				new FirebaseVisionImageMetadata.Builder()
						.setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
						.setWidth(frameMetadata.getWidth())
						.setHeight(frameMetadata.getHeight())
						.setRotation(frameMetadata.getRotation())
						.build();

		detectInVisionImage(FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata, graphicOverlay);
	}

	//endregion

	//region ----- Helper Methods -----

	protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
		return detector.processImage(image);
	}


	protected void onSuccess( @NonNull FirebaseVisionText results, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {

		graphicOverlay.clear();


		List<FirebaseVisionText.TextBlock> blocks= results.getTextBlocks();
		GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, blocks, restrictions, styles);
		graphicOverlay.add(textGraphic);

		/*for (int i = 0; i < blocks.size(); i++) {
			List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
			for (int j = 0; j < lines.size(); j++) {
				GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, );
				graphicOverlay.add(textGraphic);
			}
		}
	}*/


	}

	protected void onFailure(@NonNull Exception e) {
		Log.w(TAG, "Text detection failed." + e);
	}

	private void detectInVisionImage( FirebaseVisionImage image, final FrameMetadata metadata, final GraphicOverlay graphicOverlay) {

		detectInImage(image)
				.addOnSuccessListener(
						new OnSuccessListener<FirebaseVisionText>() {
							@Override
							public void onSuccess(FirebaseVisionText results) {
								shouldThrottle.set(false);

								TextRecognitionProcessor.this.results = results;
								TextRecognitionProcessor.this.onSuccess(results, metadata, graphicOverlay);
							}
						})
				.addOnFailureListener(
						new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								shouldThrottle.set(false);
								TextRecognitionProcessor.this.onFailure(e);
							}
						});
		// Begin throttling until this frame of input has been processed, either in onSuccess or
		// onFailure.
		shouldThrottle.set(true);
	}

	public FirebaseVisionText getResults() {
		return results;
	}
}
