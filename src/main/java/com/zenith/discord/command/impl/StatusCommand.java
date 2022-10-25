package com.zenith.discord.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.zenith.Proxy;
import com.zenith.cache.data.PlayerCache;
import com.zenith.discord.command.Command;
import com.zenith.discord.command.CommandContext;
import com.zenith.discord.command.CommandUsage;
import com.zenith.server.ServerConnection;
import com.zenith.util.Queue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.zenith.util.Constants.CACHE;
import static com.zenith.util.Constants.CONFIG;
import static java.util.Objects.nonNull;

public class StatusCommand extends Command {
    public static String getCoordinates(final PlayerCache playerCache) {
        if (CONFIG.discord.reportCoords) {
            return "||["
                    + (int) playerCache.getX() + ", "
                    + (int) playerCache.getY() + ", "
                    + (int) playerCache.getZ()
                    + "]||";
        } else {
            return "Coords disabled";
        }
    }

    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.simple(
                "status",
                "Gets the current proxy status"
        );
    }

    @Override
    public void register(CommandDispatcher<CommandContext> dispatcher) {
        dispatcher.register(
                command("status").executes(c -> {
                    final EmbedCreateSpec.Builder builder = c.getSource().getEmbedBuilder();
                    builder
                            .title("Proxy Status: " + CONFIG.authentication.username)
                            .color(Proxy.getInstance().isConnected() ? (Proxy.getInstance().isInQueue() ? Color.MOON_YELLOW : Color.CYAN) : Color.RUBY)
                            .addField("Status", getStatus(), true)
                            .addField("Connected User", getCurrentClientUserName(), true)
                            .addField("Online Time", getOnlineTime(), true)
                            .addField("Proxy IP", CONFIG.server.getProxyAddress(), true)
                            .addField("Server", CONFIG.client.server.address + ':' + CONFIG.client.server.port, true)
                            .addField("Priority Queue", (CONFIG.authentication.prio ? "yes" : "no") + " [" + (CONFIG.authentication.prioBanned ? "banned" : "unbanned") + "]", true);
                    if (!getSpectatorUserNames().isEmpty()) {
                        builder.addField("Spectators", String.join(", ", getSpectatorUserNames()), true);
                    }
                    builder.addField("2b2t Queue", getQueueStatus(), true)
                            .addField("Dimension",
                                    dimensionIdToString(CACHE.getPlayerCache().getDimension()),
                                    true);
                    if (CONFIG.discord.reportCoords) {
                        builder.addField("Coordinates", getCoordinates(CACHE.getPlayerCache()), true);
                    }
                    builder.addField("Health", "" + ((int) CACHE.getPlayerCache().getThePlayer().getHealth()), true)
                            .addField("AutoDisconnect",
                                    "[Health: " + (CONFIG.client.extra.utility.actions.autoDisconnect.enabled ? "on" : "off")
                                            + " (" + CONFIG.client.extra.utility.actions.autoDisconnect.health + ")]"
                                            + "\n[AutoClientDisconnect: " + (CONFIG.client.extra.utility.actions.autoDisconnect.autoClientDisconnect ? "on" : "off") + "]", true)
                            .addField("AutoReconnect",
                                    (CONFIG.client.extra.autoReconnect.enabled ? "on" : "off")
                                            + " [" + CONFIG.client.extra.autoReconnect.delaySeconds + "]", true)
                            .addField("AutoRespawn",
                                    (CONFIG.client.extra.autoRespawn.enabled ? "on" : "off")
                                            + " [" + CONFIG.client.extra.autoRespawn.delayMillis + "]", true)
                            .addField("AntiAFK",
                                    (CONFIG.client.extra.antiafk.enabled ? "on" : "off"), true)
                            .addField("VisualRange Notifications", (CONFIG.client.extra.visualRangeAlert ? "on" : "off")
                                    + "\n[Mention: " + (CONFIG.client.extra.visualRangeAlertMention ? "on" : "off") + "]", true)
                            .addField("Client Connection Notifications", (CONFIG.client.extra.clientConnectionMessages ? "on" : "off"), true)
                            .addField("Stalk", (CONFIG.client.extra.stalk.enabled ? "on" : "off"), true)
                            .addField("Spectators", (CONFIG.server.spectator.allowSpectator ? "on" : "off")
                                    + "\n[Public Chat: " + (CONFIG.server.spectator.spectatorPublicChatEnabled ? "on" : "off") + "]", true)
                            .addField("Active Hours", (CONFIG.client.extra.utility.actions.activeHours.enabled ? "on" : "off"), true)
                            .addField("Display Coordinates", (CONFIG.discord.reportCoords ? "on" : "off"), true)
                            .addField("Chat Relay", (CONFIG.discord.chatRelay.channelId.length() > 0 ? (CONFIG.discord.chatRelay.enable ? "on" : "off") : "Not Configured")
                                    + "\n[WhisperMention: " + (CONFIG.discord.chatRelay.mentionRoleOnWhisper ? "on" : "off") + "]"
                                    + "\n[NameMention: " + (CONFIG.discord.chatRelay.mentionRoleOnNameMention ? "on" : "off") + "]", true)
                            .addField("AutoReply", (CONFIG.client.extra.autoReply.enabled ? "on" : "off")
                                    + "\n[Cooldown: " + CONFIG.client.extra.autoReply.cooldownSeconds + "]", true)
                            .addField("AutoUpdate", (CONFIG.autoUpdate ? "on" : "off"), false);

                })
        );
    }

    private String getCurrentClientUserName() {
        ServerConnection currentConnection = Proxy.getInstance().getCurrentPlayer().get();
        if (nonNull(currentConnection)) {
            return currentConnection.getProfileCache().getProfile().getName();
        } else {
            return "None";
        }
    }

    private List<String> getSpectatorUserNames() {
        return Proxy.getInstance().getSpectatorConnections().stream()
                .map(connection -> connection.getProfileCache().getProfile().getName())
                .collect(Collectors.toList());
    }

    private String getStatus() {
        if (Proxy.getInstance().isConnected()) {
            if (Proxy.getInstance().isInQueue()) {
                if (Proxy.getInstance().getIsPrio().isPresent()) {
                    if (Proxy.getInstance().getIsPrio().get()) {
                        return "In Priority Queue [" + Proxy.getInstance().getQueuePosition() + " / " + Queue.getQueueStatus().prio + "]\nETA: " + Queue.getQueueEta(Queue.getQueueStatus().prio, Proxy.getInstance().getQueuePosition());
                    } else {
                        return "In Regular Queue [" + Proxy.getInstance().getQueuePosition() + " / " + Queue.getQueueStatus().regular + "]\nETA: " + Queue.getQueueEta(Queue.getQueueStatus().regular, Proxy.getInstance().getQueuePosition());
                    }
                } else {
                    return "Queueing";
                }
            } else {
                return "Online";
            }
        } else {
            return "Disconnected";
        }
    }

    private String getQueueStatus() {
        return "Priority: " + Queue.getQueueStatus().prio + " [" + Queue.getQueueEta(Queue.getQueueStatus().prio, Queue.getQueueStatus().prio) + "]"
                + "\nRegular: " + Queue.getQueueStatus().regular + " [" + Queue.getQueueEta(Queue.getQueueStatus().regular, Queue.getQueueStatus().regular) + "]";
    }

    private String dimensionIdToString(final int dimension) {
        if (dimension == 0) {
            return "Overworld";
        } else if (dimension == -1) {
            return "Nether";
        } else if (dimension == 1) {
            return "The End";
        } else {
            return "N/A";
        }
    }

    public String getOnlineTime() {
        if (Proxy.getInstance().isConnected()) {
            long secondsOnline = Instant.now().getEpochSecond() - Proxy.getInstance().getConnectTime().getEpochSecond();
            // hours:minutes:seconds
            return Queue.getEtaStringFromSeconds(secondsOnline);
        } else {
            return "Not Online!";
        }
    }
}