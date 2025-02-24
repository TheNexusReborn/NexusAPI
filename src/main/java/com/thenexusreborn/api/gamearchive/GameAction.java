package com.thenexusreborn.api.gamearchive;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.sql.annotations.column.ColumnCodec;
import com.thenexusreborn.api.sql.annotations.column.ColumnType;
import com.thenexusreborn.api.sql.annotations.table.TableName;
import com.thenexusreborn.api.sql.objects.codecs.ValueDataCodec;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("ComparatorMethodParameterNotUsed")
@TableName("gameactions")
public class GameAction implements Comparable<GameAction> {
    public static final int CURRENT_VESRION = 2;
    
    private long id;
    private int version = CURRENT_VESRION;
    private long gameId;
    private long timestamp;
    private String type, niceValue;
    @ColumnCodec(ValueDataCodec.class)
    @ColumnType("varchar(10000)")
    private Map<String, String> valueData = new LinkedHashMap<>();
    private String scope;
    
    private GameAction() {}
    
    public GameAction(long gameId, long timestamp, String type) {
        this.gameId = gameId;
        this.timestamp = timestamp;
        this.type = type;
        this.scope = "normal";
    }
    
    public GameAction(long timestamp, String type) {
        this.timestamp = timestamp;
        this.type = type;
        this.scope = "normal";
    }
    
    public GameAction(JsonObject json) {
        this.id = json.get("id").getAsLong();
        this.timestamp = json.get("timestamp").getAsLong();
        this.type = json.get("type").getAsString();
        this.version = json.get("version").getAsInt();
        
        JsonObject dataObject = json.getAsJsonObject("data");
        for (Map.Entry<String, JsonElement> dataEntry : dataObject.entrySet()) {
            this.valueData.put(dataEntry.getKey(), dataEntry.getValue().getAsString());
        }
    }
    
    public JsonObject toJson() {
        JsonObject actionObject = new JsonObject();
        actionObject.addProperty("id", this.id);
        actionObject.addProperty("timestamp", getTimestamp());
        actionObject.addProperty("type", getType());
        if (getVersion() == 1) {
            convertFromV1toV2();
            try {
                NexusAPI.getApi().getPrimaryDatabase().save(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        actionObject.addProperty("version", getVersion());
        JsonObject dataObject = new JsonObject();
        getValueData().forEach(dataObject::addProperty);
        actionObject.add("data", dataObject);
        return actionObject;
    }

    public String getScope() {
        return scope;
    }

    public GameAction setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public String getNiceValue() {
        return niceValue;
    }

    public void convertFromV1toV2() {
        valueData.clear();
        if (getType().equalsIgnoreCase("chat") || getType().equalsIgnoreCase("deadchat")) {
            String[] value = niceValue.split(":");
            valueData.put("sender", value[0]);
            StringBuilder msg = new StringBuilder();
            for (int i = 1; i < value.length; i++) {
                msg.append(value[i]);
            }
            valueData.put("message", msg.toString());
        } else if (getType().equalsIgnoreCase("mutation")) {
            String[] rawValue = niceValue.split(" ");
            valueData.put("mutator", rawValue[0]);
            valueData.put("target", rawValue[3]);
            if (rawValue.length > 6) {
                StringBuilder typeBuilder = new StringBuilder();
                for (int i = 6; i < rawValue.length; i++) {
                    typeBuilder.append(rawValue[i]).append(" ");
                }
                valueData.put("type", typeBuilder.toString().trim().toLowerCase().replace(" ", "_"));
            }
        } else if (getType().equalsIgnoreCase("assist")) {
            String[] rawValue = niceValue.split(" ");
            valueData.put("assistor", rawValue[0]);
            valueData.put("deadplayer", rawValue[5]);
        } else if (getType().equalsIgnoreCase("admincommand")) {
            String[] rawValue = niceValue.split(" ");
            if (niceValue.contains(" ran the ")) {
                valueData.put("sender", rawValue[0]);
                valueData.put("command", rawValue[3]);
            } else if (niceValue.contains(" gave <")) {
                this.type = "giveitem";
                valueData.put("sender", rawValue[0]);
                valueData.put("target", rawValue[2]);
                StringBuilder item = new StringBuilder();
                for (int i = 3; i < rawValue.length; i++) {
                    item.append(rawValue[i]).append(" ");
                }
                valueData.put("item", item.toString().trim().toLowerCase().replace(" ", "_"));
            } else if (niceValue.contains(" gave all players ")) {
                this.type = "givealll";
                valueData.put("sender", rawValue[0]);
                StringBuilder item = new StringBuilder();
                for (int i = 4; i < rawValue.length; i++) {
                    item.append(rawValue[i]).append(" ");
                }
                valueData.put("item", item.toString().trim().toLowerCase().replace(" ", "_"));
            }
        } else if (getType().equalsIgnoreCase("death")) {
            valueData.put("deathmessage", this.niceValue);
        } else if (getType().equalsIgnoreCase("statechange")) {
            valueData.put("newvalue", this.niceValue);
        } else {
            valueData.put("value", this.niceValue);
        }

        this.version = 2;
    }
    
    public GameAction addValueData(String key, Object value) {
        if (value == null) {
            System.out.println("Null data for key " + key + " while adding value data to Game Action");
            return null;
        }
        
        valueData.put(key, value.toString());
        return this;
    }

    public Map<String, String> getValueData() {
        return valueData;
    }

    public int getVersion() {
        return version;
    }

    public long getGameId() {
        return gameId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getType() {
        return type;
    }
    
    public void setGameId(long gameId) {
        this.gameId = gameId;
    }
    
    @Override
    public int compareTo(GameAction o) {
        if (this.timestamp > o.timestamp) {
            return 1;
        }
    
        return -1;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameAction that = (GameAction) o;
        return gameId == that.gameId && timestamp == that.timestamp && Objects.equals(type, that.type) && Objects.equals(niceValue, that.niceValue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(gameId, timestamp, type, niceValue);
    }
}
