package com.example.bdsqltester.dtos;

import java.sql.Time;
import java.util.Date;

public class Transaksi {
    private int transaksiId;
    private Date tanggal;
    private Time jam;
    private int total;
    private String status;
    private String pelanggan;
    private String ratingString;
    private String review;

    public Transaksi(int transaksiId, Date tanggal, Time jam, int total) {
        this.transaksiId = transaksiId;
        this.tanggal = tanggal;
        this.jam = jam;
        this.total = total;
    }
    public Transaksi(int transaksiId, Date tanggal, Time jam, int total, String pelanggan, String status) {
        this.transaksiId = transaksiId;
        this.tanggal = tanggal;
        this.jam = jam;
        this.total = total;
        this.pelanggan = pelanggan;
        this.status = status;
    }

    public int getTransaksiId() {
        return transaksiId;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    public Time getJam() {
        return jam;
    }

    public void setJam(Time jam) {
        this.jam = jam;
    }

    public int getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPelanggan() {
        return pelanggan;
    }

    public void setPelanggan(String pelanggan) {
        this.pelanggan = pelanggan;
    }
    public String getRatingString() {
        return ratingString;
    }

    public void setRatingString(String ratingString) {
        this.ratingString = ratingString;
    }
    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }


}
