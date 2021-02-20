package com.iridium.iridiumskyblock.database;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.configs.SQL;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.db.DatabaseTypeUtils;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private final SQL sqlConfig;
    private final IridiumSkyblock iridiumSkyblock;
    private final ConnectionSource connectionSource;

    private final Dao<User, UUID> userDao;
    private final Dao<Island, Integer> islandDao;

    @Getter
    private final List<User> userList;
    @Getter
    private final List<Island> islandList;

    public DatabaseManager(IridiumSkyblock iridiumSkyblock) throws SQLException {
        this.sqlConfig = iridiumSkyblock.getSql();
        this.iridiumSkyblock = iridiumSkyblock;
        String databaseURL = getDatabaseURL();

        connectionSource = new JdbcConnectionSource(
                databaseURL,
                sqlConfig.username,
                sqlConfig.password,
                DatabaseTypeUtils.createDatabaseType(databaseURL)
        );

        TableUtils.createTableIfNotExists(connectionSource, User.class);
        TableUtils.createTableIfNotExists(connectionSource, Island.class);

        this.userDao = DaoManager.createDao(connectionSource, User.class);
        this.islandDao = DaoManager.createDao(connectionSource, Island.class);

        userDao.setAutoCommit(getDatabaseConnection(), false);
        islandDao.setAutoCommit(getDatabaseConnection(), false);

        this.userList = getUsers();
        this.islandList = getIslands();
    }

    /**
     * @return The Database Url String
     */

    private @NotNull String getDatabaseURL() {
        switch (sqlConfig.driver) {
            case MYSQL:
            case MARIADB:
            case POSTGRESQL:
                return "jdbc:" + sqlConfig.driver + "://" + sqlConfig.host + ":" + sqlConfig.port + "/" + sqlConfig.database;
            case SQLSERVER:
                return "jdbc:sqlserver://" + sqlConfig.host + ":" + sqlConfig.port + ";databaseName=" + sqlConfig.database;
            case H2:
                return "jdbc:h2:file:" + sqlConfig.database;
            case SQLITE:
                return "jdbc:sqlite:" + new File(iridiumSkyblock.getDataFolder(), sqlConfig.database + ".db");
        }
        throw new RuntimeException("How did we get here?");
    }

    private DatabaseConnection getDatabaseConnection() throws SQLException {
        return connectionSource.getReadWriteConnection(null);
    }

    /**
     * @return a List of all users
     */

    private @NotNull List<User> getUsers() {
        try {
            return userDao.queryForAll();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * @return a List of all islands
     */

    private @NotNull List<Island> getIslands() {
        try {
            return islandDao.queryForAll();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * @param uuid The uuid of the player
     * @return the User class of the player
     */

    public Optional<User> getUserByUUID(@NotNull UUID uuid) {
        return userList.stream().filter(user -> user.getUuid().equals(uuid)).findFirst();
    }

    /**
     * @param id The id  of the island
     * @return The island
     */

    public Optional<Island> getIslandById(int id) {
        return islandList.stream().filter(island -> island.getId() == id).findFirst();
    }

    /**
     * Saves all users
     */
    public void saveUsers() {
        try {
            for (User user : userList) {
                userDao.createOrUpdate(user);
            }
            userDao.commit(getDatabaseConnection());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Saves all islands
     */
    public void saveIslands() {
        try {
            for (Island island : islandList) {
                islandDao.createOrUpdate(island);
            }
            islandDao.commit(getDatabaseConnection());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}