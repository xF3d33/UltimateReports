package dev.xf3d3.ultimatereports.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.themoep.minedown.adventure.MineDown;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.dialogs.ReportDialog;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused")
@CommandAlias("report")
@CommandPermission("ultimatereports.player")
public class ReportCommand extends BaseCommand {
    private final UltimateReports plugin;

    public ReportCommand(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@onlineUsers [reason] @nothing")
    @CommandPermission("ultimatereports.report")
    public void onReport(Player player, OfflinePlayer reported, @Optional String[] reasonArgs) {
        String reason = String.join(" ", reasonArgs);

        plugin.getUsersManager().getPlayer(player.getUniqueId()).thenAccept(onlinePlayer -> {
            if (onlinePlayer.getRemainingTime() > 0) {

                player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getCannotUseCommandsCooldown()
                        .replace("%REMAINING%", plugin.getUtils().formatTime(onlinePlayer.getRemainingTime()))
                ));
                return;
            }

            if (reported == null) {

                player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getPlayerNotFoundBlank()));
                return;
            }

            if (!plugin.getSettings().getGeneral().isReportOffline() && !reported.isOnline()) {

                player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getReport().getCanReportOnlyOnline()));
                return;
            }

            if (player.getUniqueId().equals(reported.getUniqueId())) {

                player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getReport().getCannotReportYourself()));
                return;
            }
            final Player reportedPlayer = reported.isOnline() ? Bukkit.getPlayer(reported.getUniqueId()) : null;

            if (reason.isEmpty()) {
                if (plugin.getUsersManager().canUseDialogs(player)) {

                    new ReportDialog(plugin, player, reported, reportedPlayer);
                    return;
                }

                player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getReport().getNoReasonGiven()));
                return;
            }


            plugin.getReportsManager().createReport(player, reported, reason, reportedPlayer);
        });
    }
}
