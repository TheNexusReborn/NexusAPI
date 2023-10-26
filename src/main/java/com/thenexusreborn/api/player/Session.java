package com.thenexusreborn.api.player;

import me.firestar311.starsql.api.annotations.table.TableName;

import java.util.UUID;

@TableName("sessions")
public class Session {
    private long id;
    private UUID uniqueId;
    private long start, end;
    private int gamesPlayed;

    private Session() {
    }

    public Session(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void start() {
        this.start = System.currentTimeMillis();
    }

    public void end() {
        this.end = System.currentTimeMillis();
    }

    public long getTimeOnline() {
        if (this.end == 0) {
           return System.currentTimeMillis() - this.start;
        }

        return this.end - this.start;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public int getGamesPlayed() {
        return this.gamesPlayed;
    }
}
