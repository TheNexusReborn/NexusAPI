package com.thenexusreborn.api.player;

import me.firestar311.starsql.api.annotations.table.TableName;

import java.util.Objects;
import java.util.UUID;

@TableName("nexusteammembers")
public class NexusTeamMember {
    private long id;
    private UUID uniqueId;
    
    private NexusTeamMember() {}

    public NexusTeamMember(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        NexusTeamMember that = (NexusTeamMember) object;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
}