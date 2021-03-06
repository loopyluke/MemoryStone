package za.dats.bukkit.memorystone;

import java.io.IOException;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import za.dats.bukkit.memorystone.economy.EconomyManager;
//import za.dats.bukkit.memorystone.ui.SpoutLocationPopupManager;
import za.dats.bukkit.memorystone.util.StructureManager;

public class MemoryStonePlugin extends JavaPlugin {
	private PluginDescriptionFile pdf;
	private PluginManager pm;
	private StructureManager structureManager = new StructureManager(this, "[MemoryStone] ");
	private MemoryStoneManager memoryStoneManager = new MemoryStoneManager(this);
	private CompassManager compassManager = new CompassManager(this);
	// private SpoutLocationPopupManager spoutLocationPopupManager;
	private EconomyManager economyManager = new EconomyManager();
	private static MemoryStonePlugin instance;

	public void onDisable() {
	}

	public void info(String log) {
		getServer().getLogger().info("[MemoryStone] " + log);
	}

	public void warn(String log) {
		getServer().getLogger().warning("[MemoryStone] " + log);
	}

	public void onEnable() {
		instance = this;
		try {
			Config.init(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Try loading metrics
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}

		pm = getServer().getPluginManager();
		pdf = getDescription();

		info(pdf.getName() + " version " + pdf.getVersion() + " is enabled!");

		economyManager.loadEconomy();

		structureManager.addStructureListener(memoryStoneManager);
		structureManager.registerEvents();

		memoryStoneManager.registerEvents();
		compassManager.registerEvents();

		if (isSpoutEnabled()) {
			// spoutLocationPopupManager = new SpoutLocationPopupManager(this);
			// spoutLocationPopupManager.registerEvents();
		}
	}

	public boolean isSpoutEnabled() {
		if (pm.isPluginEnabled("Spout")) {
			return true;
		}
		return false;
	}

	public StructureManager getStructureManager() {
		return structureManager;
	}

	public MemoryStoneManager getMemoryStoneManager() {
		return memoryStoneManager;
	}

	public CompassManager getCompassManager() {
		return compassManager;
	}

	// public SpoutLocationPopupManager getSpoutLocationPopupManager() {
	// return spoutLocationPopupManager;
	// }

	public static MemoryStonePlugin getInstance() {
		return instance;
	}

	public EconomyManager getEconomyManager() {
		return economyManager;
	}
}
