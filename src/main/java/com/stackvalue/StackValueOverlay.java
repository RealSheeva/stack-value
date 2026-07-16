package com.stackvalue;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import lombok.Value;

import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class StackValueOverlay extends WidgetItemOverlay
{
	private final StackValuePlugin plugin;
	private final StackValueConfig config;
	private final ItemManager itemManager;

	/**
	 * Shading an item recolors every pixel of its sprite, which is far too slow to redo each frame
	 * for a full bank. Keyed on color as well as item, so a re-tiered or recolored item misses the
	 * cache and is redrawn rather than served stale.
	 */
	private final Cache<FillKey, Image> fillCache;

	@Inject
	private StackValueOverlay(StackValuePlugin plugin, StackValueConfig config, ItemManager itemManager)
	{
		this.plugin = plugin;
		this.config = config;
		this.itemManager = itemManager;

		showOnEquipment();
		showOnInventory();
		showOnBank();

		fillCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.maximumSize(256)
				.build();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
	{
		final int quantity = itemWidget.getQuantity();
		final Color color = plugin.getValueColor(stackValue(itemId, quantity));

		// A fully transparent tier color is how a tier is hidden, so skip the whole item.
		if (color == null || color.getAlpha() == 0)
		{
			return;
		}

		final Rectangle bounds = itemWidget.getCanvasBounds();
		final int x = (int) bounds.getX();
		final int y = (int) bounds.getY();

		final BufferedImage outline = itemManager.getItemOutline(itemId, quantity, color);
		if (outline != null)
		{
			graphics.drawImage(outline, x, y, null);
		}

		final int opacity = config.getFillOpacity();
		if (!config.showFill() || opacity <= 0)
		{
			return;
		}

		// The slider sets the fill's transparency outright, independent of the tier color's own
		// alpha, so it can run from invisible up to completely replacing the item's colors.
		final Image fill = fill(itemId, quantity, ColorUtil.colorWithAlpha(color, opacity));
		if (fill != null)
		{
			graphics.drawImage(fill, x, y, null);
		}
	}

	/**
	 * Value of a whole stack. Widened to a long because a single stack can exceed the range of an
	 * int, which would wrap negative and drop the item into the lowest tier.
	 */
	private long stackValue(int itemId, int quantity)
	{
		return (long) itemManager.getItemPrice(itemId) * quantity;
	}

	private Image fill(int itemId, int quantity, Color color)
	{
		final FillKey key = new FillKey(itemId, quantity, color.getRGB());

		final Image cached = fillCache.getIfPresent(key);
		if (cached != null)
		{
			return cached;
		}

		final AsyncBufferedImage sprite = itemManager.getImage(itemId, quantity, false);

		// Sprites load in the background and are blank until they arrive. Caching one now would pin
		// an empty fill in place, so draw nothing and let a later frame build it for real.
		if (sprite == null || isBlank(sprite))
		{
			return null;
		}

		final Image fill = ImageUtil.fillImage(sprite, color);
		fillCache.put(key, fill);
		return fill;
	}

	private static boolean isBlank(BufferedImage image)
	{
		for (int x = 0; x < image.getWidth(); x++)
		{
			for (int y = 0; y < image.getHeight(); y++)
			{
				if ((image.getRGB(x, y) >>> 24) != 0)
				{
					return false;
				}
			}
		}

		return true;
	}

	@Value
	private static class FillKey
	{
		int itemId;
		int quantity;
		int rgb;
	}
}
