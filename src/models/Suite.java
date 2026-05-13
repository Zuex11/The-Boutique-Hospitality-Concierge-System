package models;

public class Suite {
    private int suiteId;
    private int hotelId;
    private int classId;
    private String suiteNumber;
    private String hotelName;

    public Suite(int hotelId, int classId, String suiteNumber) {
        this.hotelId = hotelId;
        this.classId = classId;
        this.suiteNumber = suiteNumber;
    }

    public int getHotelId()          { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    public int getClassId()          { return classId; }

    public int getSuiteId()          { return suiteId; }
    public void setSuiteId(int suiteId) { this.suiteId = suiteId; }

    public String getSuiteNumber()   { return suiteNumber; }

    public String getHotelName()     { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
}