package com.thenexusreborn.api.nickname;

import com.thenexusreborn.api.sql.annotations.column.PrimaryKey;
import com.thenexusreborn.api.sql.annotations.table.TableName;

@TableName("nameblacklist")
public class NameBlacklistEntry {
    @PrimaryKey
    private String name;
    
    private NameBlacklistEntry() {}
    
    public NameBlacklistEntry(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}