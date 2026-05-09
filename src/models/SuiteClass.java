package models;


public SuiteClass{
    private int classId;
    private String className;
    private double nightlyRate;
    private String amenities;
    public SuiteClass(String className, double nightlyRate, String amenities){
        this.className = className;
        this.nightlyRate = nightlyRate;
        this.amenities = amenities;
    }
    public int getClassId(){
        return classId;
    }
    public String getClassName()}{
        return className;
}
    public void setClassName(String className) {
this.className = className;
}
public double getNightlyRate(){
    return nightlyRate;
}
public void setNightlyRate(double nightlyRate) {
this.nightlyRate = nightlyRate;
}
public String getAmenities(){
    return amenities;
}
public void setAmenities(String amenities) {
this.amenities = amenities;
}
}