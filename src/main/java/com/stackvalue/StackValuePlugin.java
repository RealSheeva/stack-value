package com.stackvalue;

// External
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

// RuneLite Plugins
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

// RuneLite
import net.runelite.client.config.ConfigManager;

// UI
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.*;

@Slf4j
@PluginDescriptor(
		name = "Stack Value"
)
public class StackValuePlugin extends Plugin
{
	@Inject
	private StackValueConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private StackValueOverlay overlay;

	@Override
	protected void startUp() throws Exception
	{
		log.info("ItemRarity started!");
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("ItemRarity stopped!");
		overlayManager.remove(overlay);
	}

	@Provides
	StackValueConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StackValueConfig.class);
	}

	Color getRarityColor(final int itemPrice)
	{
		if (itemPrice < config.getCommonItemValue())
		{
			return config.getCommonItemColor();
		}
		else if (itemPrice < config.getUncommonItemValue())
		{
			return config.getUncommonItemColor();
		}
		else if (itemPrice < config.getRareItemValue())
		{
			return config.getRareItemColor();
		}
		else if (itemPrice < config.getEpicItemValue())
		{
			return config.getEpicItemColor();
		}

		return config.getLegendaryItemColor();
	}
}
