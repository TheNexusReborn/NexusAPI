package com.thenexusreborn.api.nickname;

import com.thenexusreborn.api.player.PlayerTime;
import com.thenexusreborn.api.sql.annotations.column.ColumnIgnored;
import com.thenexusreborn.api.sql.annotations.table.TableName;

import java.util.UUID;

@TableName("nicktimes")
public class NickTime extends PlayerTime {
    
    @ColumnIgnored
    private PlayerTime trueTime;
    
    protected NickTime() {
    }
    
    public NickTime(UUID uniqueId) {
        super(uniqueId);
    }
    
    public NickTime(UUID uniqueId, long playTime, PlayerTime trueTime) {
        super(uniqueId);
        this.playtime = playTime;
        this.firstJoined = System.currentTimeMillis() - playtime;
        this.trueTime = trueTime;
    }
    
    @Override
    public long addPlaytime(long playtime) {
        trueTime.addPlaytime(playtime);
        return super.addPlaytime(playtime);
    }
    
    public PlayerTime getTrueTime() {
        return trueTime;
    }
    
    public void setTrueTime(PlayerTime trueTime) {
        this.trueTime = trueTime;
    }
}
