package models;


public class Guest{
    private int guestId;
    private int totalSpend;
    private String fullName;
    private String email;
    private String phone;
    private String loyaltyTier;
    public Guest(String fullName, String email, String phone, String loyaltyTier) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.loyaltyTier = loyaltyTier;
    }

    public void setGuestId(int id)
    {
        this.guestId = id;
    }
    public int getGuestId() {
        return guestId;
    }
    public String getfullName() {
        return fullName;
    }
    public void setfullName(String fullName) {
        this.fullName = fullName;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getloyaltyTier() {
        return loyaltyTier;
    }
    public void setloyaltyTier(String loyaltyTier) {
        this.loyaltyTier = loyaltyTier;
    }
    public void setTotalSpend(int spends)
    {
        this.totalSpend = spends;
    }
    public int getTotalSpend()
    {
        return this.totalSpend;
    }
}