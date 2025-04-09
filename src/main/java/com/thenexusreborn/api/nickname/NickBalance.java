package com.thenexusreborn.api.nickname;

import com.thenexusreborn.api.player.PlayerBalance;
import com.thenexusreborn.api.sql.annotations.column.ColumnIgnored;
import com.thenexusreborn.api.sql.annotations.table.TableName;

import java.util.UUID;

@TableName("nickbalances")
public class NickBalance extends PlayerBalance {
    
    private boolean persist;
    
    @ColumnIgnored
    private PlayerBalance trueBalance;
    
    public NickBalance(UUID uniqueId) {
        super(uniqueId);
    }
    
    public NickBalance(UUID uniqueId, double credits, double nexites, PlayerBalance trueBalance) {
        super(uniqueId);
        this.credits = credits;
        this.nexites = nexites;
        this.trueBalance = trueBalance;
    }
    
    protected NickBalance() {}
    
    public void setPersist(boolean persist) {
        this.persist = persist;
    }
    
    public boolean isPersist() {
        return persist;
    }
    
    @Override
    public double addNexites(double nexites) {
        this.trueBalance.addNexites(nexites);
        return super.addNexites(nexites);
    }
    
    @Override
    public double addCredits(double credits) {
        this.trueBalance.addCredits(credits);
        return super.addCredits(credits);
    }
    
    public void setTrueBalance(PlayerBalance trueBalance) {
        this.trueBalance = trueBalance;
    }
}
