package com.example.bdsqltester.dtos;

public class Staff {
    private int staffId;
    private String nama;
    private String email;
    private String notelp;
    private String password;
    private String tanggalMasuk;
    private int gaji;
    private int roleId;
    private int cabangId;
    private boolean staffStatus;

    public Staff(int staffId, String nama, String email, String notelp, String password,
                 String tanggalMasuk, int gaji, int roleId, int cabangId) {
        this(staffId, nama, email, notelp, password, tanggalMasuk, gaji, roleId, cabangId, true);
    }

    public Staff(int staffId, String nama, String email, String notelp, String password,
                 String tanggalMasuk, int gaji, int roleId, int cabangId, boolean staffStatus) {
        this.staffId      = staffId;
        this.nama         = nama;
        this.email        = email;
        this.notelp       = notelp;
        this.password     = password;
        this.tanggalMasuk = tanggalMasuk;
        this.gaji         = gaji;
        this.roleId       = roleId;
        this.cabangId     = cabangId;
        this.staffStatus  = staffStatus;
    }

    public int getStaffId()    { return staffId; }
    public String getNama()    { return nama; }
    public String getEmail()   { return email; }
    public String getNotelp()  { return notelp; }
    public String getPassword(){ return password; }
    public String getTanggalMasuk() { return tanggalMasuk; }
    public int getGaji()       { return gaji; }
    public int getRoleId()     { return roleId; }
    public int getCabangId()   { return cabangId; }
    public boolean isStaffStatus() { return staffStatus; }

    public void setTanggalMasuk(String tanggalMasuk) { this.tanggalMasuk = tanggalMasuk; }
    public void setGaji(int gaji)         { this.gaji = gaji; }
    public void setRoleId(int roleId)     { this.roleId = roleId; }
    public void setCabangId(int cabangId) { this.cabangId = cabangId; }
    public void setStaffStatus(boolean staffStatus) { this.staffStatus = staffStatus; }
}
