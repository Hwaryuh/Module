package kr.murinn.module;

import kr.murinn.module.GUI.ScreenListener;
import kr.murinn.module.command.ModuleCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        registerCommands();
        registerEvents();
    }

    @Override
    public void onDisable() {
    }

    private void registerCommands() {
        getCommand("module").setExecutor(new ModuleCommand());
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new ScreenListener(), this);
    }
}