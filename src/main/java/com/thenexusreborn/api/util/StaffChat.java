package com.thenexusreborn.api.util;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.network.NetworkManager;
import com.thenexusreborn.api.network.cmd.NetworkCommand;
import com.thenexusreborn.api.player.*;
import com.thenexusreborn.api.punishment.*;

import java.sql.SQLException;
import java.util.*;

public final class StaffChat {
    /*
    The base format for the data or args for the staff chat network command is as follows
    
    staffchat <origin> <event> <data...>
    <origin> is the origin of the message, which is handled automatically by the networking api
    <event> is what happened, which is used for formatting
    <data..> is an array of the relevent data for the action
    
    Valid actions are: Chat, join, disconnect, toggle (incognito, vanish, staffmode), nickname (set, reset), punishment, report, anticheat
    Chat: staffchat <origin> chat <player> <message...>
    Join: staffchat <origin> join <player> - The server that is displayed is the origin
    Disconnect: staffchat <origin> disconnect <player>
    Anticheat: staffchat <origin> anticheat <player> <hack> <violation>
    Toggle: Base features not implemented
    Nickname: Base feature not implemented
    Punishment: staffchat <origin> punishment <id>
    Report: Base feature not implemented
     */
    
    private static final NetworkManager NETWORK = NexusAPI.getApi().getNetworkManager();
    
    public static final String PREFIX = "&2&l[&aSTAFF&2&l]";
    
    public static void handleIncoming(NetworkCommand cmd, String origin, String[] args) {
        String event = args[0];
        String format = "";
        String displayName = "";
        Rank rank = Rank.valueOf(args[2]);
        try {
            UUID uuid = UUID.fromString(args[1]);
            String name = NexusAPI.getApi().getPlayerManager().getNameFromUUID(uuid);
    
            displayName = rank.getColor() + name;
            if (event.equals("anticheat")) {
                String hack = args[2];
                int violation = Integer.parseInt(args[3]);
                format = "{prefix} &8[&9PMR&8] &8[&6{origin}&8] &r{displayName} &7is using &e" + hack + " &bVL:" + violation;
            }
        } catch (Exception e) {
            if (event.contains("punishment")) {
                int id = Integer.parseInt(args[1]);
                format = "{prefix} &6({origin}) &d{target} &fwas {type} &fby &b{actor} &ffor &3{reason}{length}";
                
                Punishment punishment = NexusAPI.getApi().getPunishmentManager().getPunishment(id);
                if (punishment == null) {
                    try {
                        punishment = NexusAPI.getApi().getPrimaryDatabase().get(Punishment.class, "id", id).get(0);
                        NexusAPI.getApi().getPunishmentManager().addPunishment(punishment);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                
                if (event.startsWith("remove")) {
                    if (punishment.getPardonInfo() == null) {
                        try {
                            punishment = NexusAPI.getApi().getPrimaryDatabase().get(Punishment.class, "id", id).get(0);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                        NexusAPI.getApi().getPunishmentManager().addPunishment(punishment);
                    }
                }
                
                if (punishment == null) {
                    NexusAPI.getApi().getLogger().severe("Received staff chat incoming message for punishment " + id + " but none could be found.");
                    return;
                }
                
                if (event.startsWith("remove")) {
                    format = format.replace("{type}", punishment.getType().getColor() + "un" + punishment.getType().getVerb());
                    format = format.replace("{length}", "");
                    format = format.replace("{reason}", punishment.getPardonInfo().getReason());
                    format = format.replace("{actor}", punishment.getPardonInfo().getActorNameCache());
                } else {
                    format = format.replace("{type}", punishment.getType().getColor() + punishment.getType().getVerb());
                    if (punishment.getType() != PunishmentType.WARN) {
                        format = format.replace("{length}", " &c(" + punishment.formatTimeLeft() + ")");
                    } else {
                        format = format.replace("{length}", "");
                    }
                    format = format.replace("{reason}", punishment.getReason());
                    format = format.replace("{actor}", punishment.getActorNameCache());
                }
                format = format.replace("{target}", punishment.getTargetNameCache());
            }
        }
        
        format = format.replace("{prefix}", PREFIX).replace("{displayName}", displayName).replace("{origin}", origin);
    
        for (NexusPlayer player : new ArrayList<>(NexusAPI.getApi().getPlayerManager().getPlayers().values())) {
            if (player.isOnline()) {
                if (player.getRank().ordinal() <= Rank.HELPER.ordinal()) {
                    player.sendMessage(format);
                }
            }
        }
    }
    
    public static void sendPunishment(Punishment punishment) {
        NETWORK.send("staffchat", "punishment", punishment.getId() + "");
    }
    
    public static void sendPunishmentRemoval(Punishment punishment) {
        NETWORK.send("staffchat", "removepunishment", punishment.getId() + "");
    }
    
    public static void sendAnticheat(NexusPlayer player, String hack, int violation) {
        String[] args = {"anticheat", player.getUniqueId().toString(), player.getRank().name(), hack, violation + ""};
        NETWORK.send("staffchat", args);
    }
}
