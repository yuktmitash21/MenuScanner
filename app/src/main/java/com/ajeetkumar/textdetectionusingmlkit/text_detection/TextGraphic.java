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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.text.TextPaint;
import android.util.Log;

import com.ajeetkumar.textdetectionusingmlkit.Food;
import com.ajeetkumar.textdetectionusingmlkit.others.GraphicOverlay;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class TextGraphic extends GraphicOverlay.Graphic {

    private static final int TEXT_COLOR = Color.WHITE;
    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 4.0f;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final List<FirebaseVisionText.TextBlock> textBlock;
    private RequestQueue mQueue;

    private static int responseCalories;
    private static int responseCarbs;
    private static int resppnseFat;

    private static String responseString;
    private String newName;
    private ArrayList<Food> allFoods;

    private HashSet<String> restrictions;
    private HashSet<String> styles;
    private String importance;
    private Food bestFood;

    private int numberOfLegitimateFoods = 0;
    private int lowest = 0;

    private JsonObjectRequest jsonObjectRequest;
    private JsonObjectRequest jsonObjectRequest1;

    private int idRequests = 0;
    private int nutrition = 0;

    private static int hits = 0;

    private static final String APP_ID = "7f1c1020";
    private static final String APPLICATION_KEY= "ade6643855503efc3b3184bf6b6ea308";


    private static final HashSet<String> Vegan = new HashSet<>(Arrays.asList("chicken", "bacon",
            "steak", "eggs", "meat", "milk", "ham", "burger", "sandwich", "cheese", "duck", "turkey",
            "pastrami", "yogurt", "fish", "shrimp", "anchovies", "squid", "scallops", "calamari", "mussels", "crab", "lobster",
            "butter", "cream", "cheese", "goose", "quail", "lamb", "pork", "veal", "beef", "angus", "frogeye"));

    private static final HashSet<String> Vegetarian = new HashSet<>(Arrays.asList("chicken", "bacon",
            "steak", "meat", "ham", "burger", "sandwich", "duck", "turkey",
            "pastrami", "fish", "shrimp", "anchovies", "squid", "scallops", "calamari", "mussels", "crab", "lobster",
            "goose", "quail", "lamb", "pork", "veal", "beef", "angus", "frogeye"));

    private static final HashSet<String> gluten = new HashSet<>(Arrays.asList("bread", "oats", "wheat", "rice",
            "beer", "cake", "pie", "cereal", "cookie", "cracker", "pasta", "burger", "sandwhich", "hot dog",
            "crouton", "french fries", "lasagne", "ravioli", "macaroni", "ziti"));

    private static final HashSet<String> nuts = new HashSet<>(Arrays.asList("peanut", "almond", "cashew"));

    private static final HashSet<String> kosher = new HashSet<>(Arrays.asList("bacon", "ham", "pork", "shellfish",
            "crab", "frogeye"));



    TextGraphic(GraphicOverlay overlay, List<FirebaseVisionText.TextBlock> textBlock, HashSet<String> restrictions,
                HashSet<String> styles) {
        super(overlay);

        mQueue = Volley.newRequestQueue(getApplicationContext());
        this.restrictions = restrictions;
        this.styles = styles;

        this.textBlock = textBlock;
        allFoods = new ArrayList<>();

        rectPaint = new Paint();
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    /** Draws the text block annotations for position, size, and raw value on the supplied canvas. */
    @Override
    public void draw(final Canvas canvas) {
        if (textBlock == null) {
            throw new IllegalStateException("Attempting to draw a null text.");
        }

        if (styles.contains("Atkins")) {
            importance = "carb";
        } else {
            importance = "fat";
        }

        for (int i = 0; i < textBlock.size(); i++) {
            FirebaseVisionText.TextBlock block = textBlock.get(i);
            final FirebaseVisionText.Line text = block.getLines().get(0);
            if (text.getElements().size() <= 4 && check(text.getElements())) {
                final String fooodN = text.getText();
                RectF rect1 = new RectF(text.getBoundingBox());
                rect1.left = translateX(rect1.left);
                rect1.top = translateY(rect1.top);
                rect1.right = translateX(rect1.right);
                rect1.bottom = translateY(rect1.bottom);
                String url = "https://api.nutritionix.com/v1_1/search/" + fooodN + "?results=0%3A20&cal_min=0&cal_max=10000&fields=item_name%2Cbrand_name%2Citem_id%2Cbrand_id&appId=" + APP_ID + "&appKey=" + APPLICATION_KEY +"&format.json";
                initialRequest(url);
                if (hits > 0) {
                String otherURL = "https://api.nutritionix.com/v1_1/item?id=" + responseString + "&appId=" + APP_ID + "&appKey=" + APPLICATION_KEY;
                secondCall(otherURL);
                Food food1 = new Food(fooodN, responseCalories, resppnseFat, responseCarbs, rect1, null);
                food1.setEdible(testIfEdible(food1));
                if (numberOfLegitimateFoods == 0 && food1.isEdible()) {
                    numberOfLegitimateFoods++;
                    if (importance.equals("carb")) {
                        lowest = food1.getCarbs();
                        bestFood = food1;
                    } else {
                        lowest = food1.getFat();
                        bestFood = food1;
                    }
                } else {
                    if (importance.equals("carb")) {
                        if (food1.getCarbs() < lowest && food1.isEdible()) {
                            bestFood = food1;
                            lowest = food1.getCarbs();
                        }
                    } else {
                        if (food1.getFat() < lowest && food1.isEdible()) {
                            lowest = food1.getFat();
                            bestFood = food1;
                        }
                    }

                }
                allFoods.add(food1);


                }
            }

        }









        for (Food food: allFoods) {
            String text;
            RectF rect = food.getBoundingBox();
            if (food.equals(bestFood)) {
                text = "HEALTHIEST OPTION: " + food.getName();
                rect.right = rect.right + (rect.right - rect.left);
            } else {
                text = food.getName();
            }
            if (food.isEdible()) {
                rectPaint.setColor(Color.BLUE);
                rectPaint.setStyle(Paint.Style.FILL);
            } else {
                rectPaint.setColor(Color.RED);
                rectPaint.setStyle(Paint.Style.FILL);
            }
            canvas.drawRect(rect, rectPaint);
            canvas.drawText(text, rect.left, rect.bottom, textPaint);


            RectF otherRect = rect;
            float diff = rect.bottom - rect.top;
            otherRect.top = rect.top + diff;
            otherRect.bottom = rect.bottom + diff;
            rectPaint.setColor(Color.BLACK);
            canvas.drawRect(otherRect, rectPaint);


            textPaint.setColor(Color.RED);
            canvas.drawText("Calories: " + food.getCalories(), otherRect.left, otherRect.bottom, textPaint);
            textPaint.setColor(Color.WHITE); //do at the end of loop

            RectF carbRect = otherRect;
            carbRect.top = carbRect.top + diff;
            carbRect.bottom = carbRect.bottom + diff;
            canvas.drawRect(otherRect, rectPaint);

            textPaint.setColor(Color.RED);
            canvas.drawText("Carbs: " + food.getCarbs(), carbRect.left, carbRect.bottom, textPaint);


            RectF fatRect = carbRect;
            fatRect.top = fatRect.top + diff;
            fatRect.bottom = fatRect.bottom + diff;
            canvas.drawRect(fatRect, rectPaint);

            textPaint.setColor(Color.RED);
            canvas.drawText("Fat: " + food.getCarbs(), fatRect.left, fatRect.bottom, textPaint);

            textPaint.setColor(Color.WHITE);
        }






    }


    private boolean testIfEdible(Food food) {
        if (restrictions.contains("None")) {
            return true;
        }
        String[] words = food.getName().toLowerCase().split(" ");


        for (String word: words) {
            if (restrictions.contains("Vegan")) {
                if (Vegan.contains(word)) {
                    return false;
                }
            }
            if (restrictions.contains("Vegetarian")) {
                if (Vegetarian.contains(word)) {
                    return false;
                }

            }
            if (restrictions.contains("Gluten-Free")) {
                if (gluten.contains(word)) {
                    return false;
                }

            }
            if (restrictions.contains("Kosher")) {
                if (kosher.contains(word)) {
                    return false;
                }

            }
            if (restrictions.contains("Nut Allergy")) {
                if (nuts.contains(word)) {
                    return false;
                }

            }
        }
        return true;


    }

    private boolean check(List<FirebaseVisionText.Element> elements) {
        for (FirebaseVisionText.Element element: elements) {
            char[] myCharArray = element.getText().toCharArray();
            for (Character c : myCharArray) {
                if (!Character.isAlphabetic(c)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static class myAsyncTask extends AsyncTask<String, Void, int[]> {


        @Override
        protected int[] doInBackground(String... strings) {
            try {
                URL urlObj = new URL(strings[0]);
                HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
                con.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputline;
                StringBuffer response = new StringBuffer();
                while ((inputline = in.readLine()) != null) {
                    response.append(inputline);
                }
                in.close();
                JSONObject myResponse = new JSONObject(response.toString());
                responseCalories = myResponse.getInt("nf_calories");
                Log.d("CALORIES", responseCalories + " ");
                responseCarbs = myResponse.getInt("nf_total_carbohydrate");
                resppnseFat = myResponse.getInt("nf_total_fat");

                int[] myStrings = new int[]{responseCalories, responseCarbs, resppnseFat};
                return myStrings;


            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(int[] ints) {
            super.onPostExecute(ints);
            if (ints != null) {
                int[] x = ints;
            }
        }
    }

    public static class otherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL urlObj = new URL(strings[0]);
                HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
                con.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputline;
                StringBuffer response = new StringBuffer();
                while ((inputline = in.readLine()) != null) {
                    response.append(inputline);
                }
                in.close();


                JSONObject myResponse = new JSONObject(response.toString());
                responseString = myResponse.getJSONArray("hits").getJSONObject(0).getJSONObject("fields").getString("item_id");
                hits = myResponse.getInt("total_hits");
                Log.d("HITS", hits + "");
                Log.d("THISMYID", responseString);
                if (hits > 0) {
                    String otherURL = "https://api.nutritionix.com/v1_1/item?id=" + responseString + "&appId=" + APP_ID + "&appKey=" + APPLICATION_KEY;
                    new myAsyncTask().execute(otherURL);
                    return responseString;
                }


            } catch (ProtocolException e) {

                Log.d("INITIAL PROTOCOL", e.getLocalizedMessage());
                return null;


            } catch (MalformedURLException f) {
                Log.d("INITIAL MALFORMED", f.getLocalizedMessage());
                return null;

            } catch (IOException e) {
                Log.d("INITIAL IO", e.getLocalizedMessage());
                return null;

            } catch (JSONException e) {
                Log.d("INITIAL JSON", e.getLocalizedMessage());
                return null;

            }

            return responseString;


        }



        }




    private void secondCall(final String otherURL) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL urlObj = new URL(otherURL);
                    HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
                    con.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputline;
                    StringBuffer response = new StringBuffer();
                    while ((inputline = in.readLine()) != null) {
                        response.append(inputline);
                    }
                    in.close();
                    JSONObject myResponse = new JSONObject(response.toString());
                    responseCalories = myResponse.getInt("nf_calories");
                    Log.d("CALORIES", responseCalories + " ");
                    responseCarbs = myResponse.getInt("nf_total_carbohydrate");
                    resppnseFat = myResponse.getInt("nf_total_fat");


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    private void initialRequest(final String url) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL urlObj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
                    con.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputline;
                    StringBuffer response = new StringBuffer();
                    while ((inputline = in.readLine()) != null) {
                        response.append(inputline);
                    }
                    in.close();


                    JSONObject myResponse = new JSONObject(response.toString());
                    responseString = myResponse.getJSONArray("hits").getJSONObject(0).getJSONObject("fields").getString("item_id");
                    hits = myResponse.getInt("total_hits");
                    Log.d("HITS", hits + "");
                    Log.d("THISMYID", responseString);

                } catch (ProtocolException e) {
                    Log.d("INITIAL PROTOCOL", e.getLocalizedMessage());


                } catch (MalformedURLException f) {
                    Log.d("INITIAL MALFORMED", f.getLocalizedMessage());

                } catch (IOException e) {
                    Log.d("INITIAL IO", e.getLocalizedMessage());

                } catch (JSONException e) {
                    Log.d("INITIAL JSON", e.getLocalizedMessage());

                }

            }
        }).start();


    }
}