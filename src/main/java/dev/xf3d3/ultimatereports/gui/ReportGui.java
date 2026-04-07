package dev.xf3d3.ultimatereports.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.minedown.adventure.MineDown;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.OnlinePlayer;
import dev.xf3d3.ultimatereports.models.Position;
import dev.xf3d3.ultimatereports.models.Report;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ReportGui extends BaseGui {
    private final int id;

    public ReportGui(@NotNull UltimateReports plugin, @NotNull Player player, int id) {
        super(plugin, player);
        this.id = id;

        open();
    }

    @Override
    protected void open() {
        final Optional<Report> optionalReport = plugin.getReportsManager().getReportById(id);
        
        if (optionalReport.isEmpty()) {
            plugin.log(Level.WARNING, "Report with id " + id + " not found!");

            return;
        }

        final Report report = optionalReport.get();

        CompletableFuture<OnlinePlayer> onlinePlayerFuture = plugin.getUsersManager().getPlayer(player.getUniqueId());
        CompletableFuture<OnlinePlayer> reporterFuture = plugin.getUsersManager().getPlayer(report.getReporter());
        CompletableFuture<OnlinePlayer> reportedFuture = plugin.getUsersManager().getPlayer(report.getReported());

        CompletableFuture.allOf(onlinePlayerFuture, reporterFuture, reportedFuture).thenAccept(v -> {
            OnlinePlayer onlinePlayer = onlinePlayerFuture.join();
            OnlinePlayer reporterPlayer = reporterFuture.join();
            OnlinePlayer reportedPlayer = reportedFuture.join();



            final InventoryGui gui = new InventoryGui(plugin, player, serialize(plugin.getGuiConfig().getReport().getTitle().replace("%ID%", String.valueOf(report.getId())), player), plugin.getGuiConfig().getReport().getSetup());
            addCommonElements(gui, onlinePlayer);


            // WAITING
            gui.addElement(
                    new DynamicGuiElement('o', (viewer) -> new StaticGuiElement('o',
                                    report.getStatus().equals(Report.Status.WAITING) ? enchanted(new ItemStack(plugin.getGuiConfig().getReport().getWaitingItem())) : new ItemStack(plugin.getGuiConfig().getReport().getWaitingItem()),
                                    1,
                                    click -> {
                                        if (!click.getWhoClicked().hasPermission("ultimatereports.reports.manage")) {

                                            player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
                                            return true;
                                        }

                                        report.setStatus(Report.Status.WAITING);
                                        plugin.runAsync(task -> plugin.getReportsManager().updateReportData(player, report));
                                        plugin.getDiscordBot().ifPresent(bot -> bot.getEmbedSender().updateReportMessage(report));

                                        plugin.getReportsManager().notifyReporter(report, plugin.getMessages().getReport().getStatusUpdate());

                                        click.getGui().draw();
                                        return true;
                                    },
                                    serialize(
                                            replaceStatus(
                                                    plugin.getGuiConfig().getReport().getSetStatus()
                                                            .replace("%STATUS%", "%" + Report.Status.WAITING.name() + "%")
                                            ),
                                            player

                                    )
                            ))
            );

            // IN PROGRESS
            gui.addElement(
                    new DynamicGuiElement('p', (viewer) -> new StaticGuiElement('p',
                            report.getStatus().equals(Report.Status.IN_PROGRESS) ? enchanted(new ItemStack(plugin.getGuiConfig().getReport().getInProgressItem())) : new ItemStack(plugin.getGuiConfig().getReport().getInProgressItem()),
                            1,
                            click -> {
                                if (!click.getWhoClicked().hasPermission("ultimatereports.reports.manage")) {

                                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
                                    return true;
                                }

                                report.setStatus(Report.Status.IN_PROGRESS);
                                plugin.runAsync(task -> plugin.getReportsManager().updateReportData(player, report));
                                plugin.getDiscordBot().ifPresent(bot -> bot.getEmbedSender().updateReportMessage(report));

                                plugin.getReportsManager().notifyReporter(report, plugin.getMessages().getReport().getStatusUpdate());

                                click.getGui().draw();
                                return true;
                            },
                            serialize(
                                    replaceStatus(
                                            plugin.getGuiConfig().getReport().getSetStatus()
                                                    .replace("%STATUS%", "%" + Report.Status.IN_PROGRESS.name() + "%")
                                    ),
                                    player

                            )
                    )
            ));

            // DONE
            gui.addElement(
                    new DynamicGuiElement('q', (viewer) -> new StaticGuiElement('q',
                            report.getStatus().equals(Report.Status.DONE) ? enchanted(new ItemStack(plugin.getGuiConfig().getReport().getDoneItem())) : new ItemStack(plugin.getGuiConfig().getReport().getDoneItem()),
                            1,
                            click -> {
                                if (!click.getWhoClicked().hasPermission("ultimatereports.reports.manage")) {

                                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
                                    return true;
                                }

                                report.setStatus(Report.Status.DONE);
                                plugin.runAsync(task -> plugin.getReportsManager().updateReportData(player, report));
                                plugin.getDiscordBot().ifPresent(bot -> bot.getEmbedSender().updateReportMessage(report));

                                plugin.getReportsManager().notifyReporter(report, plugin.getMessages().getReport().getStatusUpdate());

                                click.getGui().draw();
                                return true;
                            },
                            serialize(
                                    replaceStatus(
                                            plugin.getGuiConfig().getReport().getSetStatus()
                                                    .replace("%STATUS%", "%" + Report.Status.DONE.name() + "%")
                                    ),
                                    player

                            )
                    )
            ));

            // ARCHIVE
            gui.addElement(
                    new DynamicGuiElement('r', (viewer) -> new StaticGuiElement('r',
                            report.getStatus().equals(Report.Status.ARCHIVED) ? enchanted(new ItemStack(plugin.getGuiConfig().getReport().getArchiveItem())) : new ItemStack(plugin.getGuiConfig().getReport().getArchiveItem()),
                            1,
                            click -> {
                                if (!click.getWhoClicked().hasPermission("ultimatereports.reports.manage")) {

                                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
                                    return true;
                                }

                                report.setStatus(Report.Status.ARCHIVED);
                                plugin.runAsync(task -> plugin.getReportsManager().updateReportData(player, report));
                                plugin.getDiscordBot().ifPresent(bot -> bot.getEmbedSender().updateReportMessage(report));

                                plugin.getReportsManager().notifyReporter(report, plugin.getMessages().getReport().getStatusUpdate());

                                click.getGui().draw();
                                return true;
                            },
                            serialize(plugin.getGuiConfig().getReport().getArchive(), player)
                    )
            ));

            // DELETE
            gui.addElement(
                    new DynamicGuiElement('s', (viewer) -> new StaticGuiElement('s',
                            new ItemStack(plugin.getGuiConfig().getReport().getDeleteItem()),
                            1,
                            click -> {
                                if (!(click.getType() == ClickType.SHIFT_LEFT)) return true;

                                if (!click.getWhoClicked().hasPermission("ultimatereports.reports.manage")) {

                                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
                                    return true;
                                }

                                plugin.getReportsManager().deleteReport(player, report);
                                click.getWhoClicked().sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getReport().getReportDeleted()));

                                click.getGui().close();

                                if (plugin.getSettings().getGeneral().isReopenGui()) {
                                    new ReportsList(plugin, player, plugin.getReportsManager().getOpenReports(), Report.Status.OPEN);
                                }
                                return true;
                            },
                            serialize(plugin.getGuiConfig().getReport().getDelete(), player)
                    ))
            );

            // PUNISH PLAYER
            gui.addElement(
                    new DynamicGuiElement('m', (viewer) -> new StaticGuiElement('m',
                            new ItemStack(plugin.getGuiConfig().getReport().getPunishItem()),
                            1,
                            click -> {
                                if (!click.getWhoClicked().hasPermission("ultimatereports.reports.manage")) {

                                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
                                    return true;
                                }

                                long cooldown = reporterPlayer.addCoolDown(plugin, plugin.getSettings().getGeneral().getDefaultCooldown());

                                click.getWhoClicked().sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getCooldown().getCooldownAdd()
                                        .replace("%COOLDOWN%", plugin.getUtils().formatTime(cooldown - System.currentTimeMillis()))
                                        .replace("%PLAYER%", reporterPlayer.getLastPlayerName())
                                ));

                                return true;
                            },
                            serialize(plugin.getGuiConfig().getReport().getPunishReporter(), player)
                    )
            ));


            OfflinePlayer reporterOfflinePlayer = Bukkit.getOfflinePlayer(report.getReporter());
            OfflinePlayer reportedOfflinePlayer = Bukkit.getOfflinePlayer(report.getReported());
            gui.addElement(
                    new StaticGuiElement('l',
                            createPlayerSkullWithLore(reporterOfflinePlayer, serialize(
                                    plugin.getGuiConfig().getReport().getPlayerStats().stream()
                                            .map(s -> s.replace("%SENT%", String.valueOf(reporterPlayer.getPreferences().getReports())))
                                            .map(s -> s.replace("%RECEIVED%", String.valueOf(reporterPlayer.getPreferences().getReported())))
                                            .collect(Collectors.toList()), player
                            )),
                            1,
                            click -> {

                                if (!player.hasPermission("ultimatereports.reports.teleport")) {

                                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
                                    return true;
                                }

                                // LEFT-CLICK -- CURRENT LOCATION
                                if (click.getType().isLeftClick()) {
                                    final Player reportedOnlinePlayer = Bukkit.getPlayer(report.getReporter());

                                    if (reportedOnlinePlayer == null) {

                                        player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getCantTpPlayerOffline()));
                                        return true;
                                    }

                                    plugin.getUtils().teleport(player, Position.at(reportedOnlinePlayer.getLocation(), plugin.getSettings().getCrossServer().getServerName()));
                                }

                                // RIGHT CLICK -- POSITION DURING REPORT
                                if (click.getType().isRightClick()) {
                                    plugin.getUtils().teleport(player, report.getReporterPosition());

                                }

                                return true;
                            }
                    )
            );

            gui.addElement(
                    new StaticGuiElement('n',
                            createPlayerSkullWithLore(reportedOfflinePlayer, serialize(
                                    plugin.getGuiConfig().getReport().getPlayerStats().stream()
                                            .map(s -> s.replace("%SENT%", String.valueOf(reportedPlayer.getPreferences().getReports())))
                                            .map(s -> s.replace("%RECEIVED%", String.valueOf(reportedPlayer.getPreferences().getReported())))
                                            .collect(Collectors.toList()), player
                            )),
                            1,
                            click -> {

                                if (!player.hasPermission("ultimatereports.reports.teleport")) {

                                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
                                    return true;
                                }

                                // LEFT-CLICK -- CURRENT LOCATION
                                if (click.getType().isLeftClick()) {
                                    final Player reporterOnlinePlayer = Bukkit.getPlayer(report.getReported());

                                    if (reporterOnlinePlayer == null) {

                                        player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getCantTpPlayerOffline()));
                                        return true;
                                    }

                                    plugin.getUtils().teleport(player, Position.at(reporterOnlinePlayer.getLocation(), plugin.getSettings().getCrossServer().getServerName()));
                                }

                                // RIGHT CLICK -- POSITION DURING REPORT
                                if (click.getType().isRightClick()) {
                                    if (report.getReportedPosition() == null) {

                                        player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getPositionDoesntExist()));
                                        return true;
                                    }

                                    plugin.getUtils().teleport(player, report.getReportedPosition());
                                }

                                return true;
                            }
                    )
            );

            // COMMENTS
            gui.addElement(
                    new StaticGuiElement('t',
                            new  ItemStack(plugin.getGuiConfig().getReport().getCommentsItem()),
                            1,
                            click -> {
                                new CommentsGui(plugin, player, report);

                                return true;
                            },
                            serialize(
                                    plugin.getGuiConfig().getReport().getComments().stream()
                                            .map(s -> s.replace("%COMMENTS_NUMBER%", String.valueOf(report.getComments().size())))
                                            .collect(Collectors.toList()), player
                            )
                    )
            );

            // PROCESS REPORT
            gui.addElement(
                    new StaticGuiElement('z',
                            new  ItemStack(plugin.getGuiConfig().getReport().getProcessItem()),
                            1,
                            click -> {
                                if (!click.getWhoClicked().hasPermission("ultimatereports.reports.manage")) {

                                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
                                    return true;
                                }

                                if (report.getMarkedAs() != null) {

                                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getReport().getAlreadyProcessed()));
                                    return true;
                                }

                                new ProcessGui(plugin, player, report, reporterPlayer);
                                return true;
                            },
                            serialize(
                                    plugin.getGuiConfig().getReport().getProcessReport(),
                                    player
                            )
                    )
            );

            gui.addElement(
                    new StaticGuiElement('u',
                            new  ItemStack(Material.PAPER),
                            1,
                            click -> true,
                            getReportInfo(report, player)
                    )
            );

            gui.setCloseAction(close -> true);


            gui.show(player);
        });
    }
}