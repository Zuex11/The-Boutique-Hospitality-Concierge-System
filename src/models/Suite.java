package models;


public class Suite{
    private int sutieId;
    private int hotelId;
    private int classId;
    private String suiteNumber;
    public Suite(int hotelId, int classId, String suiteNumber){
        this.hotelId = hotelId;
        this.classId = classId;
        this.suiteNumber = suiteNumber;
    }
    public int getHotelId() {
        return hotelId;
    }

    public int getClassId() {
        return classId;
    }

    public void setSutieId(int sutieId) {
        this.sutieId = sutieId;
    }

    public int  getSutieId() {
        return sutieId;
    }

    public String getSuiteNumber() {
        return suiteNumber;
    }
    
    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }
}