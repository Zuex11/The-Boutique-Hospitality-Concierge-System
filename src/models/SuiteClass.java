package models;

public class SuiteClass {
    private int classId;
    private int hotelId;
    private String className;
    private double nightlyRate;
    private String amenities;

    public SuiteClass(int hotelId, String className, double nightlyRate, String amenities) {
        this.hotelId = hotelId;
        this.className = className;
        this.nightlyRate = nightlyRate;
        this.amenities = amenities;
    }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }
    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public double getNightlyRate() { return nightlyRate; }
    public void setNightlyRate(double nightlyRate) { this.nightlyRate = nightlyRate; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
}