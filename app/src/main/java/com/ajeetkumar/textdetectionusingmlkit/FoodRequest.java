package com.ajeetkumar.textdetectionusingmlkit;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class FoodRequest extends AsyncTask<String, Void, Food> {
    /*private static final String APP_ID = "31e4afb5";
    private static final String APPLICATION_KEY = "259bb900d5b359c0c795f9941221836d";*/


    private static final String APP_ID = new keyHolder().APP_ID;
    private static final String APPLICATION_KEY =  new keyHolder().APPLICATION_KEY;

    private static final int MINIMUM_HITS = 0;
    private RectF rectF;



    public FoodRequest(RectF rectF) {
        this.rectF = rectF;



    }

    @Override
    protected Food doInBackground(String... strings) {
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
            responseString = myResponse.getJSONArray("hits").getJSONObject(0).getJSONObject("fields").getString("item_id");
            Log.d("response", responseString);
            hits = myResponse.getInt("total_hits");
            String otherURL = "https://api.nutritionix.com/v1_1/item?id=" + responseString + "&appId=" + APP_ID + "&appKey=" + APPLICATION_KEY;

            if (hits >= MINIMUM_HITS) {
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
                int responseCalories = myResponse1.getInt("nf_calories");
                Log.d("CALORIES", responseCalories + " ");
                int responseCarbs = myResponse1.getInt("nf_total_carbohydrate");
                int resppnseFat = myResponse1.getInt("nf_total_fat");
                food = new Food(foodName, responseCalories, resppnseFat, responseCarbs, rectF, null);
            }


        } catch (ProtocolException e) {
            Log.d("INITIAL PROTOCOL", e.getLocalizedMessage());
            e.printStackTrace();


        } catch (MalformedURLException f) {
            Log.d("INITIAL MALFORMED", f.getLocalizedMessage());
            f.printStackTrace();

        } catch (IOException e) {
            Log.d("INITIAL IO", e.getLocalizedMessage());
            e.printStackTrace();

        } catch (JSONException e) {
            Log.d("INITIAL JSON", e.getLocalizedMessage());
            e.printStackTrace();

        }
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    Log.d("couldn't close", e.getLocalizedMessage());
                }
            }

            if (in1 != null) {
                try {
                    in1.close();
                } catch (Exception e) {
                    Log.d("couldn't close", e.getLocalizedMessage());
                }
            }
        }


        Log.d("FoodValue", food + "");
        return food;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    public void setRectF(RectF rectF) {
        this.rectF = rectF;
    }

    public RectF getRectF() {
        return rectF;
    }
}
