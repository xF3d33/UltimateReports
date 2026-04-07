package dev.xf3d3.ultimatereports.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Material;

import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GUI {
    public static final String GUI_HEADER = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃   UltimateReports GUI Config   ┃
        ┃      Developed by xF3d3        ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ Information: https://modrinth.com/plugin/ultimate-reports
        ┗╸ Documentation: https://ultimatereports.gitbook.io/ultimatereports-docs/documentation/basics/images-and-media""";


    @Comment({"General Messages. These messages are the same for all (or most) of the GUIs.",
            "If you don't see some of these items, it's not an error, they are added automatically when needed."})
    private SharedMessages shared = new SharedMessages();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SharedMessages {
        private String close = "[Click to](gray) [CLOSE](red bold) [the GUI.](gray)";

        private String firstPage = "[Go to first page (current:](gray) [%page%](gold)[)](gray)";

        private String prevPage = "[Go to previous page (](gray) [%prevpage%](gold)[)](gray)";

        private String nextPage = "[Go to next page (](gray)[%nextpage%](gold)[)](gray)";

        private String lastPage = "[Go to last page (](gray)[%pages%](gold)[)](gray)";

        private Material notificationsItem = Material.OAK_HANGING_SIGN;
        private Material fillerItem = Material.GRAY_STAINED_GLASS_PANE;
        private Material closeItem = Material.BARRIER;
        private Material prevPageItem = Material.ARROW;
        private Material firstPageItem = Material.ARROW;
        private Material nextPageItem = Material.ARROW;
        private Material lastPageItem = Material.ARROW;

        @Comment("You can add more lines here")
        private List<String> disableNotification = List.of(
                "[Click to](gray) [DISABLE](red bold) [the notifications](gray)",
                "[of current and future reports](gray)"
        );

        @Comment("You can add more lines here")
        private List<String> enableNotification = List.of(
                "[Click to](gray) [ENABLE](green bold) [the notifications](gray)",
                "[of current and future reports](gray)"
        );


        @Comment("You can add more lines here")
        private List<String> reportInfo = List.of(
                "[Report](gray) [#%ID%](gold)",
                " ",
                "[Status:](gray) [%STATUS%](green bold)",
                "[Date:](gray) [%DATE%](yellow)",
                " ",
                "[Reporter:](gray) [%REPORTER%](green)",
                "[Reported:](gray) [%REPORTED%](red)",
                "[Reason:](gray) [%REASON%](gold)"
        );

    }

        @Comment("Messages for the reports list")
        private ReportsListMessages reportsList = new ReportsListMessages();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class ReportsListMessages {

            @Comment("The setup of the GUI. Each char represents an item. You can move or remove them if you know what you're doing.")
            private String[] setup = {
                    "a       b",
                    "ggggggggg",
                    "ccccccccc",
                    "ccccccccc",
                    "ggggggggg",
                    "d e f h i"
            };

            @Comment("This title is shown to staffers")
            private String titleAllReports = "[Reports List -](dark_gray) [%REPORT_STATUS% Reports](gold bold)";

            @Comment("This title is shown to players")
            private String titleYourReports = "[Your Reports](dark_gray)";

            private Material filtersItem = Material.BOOKSHELF;

            @Comment("You can add more lines here")
            private List<String> filters = List.of(
                    "[Click to filter the reports to](gray) [%FILTER%](gold bold)",
                    " ",
                    "[The available filters are:](gray)",
                    " ",
                    "[%ALL%](gold)[,](gray) [%WAITING%](gold)[,](gray) [%IN_PROGRESS%](gold)[,](gray) [%DONE%](gold)[,](gray) [%ARCHIVED%](gold)[,](gray)"
            );
    }


    @Comment("Messages for the Report GUI")
    private ReportMessages report = new ReportMessages();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ReportMessages {
        private String[] setup = {
                "a        ",
                "ggggzgggg",
                "u  lmn  t",
                "  opqrs  ",
                "         ",
                "d e f h i"
        };

        private String title = "[Report](dark_gray) [#%ID%](gold)";

        private String setStatus = "[Set the report status to:](gray) [%STATUS%](gold bold)";

        private String archive = "[Archive the report](gray)";

        private List<String> delete = List.of(
                "[Delete the report](red bold)",
                " ",
                "[Shift-Left](gold) [to delete the report](gray)"
        );

        private Material waitingItem = Material.RED_TERRACOTTA;
        private Material inProgressItem = Material.YELLOW_TERRACOTTA;
        private Material doneItem = Material.GREEN_TERRACOTTA;
        private Material archiveItem = Material.CYAN_TERRACOTTA;
        private Material deleteItem = Material.BLACK_TERRACOTTA;
        private Material punishItem = Material.MACE;
        private Material commentsItem = Material.WRITABLE_BOOK;
        private Material processItem = Material.ENDER_EYE;

        @Comment("You can add more lines here")
        private List<String> comments = List.of(
                "[Comments:](gray) [%COMMENTS_NUMBER%](gold)",
                " ",
                "[Left-Click](gold) [to check the comments](gray)"
        );

        @Comment("you can modify the default cooldown value in the config.yml")
        private List<String> punishReporter = List.of(
                "[Improper Report](yellow)",
                " ",
                "[Left-Click](gold) [to punish the reporter](gray)"
        );

        private List<String> playerStats = List.of(
                "[Player stats](yellow)",
                " ",
                "[Reports:](gray) [%SENT%](gold)",
                "[Times reported:](gray) [%RECEIVED%](gold)",
                " ",
                "[Left-Click](gold) [to TP to the current location](gray)",
                "[Right-Click](gold) [to TP to the report location](gray)"
        );

        private String processReport = "[Left-Click](gold) [to process this report](gray)";
    }


    @Comment("Messages for the Comments GUI")
    private CommentsMessages comments = new CommentsMessages();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CommentsMessages {
        String[] setup = {
                "a        ",
                "ggggggggg",
                "xxxxxxxxx",
                "xxxxxxxxx",
                "         ",
                "d e f h i"
        };

        private String title = "[Report](dark_gray) [#%ID%](gold) [»](gray) [Comments](yellow)";

        @Comment("You can add more lines here")
        private List<String> comment = List.of(
                "[Author](gray) [%AUTHOR%](gold)",
                " ",
                "[Status:](gray) [%STATUS%](green bold)",
                "[Date:](gray) [%DATE%](yellow)",
                "[Message:](gray) [%MESSAGE%](white)",
                " ",
                "[Left-Click](gold) [to check mark as](gray) [READ](green bold)",
                "[Right-Click](gold) [to](gray) [REMOVE](red bold)"
        );
    }


    @Comment("Messages for the Process Report GUI")
    private ProcessMessages process = new ProcessMessages();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ProcessMessages {
        String[] setup = {
                "a        ",
                "ggggggggg",
                "         ",
                "  t   y  ",
                "         ",
                "d e f h i"
        };

        private String title = "[Report](dark_gray) [#%ID%](gold) [»](gray) [Process](yellow)";

        private Material markAsTrueMaterial = Material.GREEN_WOOL;
        private Material markAsFalseMaterial = Material.RED_WOOL;

        @Comment("You can add more lines here")
        private List<String> markAsTrue = List.of(
                "[By clicking here you will mark the report as](gray) [TRUE](green bold)",
                " ",
                "[If the rewards system is enabled](gray)",
                "[The reporter will automatically receive the Reward](gray)",
                " ",
                "[The report will be archived](gray)"
        );

        @Comment("You can add more lines here")
        private List<String> markAsFalse = List.of(
                "[By clicking here you will mark the report as](gray) [FALSE](red bold)",
                " ",
                "[The report will be deleted](gray)"
        );
    }

    @Comment("Messages for the Reward GUI")
    private RewardMessages reward = new RewardMessages();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RewardMessages {
        String[] setup = {
                "         ",
                "    a    ",
                "         "
        };

        private String title = "[Claim the Rewards for your report](dark_gray)";

        private Material markAsTrueMaterial = Material.GREEN_WOOL;
        private Material markAsFalseMaterial = Material.RED_WOOL;

        @Comment("You can add more lines here")
        private List<String> markAsTrue = List.of(
                "[By clicking here you will mark the report as](gray) [TRUE](green bold)",
                " ",
                "[If the rewards system is enabled](gray)",
                "[The reporter will automatically receive the Reward](gray)",
                " ",
                "[The report will be archived](gray)"
        );

        @Comment("You can add more lines here")
        private List<String> markAsFalse = List.of(
                "[By clicking here you will mark the report as](gray) [FALSE](red bold)",
                " ",
                "[The report will be deleted](gray)"
        );
    }

}
