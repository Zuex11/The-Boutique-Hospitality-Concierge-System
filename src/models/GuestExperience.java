package models;


public class GuestExperience{
    private int experienceId;
    private int hotelId;
    private String experienceName;
    private String description;
    private double baseCost;
    public GuestExperience(int hotelId, String experienceName, String description, double baseCost) {
        this.hotelId = hotelId;
        this.experienceName = experienceName;
        this.description = description;
        this.baseCost = baseCost;
    }
    
    public void setExperienceId(int experienceId) {
        this.experienceId = experienceId;
    }

    public int getExperienceId() {
        return experienceId;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public int getHotelId() {
        return hotelId;
    }

    public String getExperienceName() {
        return experienceName;
    }

    public void setExperienceName(String experienceName) {
        this.experienceName = experienceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBaseCost() {
        return baseCost;
    }

    public void setBaseCost(double baseCost) {
        this.baseCost = baseCost;
    }
}