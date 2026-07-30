package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Cabang {
    private long cabangID;
    private String cabangNama;
    private String cabangAlamat;
    private String cabangKontak;
    private boolean cabangStatus;

    public Cabang(long cabangID, String cabangNama, String cabangAlamat, String cabangKontak, boolean cabangStatus) {
        this.cabangID = cabangID;
        this.cabangNama = cabangNama;
        this.cabangAlamat = cabangAlamat;
        this.cabangKontak = cabangKontak;
        this.cabangStatus = cabangStatus;
    }

    public Cabang(long cabangID, String cabangNama, String cabangAlamat, String cabangKontak) {
        this(cabangID, cabangNama, cabangAlamat, cabangKontak, true);
    }

    public Cabang(ResultSet rs) throws SQLException {
        this.cabangID = rs.getLong("cabang_id");
        this.cabangNama = rs.getString("cabang_nama");
        this.cabangAlamat = rs.getString("cabang_alamat");
        this.cabangKontak = rs.getString("cabang_kontak");
        this.cabangStatus = rs.getBoolean("cabang_status");
    }
    public long getCabangID() {
        return cabangID;
    }

    public void setCabangID(long cabangID) {
        this.cabangID = cabangID;
    }

    public String getCabangNama() {
        return cabangNama;
    }

    public void setCabangNama(String cabangNama) {
        this.cabangNama = cabangNama;
    }

    public String getCabangAlamat() {
        return cabangAlamat;
    }

    public void setCabangAlamat(String cabangAlamat) {
        this.cabangAlamat = cabangAlamat;
    }

    public String getCabangKontak() {
        return cabangKontak;
    }

    public void setCabangKontak(String cabangKontak) {
        this.cabangKontak = cabangKontak;
    }

    public boolean isCabangStatus() {
        return cabangStatus;
    }

    public void setCabangStatus(boolean cabangStatus) {
        this.cabangStatus = cabangStatus;
    }

    @Override
    public String toString() {
        return cabangNama;
    }
}
