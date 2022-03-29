package net.runelite.client.plugins.NQuickPray;

import java.util.Arrays;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
public enum NQuickPrayType
{
	PRAYER_POTION(ItemID.SANFEW_SERUM1, ItemID.SANFEW_SERUM2, ItemID.SANFEW_SERUM3, ItemID.SANFEW_SERUM4, ItemID.SUPER_RESTORE1, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE4,
			ItemID.BLIGHTED_SUPER_RESTORE1, ItemID.BLIGHTED_SUPER_RESTORE2, ItemID.BLIGHTED_SUPER_RESTORE3,
			ItemID.BLIGHTED_SUPER_RESTORE4, ItemID.PRAYER_POTION1, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION4);

	public int[] ItemIDs;

	NQuickPrayType(int... ids)
	{
		this.ItemIDs = ids;
	}

	public boolean containsId(int id)
	{
		return Arrays.stream(this.ItemIDs).anyMatch(x -> x == id);
	}

	public WidgetItem getItemFromInventory(Client client)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

		if (inventoryWidget == null)
		{
			return null;
		}

		for (WidgetItem item : inventoryWidget.getWidgetItems())
		{
			if (Arrays.stream(ItemIDs).anyMatch(i -> i == item.getId()))
			{
				return item;
			}
		}

		return null;
	}
}
