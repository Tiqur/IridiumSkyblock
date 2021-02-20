package com.iridium.iridiumskyblock;

import com.heretere.hdl.dependency.maven.annotation.MavenDependency;
import com.heretere.hdl.relocation.annotation.Relocation;
import com.heretere.hdl.spigot.DependencyPlugin;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.commands.CommandManager;
import com.iridium.iridiumskyblock.configs.Configuration;
import com.iridium.iridiumskyblock.configs.Messages;
import com.iridium.iridiumskyblock.configs.SQL;
import com.iridium.iridiumskyblock.database.DatabaseManager;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.sql.SQLException;

@MavenDependency("com|fasterxml|jackson|core:jackson-databind:2.12.1")
@MavenDependency("com|fasterxml|jackson|core:jackson-core:2.12.1")
@MavenDependency("com|fasterxml|jackson|core:jackson-annotations:2.12.1")
@MavenDependency("com|fasterxml|jackson|dataformat:jackson-dataformat-yaml:2.12.1")
@MavenDependency("org|yaml:snakeyaml:1.27")
@Relocation(from = "org|yaml", to = "com|iridium|iridiumskyblock")
@Getter
public class IridiumSkyblock extends DependencyPlugin {
    private Persist persist;

    private CommandManager commandManager;
    private DatabaseManager databaseManager;

    private Configuration configuration;
    private Messages messages;
    private SQL sql;

    @Override
    public void load() {

    }

    @Override
    public void enable() {
        this.persist = new Persist(Persist.PersistType.YAML, this);
        this.commandManager = new CommandManager("iridiumskyblock", this);
        loadConfigs();
        saveConfigs();
        try {
            this.databaseManager = new DatabaseManager(this);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        new IridiumSkyblockAPI(this);
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, this::saveData, 0, 20 * 60 * 5);
        getLogger().info("----------------------------------------");
        getLogger().info("");
        getLogger().info(getDescription().getName() + " Enabled!");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("");
        getLogger().info("----------------------------------------");
    }

    @Override
    public void disable() {
        saveData();
        getLogger().info("-------------------------------");
        getLogger().info("");
        getLogger().info(getDescription().getName() + " Disabled!");
        getLogger().info("");
        getLogger().info("-------------------------------");
    }

    public void saveData() {
        getDatabaseManager().saveIslands();
        getDatabaseManager().saveUsers();
    }

    public void loadConfigs() {
        this.configuration = persist.load(Configuration.class);
        this.messages = persist.load(Messages.class);
        this.sql = persist.load(SQL.class);
    }

    public void saveConfigs() {
        this.persist.save(configuration);
        this.persist.save(messages);
        this.persist.save(sql);
    }
}