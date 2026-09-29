package dev.xf3d3.ultimatereports.discord;

import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.discord.bot.ButtonListener;
import dev.xf3d3.ultimatereports.discord.bot.DiscordBotSender;
import dev.xf3d3.ultimatereports.discord.bot.ModalListener;
import dev.xf3d3.ultimatereports.discord.bot.SlashCommandListener;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class DiscordBot {
    private final UltimateReports plugin;

    @Getter
    private final DiscordBotSender embedSender;

    @Getter
    private volatile JDA jda;

    private final CompletableFuture<Void> startFuture = new CompletableFuture<>();
    private volatile boolean shuttingDown = false;

    public DiscordBot(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
        this.embedSender = new DiscordBotSender(plugin);

        start();
    }

    private void start() {
        plugin.runAsync(task -> {
            try {
                if (isShuttingDown()) {
                    return;
                }

                this.jda = JDABuilder.createLight(plugin.getSettings().getDiscord().getBot().getToken(), Collections.emptyList())
                        .setMemberCachePolicy(MemberCachePolicy.NONE)
                        .setChunkingFilter(ChunkingFilter.NONE)
                        .disableCache(
                                CacheFlag.ACTIVITY,
                                CacheFlag.CLIENT_STATUS,
                                CacheFlag.EMOJI,
                                CacheFlag.MEMBER_OVERRIDES,
                                CacheFlag.ROLE_TAGS,
                                CacheFlag.STICKER,
                                CacheFlag.ONLINE_STATUS,
                                CacheFlag.FORUM_TAGS,
                                CacheFlag.SCHEDULED_EVENTS,
                                CacheFlag.VOICE_STATE
                        )
                        .addEventListeners(new SlashCommandListener(plugin))
                        .addEventListeners(new ButtonListener(plugin))
                        .addEventListeners(new ModalListener(plugin))
                        .build();

                if (isShuttingDown()) {
                    shutdownJda();
                    return;
                }

                jda.awaitReady();

                if (isShuttingDown()) {
                    shutdownJda();
                    return;
                }

                Guild guild = jda.getGuildById(plugin.getSettings().getDiscord().getBot().getGuildId());
                if (guild == null) {

                    plugin.log(Level.SEVERE, "The provided guild id is invalid!");
                    return;
                }

                if (plugin.getDiscordConfig() == null) {

                    plugin.log(Level.SEVERE, "Discord config is invalid!");
                    return;
                }

                guild.updateCommands().addCommands(
                        Commands.slash("reports", plugin.getDiscordConfig().getMessages().getCommands().getReportCommand())
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                                .addSubcommands(
                                    new SubcommandData("check", plugin.getDiscordConfig().getMessages().getCommands().getCheckCommand())
                                            .addOption(OptionType.INTEGER, "id", plugin.getDiscordConfig().getMessages().getCommands().getCheckCommandUsage(), true)
                                )

                ).queue(
                        success -> plugin.log(Level.INFO, "Discord slash commands registered"),
                        error -> plugin.log(Level.SEVERE, "error while registering discord commands. Check the guild id", error)
                );

                plugin.log(Level.INFO, "Discord Bot started");
            } catch (Throwable e) {
                if (!isShuttingDown()) {

                    plugin.log(Level.SEVERE, "Error while starting the Discord bot", e);
                }
            } finally {
                startFuture.complete(null);
            }

        });

    }

    public void shutdown() {
        shuttingDown = true;

        try {
            startFuture.get(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException ignored) {
        }

        shutdownJda();
    }

    private boolean isShuttingDown() {
        return shuttingDown || !plugin.isEnabled();
    }

    private void shutdownJda() {
        if (jda != null) {
            try {
                jda.shutdownNow();
            } catch (Throwable ignored) {
            }
        }
    }
}
