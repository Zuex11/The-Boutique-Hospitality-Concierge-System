package models;


public class GuestExperience{
    private int experienceId;
    private String experienceName;
    private String description;
    private double baseCost;
    public GuestExperience(String experienceName, String description, double baseCost) {
        this.experienceName = experienceName;
        this.description = description;
        this.baseCost = baseCost;
    }
    public int getExperienceId() {
        return experienceId;
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