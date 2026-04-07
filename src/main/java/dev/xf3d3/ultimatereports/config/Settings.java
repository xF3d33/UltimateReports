package dev.xf3d3.ultimatereports.config;

import com.google.common.collect.Lists;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import dev.xf3d3.ultimatereports.database.Database;
import dev.xf3d3.ultimatereports.network.Broker;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Settings {

    public static final String CONFIG_HEADER = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃    UltimateReports Config    ┃
        ┃      Developed by xF3d3      ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ Information: https://modrinth.com/plugin/ultimate-reports
        ┗╸ Documentation: https://ultimatereports.gitbook.io/ultimatereports-docs""";


    @Comment("Check for updates on startup?")
    private boolean checkForUpdates = true;

    @Comment({"Use dialogs instead of GUIs/chat when possible?. Requires server version 1.21.8+",
            "Recommended: true (disabled by default just in case the plugin is running on older server versions)",
            "",
            "After enabling this, restart the server and check the new generated file for customization"})
    private boolean useDialogs = false;

    @Comment({"Should the plugin enable the rewards system to give players reward if their reports were true?",
            "",
            "Changes require to restart. A new file will be generated for customization"})
    private boolean enableRewardsSystem = false;

    // Database settings
    @Comment("Database settings")
    private DatabaseSettings database = new DatabaseSettings();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DatabaseSettings {

        @Comment("Type of database to use (SQLITE, MYSQL, MARIADB, H2, POSTGRESQL)")
        private Database.Type type = Database.Type.H2;

        @Comment("Specify credentials here for your MYSQL/MARIADB/POSTGRESQL database")
        private DatabaseCredentials credentials = new DatabaseCredentials();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class DatabaseCredentials {
            private String host = "localhost";
            private int port = 3306;
            private String database = "UltimateReports";
            private String username = "root";
            private String password = "pa55w0rd";
            private String parameters = String.join("&",
                    "?autoReconnect=true", "useSSL=false",
                    "useUnicode=true", "characterEncoding=UTF-8");
        }

        @Comment("Hikari connection pool properties. Don't modify this unless you know what you're doing!")
        private PoolOptions connectionPool = new PoolOptions();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class PoolOptions {
            private int size = 8;
            private int idle = 8;
            private long lifetime = 1800000;
            private long keepalive = 30000;
            private long timeout = 3000;
        }

        @Comment("Names of tables to use on your database. Don't modify this unless you know what you're doing!")
        @Getter(AccessLevel.NONE)
        private Map<String, String> tableNames = Database.TableName.getDefaults();

        @NotNull
        public String getTableName(@NotNull Database.TableName tableName) {
            return tableNames.getOrDefault(tableName.name().toLowerCase(Locale.ENGLISH), tableName.getDefaultName());
        }

    }

    // Cross-server settings
    @Comment("Cross-server settings")
    private CrossServerSettings crossServer = new CrossServerSettings();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CrossServerSettings {

        @Comment("Whether to enable cross-server mode")
        private boolean enabled = false;

        @Comment("The server name in the Bungee/Velocity config. case-sensitive")
        private String serverName = "Survival-1";

        @Comment({"The cluster ID, used if you want multiple separate groups of UltimateReports servers.",
                "Do not change unless you know what you're doing, check the wiki for better explanation"})
        private String clusterId = "main";

        @Comment({"Type of network message broker to ues for data synchronization (PLUGIN_MESSAGE or REDIS)",
                "Always use REDIS if possible"})
        private Broker.Type brokerType = Broker.Type.PLUGIN_MESSAGE;

        @Comment("Settings for if you're using REDIS as your message broker")
        private RedisSettings redis = new RedisSettings();

        @Getter
        @Configuration
        @NoArgsConstructor
        public static class RedisSettings {
            private String host = "localhost";
            private int port = 6379;
            @Comment("Password for your Redis server. Leave blank if you're not using a password.")
            private String password = "";
            private boolean useSsl = false;

            @Comment({"Settings for if you're using Redis Sentinels.",
                    "If you're not sure what this is, please ignore this section."})
            private SentinelSettings sentinel = new SentinelSettings();

            @Getter
            @Configuration
            @NoArgsConstructor
            public static class SentinelSettings {
                private String masterName = "";
                @Comment("List of host:port pairs")
                private List<String> nodes = Lists.newArrayList();
                private String password = "";
            }
        }
    }

    @Comment("General Settings")
    private GeneralSettings general = new GeneralSettings();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GeneralSettings {

        @Comment("the plugin prefix")
        private String prefix = "[UltimateReports](gold bold) [»](gray) ";

        @Comment("The format used to show dates")
        private String dateFormat = "dd/MM/yyyy HH:mm:ss";

        @Comment("Allow players to report offline players?")
        private boolean reportOffline = true;

        @Comment("default cooldown for punishments. in minutes")
        private int defaultCooldown = 30;

        @Comment("Should the plugin send notifications to the Reporter when the report is updated?")
        private boolean sendNotification = true;

        @Comment("Should the plugin reopen the Reports List GUI after you process a report?")
        private boolean reopenGui = true;

    }

    @Comment({"Discord Settings.",
            "For cross-server users: Enable this ONLY in one server! Changes might require to restart the server.",
            "You can use webhooks, for just sending reports",
            "Or the bot to also be able to modify/answer/delete reports from Discord",
            "",
            "After you enable either (or both) of these options, restart the server and a new",
            "file will be generated to allow customization"})
    private DiscordSettings discord = new DiscordSettings();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DiscordSettings {

        @Comment("WebHook settings")
        private WebHookSettings webhook = new WebHookSettings();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class WebHookSettings {

            @Comment("Enable webhook?")
            private boolean enabled = false;

            @Comment("New Reports Webhook URL")
            private String reportsWebHookUrl = "";

            @Comment("New Comments Webhook URL")
            private String commentsWebHookUrl = "";
        }

        @Comment("Bot settings. Check the wiki for further information")
        private BotSettings bot = new BotSettings();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class BotSettings {

            @Comment("Enable the bot?")
            private boolean enabled = false;

            @Comment("Bot token")
            private String token = "";

            @Comment("The ID for the Guild where you will be using the bot")
            private String guildId = "";

            @Comment("The channel ID to send new reports to")
            private String reportsChannelId = "";

            @Comment("Should the plugin also send a message for new Comments?")
            private boolean sendNewComments = false;

            @Comment("The channel ID to send new comments to")
            private String commentsChannelId = "";
        }


    }

}
