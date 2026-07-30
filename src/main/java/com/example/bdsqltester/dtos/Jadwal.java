package com.example.bdsqltester.dtos;

import java.sql.Timestamp;
import java.time.LocalDate;

public class Jadwal {
    private int jadwalId;
    private LocalDate jadwalTanggal;
    private Timestamp jadwalJamkirim;
    private Timestamp jadwalJamsampai;
    private int pengirimanId;
    private String jenisPengiriman;

    public Jadwal(int jadwalId, LocalDate jadwalTanggal, Timestamp jadwalJamkirim, Timestamp jadwalJamsampai, int pengirimanId, String jenisPengiriman) {
        this.jadwalId = jadwalId;
        this.jadwalTanggal = jadwalTanggal;
        this.jadwalJamkirim = jadwalJamkirim;
        this.jadwalJamsampai = jadwalJamsampai;
        this.pengirimanId = pengirimanId;
        this.jenisPengiriman = jenisPengiriman;
    }

    public int getJadwalId() {
        return jadwalId;
    }

    public void setJadwalId(int jadwalId) {
        this.jadwalId = jadwalId;
    }

    public LocalDate getJadwalTanggal() {
        return jadwalTanggal;
    }

    public void setJadwalTanggal(LocalDate jadwalTanggal) {
        this.jadwalTanggal = jadwalTanggal;
    }

    public Timestamp getJadwalJamkirim() {
        return jadwalJamkirim;
    }

    public void setJadwalJamkirim(Timestamp jadwalJamkirim) {
        this.jadwalJamkirim = jadwalJamkirim;
    }

    public Timestamp getJadwalJamsampai() {
        return jadwalJamsampai;
    }

    public void setJadwalJamsampai(Timestamp jadwalJamsampai) {
        this.jadwalJamsampai = jadwalJamsampai;
    }

    public int getPengirimanId() {
        return pengirimanId;
    }

    public void setPengirimanId(int pengirimanId) {
        this.pengirimanId = pengirimanId;
    }

    public String getJenisPengiriman() {
        return jenisPengiriman;
    }

    public void setJenisPengiriman(String jenisPengiriman) {
        this.jenisPengiriman = jenisPengiriman;
    }

    @Override
    public String toString() {
        return "Jadwal{" +
                "jadwalId=" + jadwalId +
                ", jadwalTanggal=" + jadwalTanggal +
                ", jadwalJamkirim=" + jadwalJamkirim +
                ", jadwalJamsampai=" + jadwalJamsampai +
                ", pengirimanId=" + pengirimanId +
                ", jenisPengiriman='" + jenisPengiriman + '\'' +
                '}';
    }
}
