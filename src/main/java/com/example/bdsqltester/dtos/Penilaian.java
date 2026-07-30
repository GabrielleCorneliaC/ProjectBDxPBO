package com.example.bdsqltester.dtos;

public class Penilaian {
    private int penilaianId;
    private int transaksiId;
    private int pelangganId;
    private int rating;
    private String review;

    public Penilaian(int penilaianId, int transaksiId, int pelangganId, int rating, String review) {
        this.penilaianId = penilaianId;
        this.transaksiId = transaksiId;
        this.pelangganId = pelangganId;
        this.rating = rating;
        this.review = review;
    }

    public int getPenilaianId() {
        return penilaianId;
    }

    public int getTransaksiId() {
        return transaksiId;
    }

    public int getPelangganId() {
        return pelangganId;
    }

    public int getRating() {
        return rating;
    }

    public String getReview() {
        return review;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
