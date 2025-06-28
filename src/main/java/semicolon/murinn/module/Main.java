package semicolon.murinn.module;

import semicolon.murinn.module.menu.internal.ModuleStateManager;
import semicolon.murinn.module.menu.internal.MenuListener;
import semicolon.murinn.module.build.BuildModuleManager;
import semicolon.murinn.module.command.ModuleCommand;
import semicolon.murinn.module.placeable.PlaceableModuleListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;
    private final BuildModuleManager buildModuleManager = new BuildModuleManager();

    public static Main plugin() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        init();
        registerCommands();
        registerEvents();
    }

    @Override
    public void onDisable() {
        buildModuleManager.disable();
        Bukkit.getScheduler().cancelTasks(this);
    }
    
    public BuildModuleManager getBuildManager() {
        return buildModuleManager;
    }

    private void init() {
        Bukkit.getScheduler().runTaskLater(this, ModuleStateManager::restoreAllModuleTimers, 40L);
    }

    private void registerCommands() {
        getCommand("module").setExecutor(new ModuleCommand());
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new PlaceableModuleListener(), this);
    }
}