package dev.xf3d3.ultimatereports.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import dev.xf3d3.ultimatereports.models.Report;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Messages {
    public static final String MESSAGES_HEADER = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃   UltimateReports Messages   ┃
        ┃      Developed by xF3d3      ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ Information: https://modrinth.com/plugin/ultimate-reports
        ┣╸ Syntax: The messages are formatted with MineDown: https://github.com/Phoenix616/MineDown
        ┗╸ Documentation: https://ultimatereports.gitbook.io/ultimatereports-docs/documentation""";


    @Comment("General Messages")
    private GeneralMessages general = new GeneralMessages();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GeneralMessages {
        private String noPermission = "[You don't have the permission for this action.](dark_aqua)";

        private String cannotUseCommandsCooldown = "[You are on cooldown! You cannot use commands for another:](dark_aqua) [%REMAINING%](gold)";

        private String playerNotFound = "[Player](dark_aqua) [%PLAYER%] [not found!](dark_aqua)";

        private String playerNotFoundBlank = "[Cannot find the specified player](dark_aqua)";

        private String pluginReloaded = "[Plugin successfully reloaded](dark_aqua)";

        private String positionDoesntExist = "[The reported player was offline during the report, so there is no location to teleport to](dark_aqua)";

        private String cantTpPlayerOffline = "[The player is offline!](dark_aqua)";

        private String featureDisabled = "[This feature is disabled!](dark_aqua)";

        @Comment("You can use this to customize the name for these status")
        private Map<Report.Status, String> reportStatus = new LinkedHashMap<>();
        {
            reportStatus.put(Report.Status.OPEN, "OPEN");
            reportStatus.put(Report.Status.WAITING, "WAITING");
            reportStatus.put(Report.Status.IN_PROGRESS, "IN PROGRESS");
            reportStatus.put(Report.Status.DONE, "DONE");
            reportStatus.put(Report.Status.ARCHIVED, "ARCHIVED");
        }

        @Comment("You can use this to customize the name for these status")
        private Map<dev.xf3d3.ultimatereports.models.Comment.MessageStatus, String> commentsStatus = new LinkedHashMap<>();
        {
            commentsStatus.put(dev.xf3d3.ultimatereports.models.Comment.MessageStatus.NOT_READ, "NOT READ");
            commentsStatus.put(dev.xf3d3.ultimatereports.models.Comment.MessageStatus.READ, "READ");
        }

        @Comment("You can use this to customize the name for the report processing")
        private Map<Report.MarkedAs, String> reportsMarks = new LinkedHashMap<>();
        {
            reportsMarks.put(Report.MarkedAs.TRUE, "TRUE");
            reportsMarks.put(Report.MarkedAs.FALSE, "FALSE");
        }
    }


    @Comment("Report Messages")
    private ReportMessages report = new ReportMessages();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ReportMessages {

        private String canReportOnlyOnline = "[You can report only online players!](dark_aqua)";

        private String cannotReportYourself = "[You can't report yourself!](dark_aqua)";

        private String noReasonGiven = "[Please provide a reason for your report.](dark_aqua)";

        private List<String> reportSuccessful = List.of(
                "%PLUGIN_PREFIX% [You successfully reported](dark_aqua) [%PLAYER%](gold) [for:](dark_aqua) [%REASON%](gold)",
                "[If you want to add extra information in your report](dark_aqua) [[click here]](blue bold suggest_command=/reports comment %ID% <message> hover=[Click to add a comment](dark_aqua))"
        );

        private String reportDeleted = "[The report has been successfully deleted](dark_aqua)";

        private String alreadyProcessed = "[The report has been already been processed!](dark_aqua)";

        private String rewards = "[One of your reports has been verified and you can claim the reward!](dark_aqua)";

        private String noRewardsToClaim = "[There are no rewards to claim!](dark_aqua)";

        private List<String> adminNotification = List.of(
                "&7&m                                                    &r",
                "[New report from](dark_aqua) [%REPORTER%](gold)",
                "[Report info:](dark_aqua)",
                "",
                "[ID: #](dark_aqua)[%ID%](gold)",
                "[Reporter:](dark_aqua) [%REPORTER%](green)[, Reported:](dark_aqua) [%REPORTED%](red)",
                "[Reason:](dark_aqua) [%REASON%](gold)",
                "",
                "[[Click to open the report]](blue bold run_command=/reports check %ID% hover=[Click to open the report](dark_aqua))",
                "&7&m                                                    &r"
        );

        private String statusUpdate = "[There is an update for your report](dark_aqua) [#%ID%](gold)[. The status has changed to:](dark_aqua) [%STATUS%](gold)";

        private String commentUpdate = "[There is an update for your report](dark_aqua) [#%ID%](gold)[. A comment has been read](dark_aqua)";

        private String newComment = "[There is an update for your report](dark_aqua) [#%ID%](gold)[. A new comment has been added](dark_aqua)";

        private String generalUpdate = "[There is an update for your report](dark_aqua) [#%ID%](gold)";
    }


    @Comment("Notification messages")
    private NotificationMessages notifications = new NotificationMessages();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NotificationMessages {
        private String notificationsEnabled = "[You have successfully enabled the notifications](dark_aqua)";

        private String notificationsDisabled = "[You have successfully disabled the notifications](dark_aqua)";
    }


    @Comment("Comment Messages")
    private CommentMessages comment = new CommentMessages();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CommentMessages {
        private String commentCannotBeEmpty = "[The comment cannot be empty](dark_aqua)";

        private String commentAdded = "[Your comment has been added to the report](dark_aqua)";
    }

    @Comment("Cooldown Messages")
    private CooldownMessages cooldown = new CooldownMessages();


    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CooldownMessages {
        private String cooldownAdd = "[Cooldown successfully added to](dark_aqua) [%PLAYER%](gold)[. The new player's cooldown is:](dark_aqua) [%COOLDOWN%](gold)";

        private String cooldownReset = "[The player's cooldown has been successfully reset.](dark_aqua)";

        private String cooldownCheck = "[The player's cooldown is:](dark_aqua) [%COOLDOWN%](gold)";
    }

}
