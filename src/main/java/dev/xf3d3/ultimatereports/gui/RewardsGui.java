package dev.xf3d3.ultimatereports.gui;

import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.OnlinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RewardsGui extends BaseGui {
    private final OnlinePlayer reporter;
    private boolean hasClaimedItem;

    public RewardsGui(@NotNull UltimateReports plugin, @NotNull Player player, @NotNull OnlinePlayer reporter) {
        super(plugin, player);
        this.reporter = reporter;

        open();
    }

    @Override
    protected void open() {
        plugin.getUsersManager().getPlayer(player.getUniqueId()).thenAccept(onlinePlayer -> {

            final InventoryGui gui = new InventoryGui(plugin, player, serialize(plugin.getGuiConfig().getReward().getTitle(), player), plugin.getGuiConfig().getReward().getSetup());
            addCommonElements(gui, onlinePlayer);

            // TRUE
            gui.addElement(
                    new StaticGuiElement('a',
                            plugin.getRewardsConfig().getRewardItem().toItemStack(),
                            plugin.getRewardsConfig().getRewardItem().getAmount(),
                            click -> {

                                this.reporter.getPreferences().setRewardsToClaim(this.reporter.getPreferences().getRewardsToClaim() - 1);
                                plugin.getUsersManager().updatePlayer(this.reporter);

                                this.hasClaimedItem = true;
                                return false;
                            },
                            ""
                    )
            );

            gui.setCloseAction(close -> {
                if (!this.hasClaimedItem) {
                    this.reporter.getPreferences().setRewardsToClaim(this.reporter.getPreferences().getRewardsToClaim() - 1);
                    plugin.getUsersManager().updatePlayer(this.reporter);

                    player.give(plugin.getRewardsConfig().getRewardItem().toItemStack());
                }

                plugin.getRewardsConfig().getRewardCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%PLAYER%", player.getName())));
                return false;
            });

            gui.show(player);
        });

    }
}