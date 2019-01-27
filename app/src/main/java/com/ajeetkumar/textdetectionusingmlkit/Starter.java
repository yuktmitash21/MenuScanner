package com.ajeetkumar.textdetectionusingmlkit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import java.util.ArrayList;

public class Starter extends AppCompatActivity {
    private CardView cardView;
    private ArrayList<String> dietHabits;
    private ArrayList<String> dietRestrictions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        dietHabits = new ArrayList<>();
        dietRestrictions = new ArrayList<>();



        cardView = findViewById(R.id.cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] restrictions = {"Vegan", "Vegetarian", "Gluten-Free", "Kosher", "Nut Allergy", "Shellfish", "None"};
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(Starter.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(Starter.this);
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
                            builder2 = new AlertDialog.Builder(Starter.this, android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder2 = new AlertDialog.Builder(Starter.this);
                        }

                        final String[] diet = {"Atkins", "Low-Fat", "None"};
                        builder2.setTitle("Pick a dieting style").setMultiChoiceItems(diet, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                dietHabits.add(diet[i]);
                            }
                        }).setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("restrictions", dietRestrictions);
                                intent.putExtra("habits", dietHabits);
                                startActivity(intent);

                            }
                        }).show();

                    }
                }).show();

            }
        });
    }
}
