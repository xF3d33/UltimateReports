package dev.xf3d3.ultimatereports.gui;

import de.themoep.inventorygui.*;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.OnlinePlayer;
import dev.xf3d3.ultimatereports.models.Report;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ReportsList extends BaseGui {
    private Report.Status type;
    private Set<Report> reports;
    private String title;

    public ReportsList(@NotNull UltimateReports plugin, @NotNull Player player, @NotNull Set<Report> reports, @NotNull Report.Status type) {
        super(plugin, player);
        this.reports = reports;
        this.type = type;

        open();
    }

    // refresh the group contents
    private void updateGUI(GuiElementGroup group, @Nullable InventoryGui gui) {
        group.clearElements(); // Clear the old reports

        reports.forEach(report -> {
            OfflinePlayer reporter = Bukkit.getOfflinePlayer(report.getReporter());

            group.addElement(new StaticGuiElement('q',
                    createPlayerSkullWithLore(reporter, getReportInfo(report, player)),
                    click -> {
                        new ReportGui(plugin, player, report.getId());
                        return true;
                    }
            ));
        });

        if (player.hasPermission("ultimatereports.reports.manage")) {
            title = plugin.getGuiConfig().getReportsList().getTitleAllReports().replace("%REPORT_STATUS%", type.name());
        } else title = plugin.getGuiConfig().getReportsList().getTitleYourReports();

        if (gui != null) gui.setTitle(serialize(title, player));
    }

    @Override
    protected void open() {
        final OnlinePlayer onlinePlayer = plugin.getUsersManager().getUsermap().get(player.getUniqueId());

        if (onlinePlayer == null) {
            plugin.log(Level.WARNING, "Player with UUID " + player.getUniqueId() + " not found!");
            return;
        }

        if (player.hasPermission("ultimatereports.reports.manage")) {
            title = plugin.getGuiConfig().getReportsList().getTitleAllReports().replace("%REPORT_STATUS%", type.name());
        } else title = plugin.getGuiConfig().getReportsList().getTitleYourReports();

        final InventoryGui gui = new InventoryGui(plugin, player, serialize(title, player), plugin.getGuiConfig().getReportsList().getSetup());
        addCommonElements(gui, onlinePlayer);

        // Reports
        final GuiElementGroup group = new GuiElementGroup('c');
        updateGUI(group, null); // Populate initially
        gui.addElement(group);


        // Report Type
        GuiStateElement reportsType = new GuiStateElement('b',
                new GuiStateElement.State(
                        change -> {
                            this.reports = plugin.getReportsManager().getWaitingReports();
                            this.type = Report.Status.WAITING;

                            updateGUI(group, gui); // Repopulate with new data
                            change.getGui().show(player);
                        },
                        "waiting",
                        new ItemStack(plugin.getGuiConfig().getReportsList().getFiltersItem()),
                        serialize(plugin.getGuiConfig().getReportsList().getFilters().stream()
                                .map(s -> s.replace("%FILTER%", "%IN_PROGRESS%"))
                                .map(this::replaceStatus)
                                .collect(Collectors.toList()),
                                player
                        )
                ),
                new GuiStateElement.State(
                        change -> {
                            this.reports = plugin.getReportsManager().getInProgressReports();
                            this.type = Report.Status.IN_PROGRESS;

                            updateGUI(group, gui); // Repopulate with new data
                            change.getGui().show(player);
                        },
                        "in_progress",
                        new ItemStack(plugin.getGuiConfig().getReportsList().getFiltersItem()),
                        serialize(plugin.getGuiConfig().getReportsList().getFilters().stream()
                                .map(s -> s.replace("%FILTER%", "%DONE%"))
                                .map(this::replaceStatus)
                                .collect(Collectors.toList()),
                                player
                        )
                ),
                new GuiStateElement.State(
                        change -> {
                            this.reports = plugin.getReportsManager().getClosedReports();
                            this.type = Report.Status.DONE;

                            updateGUI(group, gui); // Repopulate with new data
                            change.getGui().show(player);
                        },
                        "done",
                        new ItemStack(plugin.getGuiConfig().getReportsList().getFiltersItem()),
                        serialize(plugin.getGuiConfig().getReportsList().getFilters().stream()
                                .map(s -> s.replace("%FILTER%", "%ARCHIVED%"))
                                .map(this::replaceStatus)
                                .collect(Collectors.toList()),
                                player
                        )
                ),
                new GuiStateElement.State(
                        change -> {
                            this.reports = plugin.getReportsManager().getArchivedReports();
                            this.type = Report.Status.ARCHIVED;

                            updateGUI(group, gui); // Repopulate with new data
                            change.getGui().show(player);
                        },
                        "archived",
                        new ItemStack(plugin.getGuiConfig().getReportsList().getFiltersItem()),
                        serialize(plugin.getGuiConfig().getReportsList().getFilters().stream()
                                .map(s -> s.replace("%FILTER%", "%ALL%"))
                                .map(this::replaceStatus)
                                .collect(Collectors.toList()),
                                player
                        )
                ),
                new GuiStateElement.State(
                        change -> {
                            this.reports = plugin.getReportsManager().getAllReports();
                            this.type = Report.Status.OPEN;

                            updateGUI(group, gui); // Repopulate with new data
                            change.getGui().show(player);
                        },
                        "all",
                        new ItemStack(plugin.getGuiConfig().getReportsList().getFiltersItem()),
                        serialize(plugin.getGuiConfig().getReportsList().getFilters().stream()
                                .map(s -> s.replace("%FILTER%", "%WAITING%"))
                                .map(this::replaceStatus)
                                .collect(Collectors.toList()),
                                player
                        )
                )
        );

        if (type.equals(Report.Status.OPEN)) {

            reportsType.setState("all");
        } else if (type.equals(Report.Status.WAITING)) {

            reportsType.setState("waiting");
        } else if (type.equals(Report.Status.IN_PROGRESS)) {

            reportsType.setState("in_progress");
        } else if (type.equals(Report.Status.DONE)) {

            reportsType.setState("done");
        } else if (type.equals(Report.Status.ARCHIVED)) {

            reportsType.setState("archived");
        }

        if (player.hasPermission("ultimatereports.reports.manage")) {
            gui.addElement(reportsType);
        }

        gui.show(player);
    }
}