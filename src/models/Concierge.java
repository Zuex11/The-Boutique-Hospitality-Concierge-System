package models;


public class Concierge {
    private int conciergeId;
    private String fullName;
    private int hotelId;
    public Concierge(String fullName, int hotelId) {
        this.fullName = fullName;
        this.hotelId = hotelId;
    }
    public int getHotelId() {
        return hotelId;
    }
    public int getConciergeId() {
        return conciergeId;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName() {
        this.fullName = fullName;
    }
}