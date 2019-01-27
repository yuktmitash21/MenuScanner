package com.ajeetkumar.textdetectionusingmlkit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;
import com.amazonaws.services.polly.model.Voice;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.TextDetection;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import okhttp3.OkHttpClient;

import static okhttp3.internal.tls.TrustRootIndex.get;

public class Meal extends AppCompatActivity {




    private byte[] myPic;
    private Bitmap bitmap;
    private CardView cardView;
    private static final int CAMERA_REQUEST_CODE = 1;
    private ProgressDialog progressDialog;
    private AmazonRekognitionClient amazonRekognitionClient;

    private static final String APP_ID = new GeneralQueryKeyHolder().APP_ID;
    private static final String APPLICATION_KEY = new GeneralQueryKeyHolder().APPLICATION_KEY;
    private ImageView imageView;
    private ClarifaiClient clarifaiClient;
    private MediaPlayer mediaPlayer;

    private AmazonPollyPresigningClient client;
    private List<Voice> voices;
    private TextToSpeech tts;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal);


        cardView = findViewById(R.id.examinePic);
        progressDialog = new ProgressDialog(this);
        imageView = findViewById(R.id.imageView);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        clarifaiClient = new ClarifaiBuilder("6cc5f72d934548b6a6e0f65b32c9d4b7")
                .client(new OkHttpClient()) // OPTIONAL. Allows customization of OkHttp by the user
                .buildSync();

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent picIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(picIntent, CAMERA_REQUEST_CODE);
            }
        });
        AWSCredentials awsCredentials = new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return "AKIAJ7OHJBLBT3HSMBIA";
            }

            @Override
            public String getAWSSecretKey() {
                return "WJUFEaVeIu+Vbo4nd01A4iC3Qdy4zRpdBc1sD2tI";
            }
        };
        amazonRekognitionClient = new AmazonRekognitionClient(awsCredentials);
        initPollyClient();
        setupNewMediaPlayer();
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                   int result = tts.setLanguage(Locale.ENGLISH);
                   if (result == TextToSpeech.LANG_MISSING_DATA ||
                           result == TextToSpeech.LANG_NOT_SUPPORTED) {
                       Log.d("tts", "PROBLEM");
                   }
                }

            }
        });




    }

    void initPollyClient() {
        AWSMobileClient.getInstance().initialize(this, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                // Create a client that supports generation of presigned URLs.
                client = new AmazonPollyPresigningClient(AWSMobileClient.getInstance());


                if (voices == null) {
                    // Create describe voices request.
                    DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

                    try {
                        // Synchronously ask the Polly Service to describe available TTS voices.
                        DescribeVoicesResult describeVoicesResult = client.describeVoices(describeVoicesRequest);

                        // Get list of voices from the result.
                        voices = describeVoicesResult.getVoices();

                        // Log a message with a list of available TTS voices.

                    } catch (RuntimeException e) {

                        return;
                    }
                }


            }

            @Override
            public void onError(Exception e) {
                Log.e("TAG", "onError: Initialization error", e);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK)) {
            progressDialog.setMessage("Uploading image...");
            progressDialog.show();



            bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            myPic = baos.toByteArray();
            imageView.setImageBitmap(bitmap);

            Image image = new Image().withBytes(ByteBuffer.wrap(myPic));

            try {
                List<Label> result = new myTask().execute(image).get();
                HashSet<String> tags = new runClari().execute().get();
                for (Label l :result) {
                   if (tags.contains(l.getName().toLowerCase())) {
                      Toast.makeText(getApplicationContext(), "common tag " + l.getName(), Toast.LENGTH_SHORT).show();
                      getFoodInfo(l.getName());
                      break;

                   }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            progressDialog.dismiss();
            //Toast.makeText(getApplicationContext(), "Picture Set", Toast.LENGTH_SHORT).show();

        }
    }

    private void getFoodInfo(String name) {
        String url = "https://api.nutritionix.com/v1_1/search/" + name + "?results=0%3A20&cal_min=0&cal_max=10000&fields=item_name%2Cbrand_name%2Citem_id%2Cbrand_id&appId=" + APP_ID + "&appKey=" + APPLICATION_KEY +"&format.json";
        try {
            VoiceFoodObject voiceFoodObject = new getFoodTask().execute(url, name).get();
            String speaking = "This is a " + voiceFoodObject.getName() + ". It has " + voiceFoodObject.getCalories() +" " +
                    "calories, " + voiceFoodObject.getCarbs() + " grams of carbs, " + voiceFoodObject.getProtein() + " grams" +
                    "of protein, and " + voiceFoodObject.getFat() + " grams of fat. " + "A " + voiceFoodObject.getName() + " also " +
                    "has " + voiceFoodObject.getDietaryFiber() + " grams of dietary fiber, " + voiceFoodObject.getCholesterol() +
                    " grams of cholesterol. In terms of daily values, " + voiceFoodObject.getName() + " has " +
                    voiceFoodObject.getCalcium() + " percent of your daily calcium requirements, " +
                    voiceFoodObject.getVitaminA() + " percent of your daily vitamin A requirement, " +
                    voiceFoodObject.getVitaminC() + " percent of your daily vitamin C requirements, and " +
                    voiceFoodObject.getIron() + " percent of your daily iron requirements";
            DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

// Synchronously ask Amazon Polly to describe available TTS voices.
            tts.speak(speaking, TextToSpeech.QUEUE_FLUSH, null);
            SynthesizeSpeechPresignRequest synthesizeSpeechPresignRequest =
                    new SynthesizeSpeechPresignRequest()
                            // Set text to synthesize.
                            .withText("WASSUPP")
                            // Set voice selected by the user.
                            .withVoiceId(voices.get(0).getId())
                            // Set format to MP3.
                            .withOutputFormat(OutputFormat.Mp3);

            // Get the presigned URL for synthesized speech audio stream.
            URL presignedSynthesizeSpeechUrl =
                    client.getPresignedSynthesizeSpeechUrl(synthesizeSpeechPresignRequest);

            Log.i("TAG", "Playing speech from presigned URL: " + presignedSynthesizeSpeechUrl);

            // Create a media player to play the synthesized audio stream.
            if (mediaPlayer.isPlaying()) {
                setupNewMediaPlayer();
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                // Set media player's data source to previously obtained URL.
                mediaPlayer.setDataSource(presignedSynthesizeSpeechUrl.toString());
            } catch (IOException e) {
                Log.e("TAG", "Unable to set data source for the media player! " + e.getMessage());
            }

            // Start the playback asynchronously (since the data source is a network stream).
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    void setupNewMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                setupNewMediaPlayer();
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    class myTask extends AsyncTask<Image, Void, List<Label>> {

        @Override
        protected List<Label> doInBackground(Image... images) {
            DetectLabelsRequest detectLabelsRequest = new DetectLabelsRequest().withImage(images[0]);
            DetectLabelsResult detectLabelsResult = amazonRekognitionClient.detectLabels(detectLabelsRequest);
            return detectLabelsResult.getLabels();

        }
    }

    class runClari extends AsyncTask<Void, Void, HashSet<String>> {

        @Override
        protected HashSet<String> doInBackground(Void... voids) {
            ClarifaiClient client = new ClarifaiBuilder("6cc5f72d934548b6a6e0f65b32c9d4b7")
                    .buildSync();
            ClarifaiResponse<List<ClarifaiOutput<Concept>>> response = client.getDefaultModels().foodModel()
                    .predict()
                    .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(myPic)))
                    .executeSync();
            ArrayList<String> myList = new ArrayList<>();

            for (ClarifaiOutput<Concept> c: response.get()) {
                for(Concept concept: c.data()) {
                    myList.add(concept.name().toLowerCase());
                }
            }

            return new HashSet<String>(myList);
        }
    }

    class getFoodTask extends AsyncTask<String, Void, VoiceFoodObject> {

        @Override
        protected VoiceFoodObject doInBackground(String... strings) {
            String url = strings[0];
            String foodName = strings[1];
            Log.d("URL", url);
            Log.d("FoodName", foodName);
            String responseString;
            int hits;
            Food food = null;
            BufferedReader in = null;
            BufferedReader in1 = null;
            try {
                URL urlObj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
                con.setRequestMethod("GET");
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputline;
                StringBuffer response = new StringBuffer();
                while ((inputline = in.readLine()) != null) {
                    response.append(inputline);
                }
                JSONObject myResponse = new JSONObject(response.toString());
                hits = myResponse.getInt("total_hits");
                responseString = myResponse.getJSONArray("hits").getJSONObject(0).getJSONObject("fields").getString("item_id");

                String otherURL = "https://api.nutritionix.com/v1_1/item?id=" + responseString + "&appId=" + APP_ID + "&appKey=" + APPLICATION_KEY;
                if (hits > 0) {
                    Log.d("Inside", "second parsing");
                    URL urlObj1 = new URL(otherURL);
                    HttpURLConnection con1 = (HttpURLConnection) urlObj1.openConnection();
                    con1.setRequestMethod("GET");
                    in1 = new BufferedReader(new InputStreamReader(con1.getInputStream()));
                    String inputline1;
                    StringBuffer response1 = new StringBuffer();

                    while ((inputline1 = in1.readLine()) != null) {
                        response1.append(inputline1);
                        Log.d("inputLines", inputline1);
                    }
                    Log.d("responseURL", otherURL);


                    Log.d("responseString", response1.toString());
                    JSONObject myResponse1 = new JSONObject(response1.toString());
                    VoiceFoodObject voiceFoodObject = new VoiceFoodObject();
                    voiceFoodObject.setName(foodName);
                    int responseCalories = myResponse1.getInt("nf_calories");
                    voiceFoodObject.setCalories(responseCalories);
                    int responseCarbs = myResponse1.getInt("nf_total_carbohydrate");
                    voiceFoodObject.setCarbs(responseCarbs);
                    int resppnseFat = myResponse1.getInt("nf_total_fat");
                    voiceFoodObject.setFat(resppnseFat);
                    int protein = myResponse1.getInt("nf_protein");
                    voiceFoodObject.setProtein(protein);
                    int cholesterol = myResponse1.getInt("nf_cholesterol");
                    voiceFoodObject.setCholesterol(cholesterol);
                    int dietaryfiber = myResponse1.getInt("nf_dietary_fiber");
                    voiceFoodObject.setDietaryFiber(dietaryfiber);
                    int calcium = myResponse1.getInt("nf_calcium_dv");
                    voiceFoodObject.setCalcium(calcium);
                    int vitaminA = myResponse1.getInt( "nf_vitamin_a_dv");
                    voiceFoodObject.setVitaminA(vitaminA);
                    int vitaminC = myResponse1.getInt("nf_vitamin_c_dv");
                    voiceFoodObject.setVitaminC(vitaminC);
                    int iron = myResponse1.getInt("nf_iron_dv");
                    voiceFoodObject.setIron(iron);

                    return voiceFoodObject;




                }

            } catch (Exception e) {
                e.printStackTrace();

            }

            return null;
        }
    }



    //class Speech extends AsyncTask<VoiceFoodObject, Void, >




}
