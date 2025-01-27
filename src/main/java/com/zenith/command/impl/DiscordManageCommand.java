package com.zenith.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.command.Command;
import com.zenith.command.CommandContext;
import com.zenith.command.CommandUsage;
import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.zenith.Shared.*;
import static com.zenith.command.CustomStringArgumentType.getString;
import static com.zenith.command.CustomStringArgumentType.wordWithChars;
import static java.util.Arrays.asList;

public class DiscordManageCommand extends Command {
    private static final Pattern CHANNEL_ID_PATTERN = Pattern.compile("<#\\d+>");

    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.args(
            "discord",
            "Manages the discord bot configuration",
            asList(
                "channel <channel ID>",
                "relayChannel <channel ID>",
                "manageProfileImage on/off",
                "manageNickname on/off",
                "manageDescription on/off"
            )
        );
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("discord")
            .then(literal("channel").requires(Command::validateAccountOwner)
                .then(argument("channel ID", wordWithChars()).executes(c -> {
                    String channelId = getString(c, "channel ID");
                    if (CHANNEL_ID_PATTERN.matcher(channelId).matches())
                        channelId = channelId.substring(2, channelId.length() - 1);
                    try {
                        Snowflake.of(channelId);
                    } catch (final Exception e) {
                        // invalid id
                        c.getSource().getEmbedBuilder()
                            .title("Invalid Channel ID")
                            .description("The channel ID provided is invalid")
                            .color(Color.RUBY);
                        return 1;
                    }
                    if (channelId.equals(CONFIG.discord.chatRelay.channelId)) {
                        c.getSource().getEmbedBuilder()
                            .title("Invalid Channel ID")
                            .description("Cannot use the same channel ID for both the relay and main channel")
                            .color(Color.RUBY);
                        return 1;
                    }
                    CONFIG.discord.channelId = channelId;
                    populate(c.getSource().getEmbedBuilder()
                                 .title("Channel set!")
                                 .color(Color.CYAN)
                                 .description("Discord bot will now restart if enabled"));
                    if (DISCORD_BOT.isRunning())
                        SCHEDULED_EXECUTOR_SERVICE.schedule(this::restartDiscordBot, 3, TimeUnit.SECONDS);
                    return 1;
                    })))
            .then(literal("relaychannel").requires(Command::validateAccountOwner)
                      .then(argument("channel ID", wordWithChars()).executes(c -> {
                          String channelId = getString(c, "channel ID");
                          if (CHANNEL_ID_PATTERN.matcher(channelId).matches())
                              channelId = channelId.substring(2, channelId.length() - 1);
                          try {
                              Snowflake.of(channelId);
                          } catch (final Exception e) {
                              // invalid id
                              c.getSource().getEmbedBuilder()
                                  .title("Invalid Channel ID")
                                  .description("The channel ID provided is invalid")
                                  .color(Color.RUBY);
                              return 1;
                          }
                          if (channelId.equals(CONFIG.discord.channelId)) {
                              c.getSource().getEmbedBuilder()
                                  .title("Invalid Channel ID")
                                  .description("Cannot use the same channel ID for both the relay and main channel")
                                  .color(Color.RUBY);
                              return 1;
                          }
                          CONFIG.discord.chatRelay.channelId = channelId;
                          populate(c.getSource().getEmbedBuilder()
                                       .title("Relay Channel set!")
                                       .color(Color.CYAN)
                                       .description("Discord bot will now restart if enabled"));
                          if (DISCORD_BOT.isRunning())
                              SCHEDULED_EXECUTOR_SERVICE.schedule(this::restartDiscordBot, 3, TimeUnit.SECONDS);
                          return 1;
                      })))
            .then(literal("manageprofileimage")
                      .then(literal("on").executes(c -> {
                            CONFIG.discord.manageProfileImage = true;
                            populate(c.getSource().getEmbedBuilder()
                                         .color(Color.CYAN)
                                         .title("Manage Profile Image On!"));
                      }))
                      .then(literal("off").executes(c -> {
                            CONFIG.discord.manageProfileImage = false;
                            populate(c.getSource().getEmbedBuilder()
                                         .color(Color.CYAN)
                                         .title("Manage Profile Image Off!"));
                      })))
            .then(literal("managenickname")
                      .then(literal("on").executes(c -> {
                            CONFIG.discord.manageNickname = true;
                            populate(c.getSource().getEmbedBuilder()
                                         .color(Color.CYAN)
                                         .title("Manage Nickname On!"));
                      }))
                      .then(literal("off").executes(c -> {
                            CONFIG.discord.manageNickname = false;
                            populate(c.getSource().getEmbedBuilder()
                                         .color(Color.CYAN)
                                         .title("Manage Nickname Off!"));
                      })))
            .then(literal("managedescription")
                      .then(literal("on").executes(c -> {
                            CONFIG.discord.manageDescription = true;
                            populate(c.getSource().getEmbedBuilder()
                                         .color(Color.CYAN)
                                         .title("Manage Description On!"));
                      }))
                      .then(literal("off").executes(c -> {
                          CONFIG.discord.manageDescription = false;
                          populate(c.getSource().getEmbedBuilder()
                                       .color(Color.CYAN)
                                       .title("Manage Description Off!"));
                        })));
    }

    private EmbedCreateSpec.Builder populate(final EmbedCreateSpec.Builder builder) {
        return builder
            .addField("Channel ID", "<#" + CONFIG.discord.channelId + ">", false)
            .addField("Relay Channel ID", "<#" + CONFIG.discord.chatRelay.channelId + ">", false)
            .addField("Manage Profile Image", CONFIG.discord.manageProfileImage ? "on" : "off", false)
            .addField("Manage Nickname", CONFIG.discord.manageNickname ? "on" : "off", false)
            .addField("Manage Description", CONFIG.discord.manageDescription ? "on" : "off", false);
    }

    private void restartDiscordBot() {
        DISCORD_LOG.info("Restarting discord bot");
        try {
            DISCORD_BOT.stop(false);
            DISCORD_BOT.start();
            DISCORD_BOT.sendEmbedMessage(EmbedCreateSpec.builder()
                                             .title("Discord Bot Restarted")
                                             .color(Color.GREEN)
                                             .build());
        } catch (final Exception e) {
            DISCORD_LOG.error("Failed to restart discord bot", e);
        }
    }
}
