package models;


public class Suite{
    private int suiteId;
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

    public void setSuiteId(int suiteId) {
        this.suiteId = suiteId;
    }

    public int  getSuiteId() {
        return suiteId;
    }

    public String getSuiteNumber() {
        return suiteNumber;
    }
    
    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }
}