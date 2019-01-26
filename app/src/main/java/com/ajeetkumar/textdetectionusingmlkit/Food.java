package com.ajeetkumar.textdetectionusingmlkit;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Food implements Comparable<Food>, Parcelable {
    private String name;
    private int calories;
    private int fat;
    private int carbs;
    private RectF boundingBox;
    private Point[] corners;
    private boolean edible;
    private String compareWith = "fat";
    private int mData;

    public Food(String name, int calories, int fat, int carbs, RectF boundingBox, Point[] corners) {
        this.name = name;
        this.calories = calories;
        this.fat = fat;
        this.carbs = carbs;
        this.boundingBox = boundingBox;
        this.corners = corners;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getFat() {
        return fat;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public int getCarbs() {
        return carbs;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    public RectF getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(RectF boundingBox) {
        this.boundingBox = boundingBox;
    }

    public Point[] getCorners() {
        return corners;
    }

    public void setCorners(Point[] corners) {
        this.corners = corners;
    }

    public boolean isEdible() {
        return edible;
    }

    public void setEdible(boolean edible) {
        this.edible = edible;
    }

    public String getCompareWith() {
        return compareWith;
    }

    public void setCompareWith(String compareWith) {
        this.compareWith = compareWith;
    }

    @Override
    public int compareTo(@NonNull Food food) {
        if (compareWith.equals("fat")) {
            return food.getFat() - this.getFat();
        } else {
            return food.getCarbs() - this.getCarbs();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(calories);
        parcel.writeInt(fat);
        parcel.writeInt(carbs);
        parcel.writeString(compareWith);
        parcel.writeByte((byte) (edible ? 1 : 0));

    }

    public static final Parcelable.Creator<Food> CREATOR
            = new Parcelable.Creator<Food>() {
        public Food createFromParcel(Parcel in) {
            return new Food(in);
        }
        public Food[] newArray(int size) {
            return new Food[size];
        }
    };

    private Food(Parcel in) {
        name = in.readString();
        calories = in.readInt();
        fat = in.readInt();
        carbs = in.readInt();
        compareWith = in.readString();
        edible = in.readByte() != 0;





    }

   /* private String name;
    private int calories;
    private int fat;
    private int carbs;
    private Rect boundingBox;
    private Point[] corners;
    private boolean edible;
    private String compareWith = "fat";
    private int mData;*/
}
