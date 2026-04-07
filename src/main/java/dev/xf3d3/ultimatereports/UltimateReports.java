package dev.xf3d3.ultimatereports;

import co.aikar.commands.PaperCommandManager;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import de.themoep.minedown.adventure.MineDown;
import dev.xf3d3.ultimatereports.commands.ReportCommand;
import dev.xf3d3.ultimatereports.commands.ReportsCommand;
import dev.xf3d3.ultimatereports.config.*;
import dev.xf3d3.ultimatereports.database.*;
import dev.xf3d3.ultimatereports.discord.DiscordBot;
import dev.xf3d3.ultimatereports.discord.WebHook;
import dev.xf3d3.ultimatereports.hooks.PapiExpansion;
import dev.xf3d3.ultimatereports.hooks.ViaVersionHook;
import dev.xf3d3.ultimatereports.listeners.PlayerConnectEvent;
import dev.xf3d3.ultimatereports.listeners.PlayerDisconnectEvent;
import dev.xf3d3.ultimatereports.managers.ReportsManager;
import dev.xf3d3.ultimatereports.managers.UsersManager;
import dev.xf3d3.ultimatereports.models.Report;
import dev.xf3d3.ultimatereports.models.User;
import dev.xf3d3.ultimatereports.network.Broker;
import dev.xf3d3.ultimatereports.network.PluginMessageBroker;
import dev.xf3d3.ultimatereports.network.RedisBroker;
import dev.xf3d3.ultimatereports.utils.TaskRunner;
import dev.xf3d3.ultimatereports.utils.Utils;
import dev.xf3d3.ultimatereports.utils.gson.GsonUtils;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.william278.desertwell.util.ThrowingConsumer;
import net.william278.desertwell.util.Version;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class UltimateReports extends JavaPlugin implements TaskRunner, GsonUtils, PluginMessageListener {
    private static UltimateReports instance;
    @Getter @Setter public boolean loaded = false;

    private static final int METRICS_ID = 28016;

    @Nullable private Broker broker;
    @Getter private Database database;
    @Getter private ReportsManager reportsManager;
    @Getter private UsersManager usersManager;
    @Getter private Utils utils;

    @Getter @Setter private Settings settings;
    @Getter @Setter private Messages messages;
    @Getter @Setter private GUI guiConfig;
    @Getter @Setter private Dialog dialogConfig;
    @Getter @Setter private Rewards rewardsConfig;
    @Getter @Setter @Nullable private Discord discordConfig;

    @Nullable @Getter private ViaVersionHook viaVersionHook;

    @Nullable private WebHook webHookSender;
    @Nullable private DiscordBot discordBot;

    private FoliaLib foliaLib;
    private PaperCommandManager manager;

    @Override
    public void onLoad() {
        instance = this;

        initialize("permissions", (plugin) -> {
            addPermission("ultimatereports.player", "Basic permission", PermissionDefault.TRUE);
            addPermission("ultimatereports.reports", "Basic permission", PermissionDefault.TRUE);
            addPermission("ultimatereports.report", "Send a new report", PermissionDefault.TRUE);
            addPermission("ultimatereports.notify", "Change notification status", PermissionDefault.TRUE);
            addPermission("ultimatereports.reports.comment", "Add a comment to a report", PermissionDefault.TRUE);
            addPermission("ultimatereports.reward", "Claim rewards for your reports", PermissionDefault.TRUE);

            addPermission("ultimatereports.reports.manage", "Manage Reports", PermissionDefault.OP);
            addPermission("ultimatereports.reports.teleport", "Teleport to report positions", PermissionDefault.OP);
            addPermission("ultimatereports.reload", "Reload the plugin", PermissionDefault.OP);
            addPermission("ultimatereports.about", "Get plugin info", PermissionDefault.OP);
            addPermission("ultimatereports.reports.check", "Check a report", PermissionDefault.OP);
            addPermission("ultimatereports.cooldown.add", "Add cooldown to a player", PermissionDefault.OP);
            addPermission("ultimatereports.cooldown.reset", "Reset a player's cooldown", PermissionDefault.OP);
            addPermission("ultimatereports.cooldown.check", "Check a player's cooldown", PermissionDefault.OP);
        });
    }


    @Override
    public void onEnable() {
        this.foliaLib = new FoliaLib(this);
        this.manager = new PaperCommandManager(this);

        this.reportsManager = new ReportsManager(this);
        this.usersManager = new UsersManager(this);

        // Load settings and locales
        initialize("plugin config & locale files", (plugin) -> loadConfigs());

        // Initialize the database
        initialize(getSettings().getDatabase().getType().getDisplayName() + " database connection", (plugin) -> {
            this.database = switch (getSettings().getDatabase().getType()) {
                case MYSQL, MARIADB -> new MySqlDatabase(this);
                case SQLITE -> new SQLiteDatabase(this);
                case H2 -> new H2Database(this);
                case POSTGRESQL -> new PostgreSqlDatabase(this);
            };

            database.initialize();
        });

        // Initialize message broker
        if (getSettings().getCrossServer().isEnabled()) {
            initialize(getSettings().getCrossServer().getBrokerType() + " broker", (plugin) -> {
                final Broker.Type brokerType = getSettings().getCrossServer().getBrokerType();

                this.broker = switch (brokerType) {
                    case PLUGIN_MESSAGE -> new PluginMessageBroker(this);
                    case REDIS -> new RedisBroker(this);
                };

                broker.initialize();
            });
        }

        if (getSettings().getDiscord().getWebhook().isEnabled()) {
            initialize("Discord WebHooks", (plugin) -> this.webHookSender = new WebHook(this));
        }

        if (getSettings().getDiscord().getBot().isEnabled()) {
            initialize("Discord Bot", (plugin) -> this.discordBot = new DiscordBot(this));
        }

        initialize("Commands and Async command completion", (plugin) -> {
            this.manager.registerCommand(new ReportCommand(this));
            this.manager.registerCommand(new ReportsCommand(this));

            this.manager.getCommandCompletions().registerAsyncCompletion("onlineUsers", c -> getUsersManager().getUserList().stream()
                    .map(User::getUsername)
                    .collect(Collectors.toList())
            );
            this.manager.getCommandCompletions().registerAsyncCompletion("reports", c -> getReportsManager().getReports().stream()
                    .map(Report::getId)
                    .sorted()
                    .map(String::valueOf)
                    .collect(Collectors.toList())
            );
            this.manager.getCommandCompletions().registerAsyncCompletion("playerReports", c -> getReportsManager().getReports().stream()
                    .filter(report -> report.getReporter().equals(c.getPlayer().getUniqueId()))
                    .map(Report::getId)
                    .sorted()
                    .map(String::valueOf)
                    .collect(Collectors.toList())
            );
        });

        // Register events
        initialize("events", (plugin) -> {
            getServer().getPluginManager().registerEvents(new PlayerConnectEvent(this), this);
            getServer().getPluginManager().registerEvents(new PlayerDisconnectEvent(this), this);
        });

        // Load the reports
        initialize("reports", (plugin) -> runAsync(task -> reportsManager.loadReports()));

        if (getServer().getPluginManager().getPlugin("ViaVersion") != null) {
            initialize("ViaVersion Hook", (plugin) -> this.viaVersionHook = new ViaVersionHook());
        }

        // Register PlaceHolderAPI hooks
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {

            sendConsole("-------------------------------------------");
            sendConsole("&6UltimateReports: &3PlaceholderAPI found!");

            initialize("placeholderapi", (plugin) -> new PapiExpansion(this).register());

            sendConsole("&6UltimateReports: &3External placeholders enabled!");
            sendConsole("-------------------------------------------");
        } else {
            sendConsole("-------------------------------------------");
            sendConsole("&6UltimateReports: &cPlaceholderAPI not found!");
            sendConsole("&6UltimateReports: &cExternal placeholders disabled!");
            sendConsole("-------------------------------------------");
        }

        initialize("metrics", (plugin) -> this.registerMetrics(METRICS_ID));

        this.utils = new Utils(this);
    }

    @Override
    public void onDisable() {
        // Cancel plugin tasks and close the database connection
        getScheduler().cancelAllTasks();
        database.close();
    }

    public void log(@NotNull Level level, @NotNull String message, @Nullable Throwable... throwable) {
        if (throwable != null && throwable.length > 0) {
            getLogger().log(level, message, throwable[0]);
            return;
        }
        getLogger().log(level, message);
    }

    private void initialize(@NotNull String name, @NotNull ThrowingConsumer<UltimateReports> runner) {
        log(Level.INFO, "Initializing " + name + "...");
        try {
            runner.accept(this);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize " + name, e);
        }
        log(Level.INFO, "Successfully initialized " + name);
    }

    YamlConfigurationProperties.Builder<?> YAML_CONFIGURATION_PROPERTIES = YamlConfigurationProperties.newBuilder()
            .charset(StandardCharsets.UTF_8)
            .setNameFormatter(NameFormatters.LOWER_UNDERSCORE);


    public void initializePluginChannels() {
        getServer().getMessenger().registerIncomingPluginChannel(this, PluginMessageBroker.BUNGEE_CHANNEL_ID, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, PluginMessageBroker.BUNGEE_CHANNEL_ID);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (broker != null && broker instanceof PluginMessageBroker pluginMessenger
                && getSettings().getCrossServer().getBrokerType() == Broker.Type.PLUGIN_MESSAGE) {
            pluginMessenger.onReceive(channel, player, message);
        }
    }

    public void loadConfigs() {
        setSettings(YamlConfigurations.update(
                getDataFolder().toPath().resolve("config.yml"),
                Settings.class,
                YAML_CONFIGURATION_PROPERTIES.header(Settings.CONFIG_HEADER).build()
        ));

        setMessages(YamlConfigurations.update(
                getDataFolder().toPath().resolve("messages.yml"),
                Messages.class,
                YAML_CONFIGURATION_PROPERTIES.header(Messages.MESSAGES_HEADER).build()
        ));

        setGuiConfig(YamlConfigurations.update(
                getDataFolder().toPath().resolve("gui.yml"),
                GUI.class,
                YAML_CONFIGURATION_PROPERTIES.header(GUI.GUI_HEADER).build()
        ));

        if (getSettings().getDiscord().getBot().isEnabled() || getSettings().getDiscord().getWebhook().isEnabled()) {
            setDiscordConfig(YamlConfigurations.update(
                    getDataFolder().toPath().resolve("discord.yml"),
                    Discord.class,
                    YAML_CONFIGURATION_PROPERTIES.header(Discord.DISCORD_HEADER).build()
            ));
        }

        if (getSettings().isUseDialogs()) {
            setDialogConfig(YamlConfigurations.update(
                    getDataFolder().toPath().resolve("dialog.yml"),
                    Dialog.class,
                    YAML_CONFIGURATION_PROPERTIES.header(Dialog.DIALOG_HEADER).build()
            ));
        }

        if (getSettings().isEnableRewardsSystem()) {
            setRewardsConfig(YamlConfigurations.update(
                    getDataFolder().toPath().resolve("rewards.yml"),
                    Rewards.class,
                    YAML_CONFIGURATION_PROPERTIES.header(Rewards.REWARDS_HEADER).build()
            ));
        }
    }

    public void registerMetrics(int metricsId) {
        try {
            final Metrics metrics = new Metrics(this, metricsId);

            metrics.addCustomChart(new SimplePie("database_type", () -> getSettings().getDatabase().getType().getDisplayName()));
            metrics.addCustomChart(new SimplePie("discord_webhook", () -> Boolean.toString(getSettings().getDiscord().getWebhook().isEnabled())));
            metrics.addCustomChart(new SimplePie("discord_bot", () -> Boolean.toString(getSettings().getDiscord().getBot().isEnabled())));

            metrics.addCustomChart(new SimplePie("cross_server", () -> Boolean.toString(getSettings().getCrossServer().isEnabled())));
            if (getSettings().getCrossServer().isEnabled()) {
                metrics.addCustomChart(new SimplePie("broker_type", () -> getSettings().getCrossServer().getBrokerType().name().toLowerCase()));
            }

        } catch (Exception e) {
            log(Level.WARNING, "Failed to register bStats metrics", e);
        }
    }

    private void addPermission(String name, String description, PermissionDefault defaultValue) {
        if (getServer().getPluginManager().getPermission(name) == null) {
            Permission perm = new Permission(name, description, defaultValue);
            getServer().getPluginManager().addPermission(perm);
        }
    }

    private boolean isPlaceholderAPIEnabled() {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPIPlugin");

            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public String replacePlaceholders(OfflinePlayer player, String text) {
        if (text == null) return "";

        if (isPlaceholderAPIEnabled()) {

            return PlaceholderAPI.setPlaceholders(player, text);
        } else {

            return text;
        }
    }

    public List<String> replacePlaceholders(OfflinePlayer player, List<String> text) {
        if (text == null) return Collections.emptyList();

        if (isPlaceholderAPIEnabled()) {

            return text.stream().map(line -> PlaceholderAPI.setPlaceholders(player, line)).collect(Collectors.toList());
        } else {

            return text;
        }
    }



    @NotNull
    public Version getPluginVersion() {
        return Version.fromString(this.getPluginMeta().getVersion());
    }

    public void sendConsole(String text) {
        Bukkit.getConsoleSender().sendMessage(MineDown.parse(text));
    }

    public static UltimateReports getPlugin() {
        return instance;
    }

    @NotNull
    public Optional<Broker> getMessageBroker() {
        return Optional.ofNullable(broker);
    }

    @NotNull
    public Optional<WebHook> getWebHookSender() {
        return Optional.ofNullable(webHookSender);
    }

    @NotNull
    public Optional<DiscordBot> getDiscordBot() {
        return Optional.ofNullable(discordBot);
    }

    @Override
    @NotNull
    public PlatformScheduler getScheduler() {
        return foliaLib.getScheduler();
    }
}
