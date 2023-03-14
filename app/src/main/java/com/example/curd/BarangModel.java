package com.example.curd;

import java.io.Serializable;
import java.util.Objects;

public class BarangModel implements Serializable {

    String key;
    String nama;
    String merk;
    String harga;

    public BarangModel(String key, String nama, String merk, String harga) {
        this.key = key;
        this.nama = nama;
        this.merk = merk;
        this.harga = harga;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BarangModel)) return false;
        BarangModel that = (BarangModel) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getMerk() {
        return merk;
    }

    public void setMerk(String merk) {
        this.merk = merk;
    }

    public String getHarga() {
        return harga;
    }

    public void setHarga(String harga) {
        this.harga = harga;
    }
}
