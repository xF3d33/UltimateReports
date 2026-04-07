package dev.xf3d3.ultimatereports.managers;

import com.google.common.collect.Sets;
import de.themoep.minedown.adventure.MineDown;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.Comment;
import dev.xf3d3.ultimatereports.models.Position;
import dev.xf3d3.ultimatereports.models.Report;
import dev.xf3d3.ultimatereports.network.Message;
import dev.xf3d3.ultimatereports.network.Payload;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ReportsManager {
    private final UltimateReports plugin;

    @Getter
    private final Set<Report> reports = Sets.newConcurrentHashSet();

    public ReportsManager(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
    }

    public void loadReports() {
        reports.addAll(plugin.getDatabase().loadReports());

        plugin.sendConsole("&eLoaded " + reports.size() + " reports!");
        plugin.setLoaded(true);
    }

    public void removeReport(@NotNull Report report) {
        reports.removeIf(r -> r.getId() == report.getId());
    }

    public void updateReportLocal(@NotNull Report report) {
        removeReport(report);
        reports.add(report);
    }

    public void updateReportLocal(Report report, Integer id) {
        reports.removeIf(r -> r.getId() == id);
        reports.add(report);
    }

    public void updateReportData(@Nullable Player actor, @NotNull Report report) {
        // update team in the cache
        updateReportLocal(report);

        // Update in the database
        plugin.getDatabase().updateReport(report);

        // Propagate to other servers
        plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                .type(Message.Type.REPORT_UPDATE)
                .payload(Payload.integer(report.getId()))
                .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                .build()
                .send(broker, actor));
    }

    public void deleteReport(@Nullable Player actor, @NotNull Report report) {
        removeReport(report);

        plugin.runAsync(task -> {

            plugin.getDatabase().deleteReport(report.getId());

            // SEND TO OTHER SERVERS
            plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                    .type(Message.Type.REPORT_DELETE)
                    .payload(Payload.integer(report.getId()))
                    .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                    .build()
                    .send(broker, actor));
        });
    }

    public void createReport(@NotNull Player reporter, @NotNull OfflinePlayer reported, @NotNull String reason, @Nullable Player reportedPlayer) {
        plugin.runAsync(task -> {
            Position reporterPosition = Position.at(
                    reporter.getX(),
                    reporter.getY(),
                    reporter.getZ(),
                    reporter.getWorld().getName(),
                    reporter.getYaw(),
                    reporter.getPitch(),
                    plugin.getSettings().getCrossServer().isEnabled() ? plugin.getSettings().getCrossServer().getServerName() : null

            );

            Position reportedPosition;
            if (reportedPlayer != null) {
                reportedPosition = Position.at(
                        reportedPlayer.getX(),
                        reportedPlayer.getY(),
                        reportedPlayer.getZ(),
                        reportedPlayer.getWorld().getName(),
                        reportedPlayer.getYaw(),
                        reportedPlayer.getPitch(),
                        plugin.getSettings().getCrossServer().isEnabled() ? plugin.getSettings().getCrossServer().getServerName() : null

                );
            } else {

                reportedPosition = null;
            }

            Report report = plugin.getDatabase().createReport(reporter, reported, reason, reporterPosition, reportedPosition);
            reports.add(report);

            plugin.getMessages().getReport().getReportSuccessful()
                    .forEach(message -> reporter.sendMessage(MineDown.parse(message
                            .replace("%PLAYER%", Objects.requireNonNullElse(reported.getName(), "Player not found"))
                            .replace("%REASON%", reason)
                            .replace("%ID%", String.valueOf(report.getId()))
                            .replace("%PLUGIN_PREFIX%", plugin.getSettings().getGeneral().getPrefix())
            )));

            // SEND TO OTHER SERVERS
            plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                    .type(Message.Type.REPORT_CREATE)
                    .payload(Payload.integer(report.getId()))
                    .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                    .build()
                    .send(broker, reporter));

            // SEND TO DISCORD
            plugin.getWebHookSender().ifPresent(sender -> sender.sendReportEmbed(report));
            plugin.getDiscordBot().ifPresent(bot -> bot.getEmbedSender().sendReportEmbed(report));

            // SEND TO STAFFERS
            notifyStaffers(report);
        });

        plugin.getUsersManager().getPlayer(reporter.getUniqueId()).thenAccept(user -> {
           user.getPreferences().setReports(user.getPreferences().getReports() + 1);

           plugin.getUsersManager().updatePlayer(user);
        });

        plugin.getUsersManager().getPlayer(reported.getUniqueId()).thenAccept(user -> {
            user.getPreferences().setReported(user.getPreferences().getReported() + 1);

            plugin.getUsersManager().updatePlayer(user);
        });
    }

    public void notifyStaffers(@NotNull Report report) {
        plugin.runSync(task1 -> Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("ultimatereports.reports.notify"))
                .forEach(p -> plugin.getUsersManager().getPlayer(p.getUniqueId())
                        .thenAccept(u -> {
                            if (u.getPreferences().isNotifications()) {
                                p.sendMessage(MineDown.parse(String.join("\n", plugin.getMessages().getReport().getAdminNotification())
                                        .replace("%REPORTER%", report.getReporterName())
                                        .replace("%REPORTED%", report.getReportedName())
                                        .replace("%ID%", String.valueOf(report.getId()))
                                        .replace("%REASON%", report.getReason())
                                ));
                            }
                        })

                ));
    }

    public void addComment(@NotNull Player author, @NotNull Report report, @NotNull String commentText) {
        Comment comment = Comment.create(author, commentText);
        report.getComments().add(comment);

        plugin.runAsync(task -> plugin.getReportsManager().updateReportData(author, report));

        // DISCORD
        plugin.getDiscordBot().ifPresent(bot -> bot.getEmbedSender().updateReportMessage(report));
        plugin.getDiscordBot().ifPresent(bot -> bot.getEmbedSender().sendCommentEmbed(report, comment));
        plugin.getWebHookSender().ifPresent(sender -> plugin.runAsync(task -> sender.sendCommentEmbed(report, comment)));

        if (report.getReporter().equals(author.getUniqueId())) return;
        notifyReporter(report, plugin.getMessages().getReport().getNewComment());
    }

    public void notifyReporter(@NotNull Report report, @NotNull String msg) {
        Player reporter = Bukkit.getPlayer(report.getReporter());

        if (reporter == null || !plugin.getSettings().getGeneral().isSendNotification()) return;
        plugin.getUsersManager().getPlayer(reporter.getUniqueId()).thenAccept(user -> {
            if (!user.getPreferences().isNotifications()) return;

            final String message = msg
                            .replace("%ID%", String.valueOf(report.getId()))
                            .replace("%STATUS%", plugin.getMessages().getGeneral().getReportStatus().get(report.getStatus()));


            reporter.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + message));
        });
    }

    public Optional<Report> getReportById(int id) {
        return reports.stream().filter(r -> r.getId() == id).findFirst();
    }

    public Set<Report> getAllReports() {
        return getReports().stream()
                .sorted(Comparator.comparingInt(Report::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<Report> getPlayerReports(@NotNull UUID uuid) {
        return getReports().stream()
                .filter(report -> report.getReporter().equals(uuid))
                .sorted(Comparator.comparingInt(Report::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<Report> getOpenReports() {
        return getReports().stream()
                .filter(report -> !report.getStatus().equals(Report.Status.ARCHIVED))
                .sorted(Comparator.comparingInt(Report::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<Report> getWaitingReports() {
        return getReports().stream()
                .filter(report -> report.getStatus().equals(Report.Status.WAITING))
                .sorted(Comparator.comparingInt(Report::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<Report> getInProgressReports() {
        return getReports().stream()
                .filter(report -> report.getStatus().equals(Report.Status.IN_PROGRESS))
                .sorted(Comparator.comparingInt(Report::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<Report> getClosedReports() {
        return getReports().stream()
                .filter(report -> report.getStatus().equals(Report.Status.DONE))
                .sorted(Comparator.comparingInt(Report::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<Report> getArchivedReports() {
        return getReports().stream()
                .filter(report -> report.getStatus().equals(Report.Status.ARCHIVED))
                .sorted(Comparator.comparingInt(Report::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}