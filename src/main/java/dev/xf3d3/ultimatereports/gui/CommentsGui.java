package dev.xf3d3.ultimatereports.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.minedown.adventure.MineDown;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.Comment;
import dev.xf3d3.ultimatereports.models.Report;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class CommentsGui extends BaseGui {
    private final Report report;
    private GuiElementGroup group;

    public CommentsGui(@NotNull UltimateReports plugin, @NotNull Player player, @NotNull Report report) {
        super(plugin, player);
        this.report = report;

        open();
    }

    @Override
    protected void open() {
        plugin.getUsersManager().getPlayer(player.getUniqueId()).thenAccept(onlinePlayer -> {

            final InventoryGui gui = new InventoryGui(plugin, player, serialize(plugin.getGuiConfig().getComments().getTitle().replace("%ID%", String.valueOf(report.getId())), player), plugin.getGuiConfig().getComments().getSetup());
            addCommonElements(gui, onlinePlayer);

            // Comments
            this.group = new GuiElementGroup('x');
            addComments();
            gui.addElement(group);

            gui.setCloseAction(close -> true);

            gui.show(player);
        });

    }

    private void addComments() {
        group.clearElements();

        report.getComments().forEach(comment -> {
            OfflinePlayer author = Bukkit.getOfflinePlayer(comment.getAuthor());

            group.addElement(new DynamicGuiElement('o', (viewer) -> new StaticGuiElement('o',
                    createPlayerSkullWithLore(author, serialize(
                            plugin.getGuiConfig().getComments().getComment().stream()
                                    .map(s -> s.replace("%AUTHOR%", comment.getAuthorName()))
                                    .map(s -> s.replace("%MESSAGE%", comment.getMessage()))
                                    .map(s -> s.replace("%DATE%", plugin.getUtils().formatDate(comment.getTimestamp())))
                                    .map(s -> s.replace("%STATUS%", "%" + comment.getStatus().name() + "%"))
                                    .map(this::replaceStatus)
                                    .collect(Collectors.toList()), player
                    )),
                    click -> {
                        if (!click.getWhoClicked().hasPermission("ultimatereports.reports.manage")) {

                            player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
                            return true;
                        }

                        // LEFT CLICK -- SET AS READ
                        if (click.getType().equals(ClickType.LEFT)) {
                            if (comment.getStatus().equals(Comment.MessageStatus.READ)) return true;

                            comment.setStatus(Comment.MessageStatus.READ);
                            report.addOrUpdateComment(comment);

                            plugin.runAsync(task -> plugin.getReportsManager().updateReportData(player, report));
                            plugin.getDiscordBot().ifPresent(bot -> bot.getEmbedSender().updateCommentMessage(report, comment));

                            plugin.getReportsManager().notifyReporter(report, plugin.getMessages().getReport().getCommentUpdate());

                            click.getGui().draw();
                            return true;

                            // RIGHT CLICK -- DELETE
                        } else if (click.getType().equals(ClickType.RIGHT)) {

                            report.removeComment(comment.getId());

                            plugin.runAsync(task -> plugin.getReportsManager().updateReportData(player, report));
                            plugin.getDiscordBot().ifPresent(bot -> bot.getEmbedSender().updateReportMessage(report));

                            addComments();
                            click.getGui().draw();
                            return true;
                        }

                        return true;
                    }
            )));
        });
    }
}