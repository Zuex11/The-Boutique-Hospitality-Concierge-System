package models;

import java.time.LocalDate;

public class LoyaltyTierLog {

    private int logId;
    private int guestId;
    private String oldTier;
    private String newTier;
    private LocalDate changedDate;

    public LoyaltyTierLog(int guestId, String oldTier, String newTier) {
        this.guestId = guestId;
        this.oldTier = oldTier;
        this.newTier = newTier;
        this.changedDate = LocalDate.now();
    }

    public int getLogId() 
    { 
        return logId; 
    }
    public void setLogId(int logId) 
    { 
        this.logId = logId; 
    }
    public int getGuestId() 
    { 
        return guestId; 
    }
    public String getOldTier() 
    {
        return oldTier; 
    }
    public String getNewTier() 
    { 
        return newTier; 
    }
    public LocalDate getChangedDate() 
    { 
        return changedDate; 
    }
}