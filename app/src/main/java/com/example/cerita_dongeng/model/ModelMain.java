package com.example.cerita_dongeng.model;

import java.io.Serializable;
import java.util.Comparator;



public class ModelMain implements Serializable {

    String id;
    String strJudul;
    String strCerita;
    private String imagePath;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getStrJudul() {
        return strJudul;
    }

    public void setStrJudul(String strJudul) {
        this.strJudul = strJudul;
    }

    public String getStrCerita() {
        return strCerita;
    }

    public void setStrCerita(String strCerita) {
        this.strCerita = strCerita;
    }

    public static Comparator<ModelMain> sortByAsc = new Comparator<ModelMain>() {
        @Override
        public int compare(ModelMain o1, ModelMain o2) {
            return o1.strJudul.compareTo(o2.strJudul);
        }
    };
}
