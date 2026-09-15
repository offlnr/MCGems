package org.offlnr.offlnrPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.offlnr.offlnrPlugin.command.GemCommand;
import org.offlnr.offlnrPlugin.command.MiniMessageCommand;
import org.offlnr.offlnrPlugin.gem.GemBlockItemFactory;
import org.offlnr.offlnrPlugin.gem.GemBlockRegistry;
import org.offlnr.offlnrPlugin.gem.GemHeadFactory;
import org.offlnr.offlnrPlugin.gem.GemHoeFactory;
import org.offlnr.offlnrPlugin.gem.GemMineManager;
import org.offlnr.offlnrPlugin.listener.GemMiningListener;

public final class OfflnrPlugin extends JavaPlugin {

    private GemBlockRegistry gemRegistry;
    private GemMineManager mineManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        gemRegistry = new GemBlockRegistry(this);
        gemRegistry.load();

        mineManager = new GemMineManager(this, gemRegistry);
        mineManager.load();

        GemHoeFactory hoeFactory = new GemHoeFactory(this);
        GemHeadFactory headFactory = new GemHeadFactory(this);
        GemBlockItemFactory blockItemFactory = new GemBlockItemFactory(this);

        getServer().getPluginManager().registerEvents(
                new GemMiningListener(gemRegistry, hoeFactory, headFactory, blockItemFactory, mineManager), this);

        GemCommand gemCommand = new GemCommand(gemRegistry, hoeFactory, blockItemFactory, mineManager);
        getCommand("gema").setExecutor(gemCommand);
        getCommand("gema").setTabCompleter(gemCommand);
        getCommand("mm").setExecutor(new MiniMessageCommand());

        getLogger().info("Sistema de Gemas cargado (" + gemRegistry.size() + " gemas registradas, "
                + mineManager.mines().size() + " minas).");
    }

    @Override
    public void onDisable() {
        if (gemRegistry != null) {
            gemRegistry.save();
        }
        if (mineManager != null) {
            mineManager.save();
        }
    }
}
