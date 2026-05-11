package models;


public class Hotel{
    private int hotelId;
    private String name;
    private String theme;
    private String location;
    private int totalSuites;
    public Hotel(String name, String theme, String location, int totalSuites){
        this.name = name;
        this.theme = theme;
        this.location = location;
        this.totalSuites = totalSuites;
    }
    public int getHotelId() {
        return hotelId;
    }

	public void setHotelId(int hotelId) {
		this.hotelId = hotelId;
	}
	
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTheme() {
        return theme;
    }
    public void setTheme(String theme) {
        this.theme = theme;
    }
    public String getLocation() {
        return location;
    }
    public int  getTotalSuites() {
        return totalSuites;
    }
    public void setTotalSuites(int totalSuites) {
        this.totalSuites = totalSuites;
    }
}
