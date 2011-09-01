package za.dats.bukkit.memorystone.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.event.screen.ScreenListener;
import org.getspout.spoutapi.event.spout.SpoutListener;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.keyboard.KeyboardManager;
import org.getspout.spoutapi.player.SpoutPlayer;

import za.dats.bukkit.memorystone.MemoryStonePlugin;

public class SpoutLocationPopupManager extends ScreenListener {
    private static final int PAGE_COLUMNS = 2;
    private static final int PAGE_ROWS = 5;
    private static final int PAGE_SIZE = PAGE_COLUMNS * PAGE_ROWS;

    public interface LocationPopupListener {
	public void selected(String entry);
    }

    private class LocationPopup {
	int page = 0;
	List<String> locations;
	Map<UUID, String> locationButtons = new HashMap<UUID, String>();
	GenericPopup popup;
	UUID cancelId;
	UUID nextId;
	UUID prevId;
	SpoutPlayer player;
	String heading;
	LocationPopupListener listener;
	
	void updatePage() {
	    for (Widget widget : popup.getAttachedWidgets()) {
		popup.removeWidget(widget);
	    }
	    locationButtons.clear();
	    
	    int center = player.getMainScreen().getWidth() / 2;
	    int widthScale = player.getMainScreen().getWidth() / 100;
	    int heightScale = player.getMainScreen().getHeight() / 100;

	    popup.setBgVisible(true);

	    GenericLabel label = new GenericLabel(heading);
	    label.setAlign(WidgetAnchor.CENTER_CENTER);
	    label.setX(center).setY(heightScale * 7);
	    popup.attachWidget(plugin, label);

	    String pageText = "" + (page + 1) + " / " + ((locations.size() / (PAGE_SIZE)+1));
	    GenericLabel pageLabel = new GenericLabel(pageText);
	    pageLabel.setAlign(WidgetAnchor.CENTER_CENTER);
	    pageLabel.setX(center).setY(heightScale * 12);
	    popup.attachWidget(plugin, pageLabel);

	    for (int i = 0; i < PAGE_SIZE; i++) {
		int currentEntry = i + (page * PAGE_SIZE);
		if (currentEntry >= locations.size()) {
		    break;
		}

		int row = i % PAGE_ROWS;
		int col = i / PAGE_ROWS;

		String text = locations.get(currentEntry);
		GenericButton locationButton = new GenericButton(text);
		locationButton.setX(center - (widthScale * 35) + (widthScale * col * 40)).setY(
			heightScale * (row + 2) * 13);
		locationButton.setWidth(widthScale * 30).setHeight(heightScale * 10);
		popup.attachWidget(plugin, locationButton);

		locationButtons.put(locationButton.getId(), text);
	    }

	    if (page > 0) {
		GenericButton previousButton = new GenericButton("<<");
		previousButton.setX(center - (widthScale * 25)).setY(heightScale * 90);
		previousButton.setWidth(widthScale * 10).setHeight(heightScale * 10);
		popup.attachWidget(plugin, previousButton);
		prevId = previousButton.getId();
	    }

	    GenericButton declineButton = new GenericButton("Cancel");
	    declineButton.setX(center - (widthScale * 10)).setY(heightScale * 94);
	    declineButton.setWidth(widthScale * 20).setHeight(heightScale * 10);
	    popup.attachWidget(plugin, declineButton);
	    cancelId = declineButton.getId();

	    if ((page + 1) * PAGE_SIZE < locations.size()) {
		GenericButton nextButton = new GenericButton(">>");
		nextButton.setX(center + (widthScale * 15)).setY(heightScale * 90);
		nextButton.setWidth(widthScale * 10).setHeight(heightScale * 10);
		popup.attachWidget(plugin, nextButton);
		nextId = nextButton.getId();
	    }
	    
	    popup.setDirty(true);
	}

	void createPopup(SpoutPlayer sPlayer, Set<String> locationSet, String text, LocationPopupListener listener) {
	    popup = new GenericPopup();
	    this.listener = listener;
	    this.locations = new ArrayList<String>(locationSet);
	
	    player = sPlayer;
	    heading = text;
	    updatePage();
	    player.getMainScreen().attachPopupScreen(popup);
	    
	}

    }

    private final JavaPlugin plugin;
    private HashMap<UUID, LocationPopup> popups = new HashMap<UUID, SpoutLocationPopupManager.LocationPopup>();

    public SpoutLocationPopupManager(JavaPlugin plugin) {
	this.plugin = plugin;
    }

    public void registerEvents() {
	plugin.getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, this, Priority.Normal, plugin);
    }

    public void showPopup(SpoutPlayer sPlayer, Set<String> locations, String text, LocationPopupListener listener) {
	LocationPopup newPopup = new LocationPopup();
	newPopup.createPopup(sPlayer, locations, text, listener);
	popups.put(newPopup.popup.getId(), newPopup);
    }

    private void closePopup(LocationPopup popup) {
	popup.popup.close();
	popups.remove(popup);
    }
    
    

    @Override
    public void onButtonClick(ButtonClickEvent event) {
	LocationPopup locationPopup = popups.get(event.getScreen().getId());
	if (locationPopup == null) {
	    return;
	}

	UUID id = event.getButton().getId();
	if (id.equals(locationPopup.nextId)) {
	    locationPopup.page++;
	    locationPopup.updatePage();
	} else if (event.getButton().getId().equals(locationPopup.prevId)) {
	    locationPopup.page--;
	    locationPopup.updatePage();
	} else if (locationPopup.locationButtons.containsKey(id)) {
	    closePopup(locationPopup);
	    locationPopup.listener.selected(locationPopup.locationButtons.get(id));
	} else {
	    closePopup(locationPopup);
	}
    }
}