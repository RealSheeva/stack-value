package com.stackvalue;

import com.google.inject.Provides;
import javax.inject.Inject;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.Color;

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
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	@Provides
	StackValueConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StackValueConfig.class);
	}

	Color getValueColor(final long stackValue)
	{
		if (stackValue < config.getCommonItemValue())
		{
			return config.getCommonItemColor();
		}
		else if (stackValue < config.getUncommonItemValue())
		{
			return config.getUncommonItemColor();
		}
		else if (stackValue < config.getRareItemValue())
		{
			return config.getRareItemColor();
		}
		else if (stackValue < config.getEpicItemValue())
		{
			return config.getEpicItemColor();
		}

		return config.getLegendaryItemColor();
	}
}
