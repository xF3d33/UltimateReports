package dev.xf3d3.ultimatereports.dialogs;

import de.themoep.minedown.adventure.MineDown;
import dev.xf3d3.ultimatereports.UltimateReports;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class ReportDialog {
    private final UltimateReports plugin;
    private final Player player;
    private final OfflinePlayer reported;
    @Nullable private final Player reportedPlayer;

    public ReportDialog(@NotNull UltimateReports plugin, @NotNull Player player, @NotNull OfflinePlayer reported, @Nullable Player reportedPlayer) {
        this.plugin = plugin;
        this.player = player;
        this.reported = reported;
        this.reportedPlayer = reportedPlayer;

        open();
    }

    private void open() {
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(MineDown.parse(plugin.getDialogConfig().getReport().getTitle()))
                        .inputs(List.of(
                                DialogInput.text(
                                        "reason",
                                        plugin.getDialogConfig().getReport().getReason().getWidth(),
                                        MineDown.parse(plugin.getDialogConfig().getReport().getReason().getTitle()),
                                        true,
                                        plugin.getDialogConfig().getReport().getReason().getDefaultText(),
                                        plugin.getDialogConfig().getReport().getReason().getMaxLength(),
                                        null
                                )
                        ))
                        .build()
                )
                .type(DialogType.confirmation(
                        ActionButton.create(
                                MineDown.parse(plugin.getDialogConfig().getReport().getButtons().getConfirmation().getName()),
                                MineDown.parse(plugin.getDialogConfig().getReport().getButtons().getConfirmation().getHover()),
                                100,
                                DialogAction.customClick(
                                        (view, audience) -> {
                                            String reason = view.getText("reason");

                                            if (reason == null) {

                                                player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getReport().getNoReasonGiven()));
                                                return;
                                            }

                                            if (audience instanceof Player dialogPlayer) {

                                                plugin.getReportsManager().createReport(dialogPlayer, reported, reason, reportedPlayer);

                                            }
                                        },
                                        ClickCallback.Options.builder()
                                                .uses(1) // Set the number of uses for this callback. Defaults to 1
                                                .lifetime(ClickCallback.DEFAULT_LIFETIME) // Set the lifetime of the callback. Defaults to 12 hours
                                                .build()
                                )
                        ),
                        ActionButton.create(
                                MineDown.parse(plugin.getDialogConfig().getReport().getButtons().getDiscard().getName()),
                                MineDown.parse(plugin.getDialogConfig().getReport().getButtons().getDiscard().getHover()),
                                100,
                                null // If we set the action to null, it doesn't do anything and closes the dialog
                        )
                ))
        );

        player.showDialog(dialog);
    }

}
