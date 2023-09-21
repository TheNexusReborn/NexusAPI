package com.thenexusreborn.api.storage.handler;

import com.thenexusreborn.api.gamearchive.*;
import me.firestar311.starsql.api.objects.ObjectHandler;
import me.firestar311.starsql.api.objects.SQLDatabase;
import me.firestar311.starsql.api.objects.Table;

import java.sql.SQLException;
import java.util.List;

public class GamesObjectHandler extends ObjectHandler {
    public GamesObjectHandler(Object object, SQLDatabase database, Table table) {
        super(object, database, table);
    }
    
    @Override
    public void afterLoad() {
        GameInfo gameInfo = (GameInfo) object;
        try {
            List<GameAction> gameActions = database.get(GameAction.class, "gameId", gameInfo.getId());
            gameInfo.getActions().addAll(gameActions);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void afterSave() {
        GameInfo gameInfo = (GameInfo) object;
        for (GameAction action : gameInfo.getActions()) {
            action.setGameId(gameInfo.getId());
            database.saveSilent(action);
        }
    }
}
