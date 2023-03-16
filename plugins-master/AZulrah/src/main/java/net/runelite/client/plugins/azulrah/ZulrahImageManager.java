package net.runelite.client.plugins.azulrah;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//



import java.awt.image.BufferedImage;
import net.runelite.api.Prayer;
import net.runelite.client.plugins.azulrah.AZulrahPlugin;
import net.runelite.client.plugins.azulrah.ZulrahType;
import net.runelite.client.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ZulrahImageManager {
	private static final Logger log = LoggerFactory.getLogger(ZulrahImageManager.class);
	private static final BufferedImage[] zulrahBufferedImages = new BufferedImage[3];
	private static final BufferedImage[] smallZulrahBufferedImages = new BufferedImage[3];
	private static final BufferedImage[] prayerBufferedImages = new BufferedImage[2];

	ZulrahImageManager() {
	}

	static BufferedImage getZulrahBufferedImage(ZulrahType type) {
		switch(type) {
			case RANGE:
				if (zulrahBufferedImages[0] == null) {
					zulrahBufferedImages[0] = getBufferedImage("zulrah_range.png");
				}

				return zulrahBufferedImages[0];
			case MAGIC:
				if (zulrahBufferedImages[1] == null) {
					zulrahBufferedImages[1] = getBufferedImage("zulrah_magic.png");
				}

				return zulrahBufferedImages[1];
			case MELEE:
				if (zulrahBufferedImages[2] == null) {
					zulrahBufferedImages[2] = getBufferedImage("zulrah_melee.png");
				}

				return zulrahBufferedImages[2];
			default:
				return null;
		}
	}

	static BufferedImage getSmallZulrahBufferedImage(ZulrahType type) {
		switch(type) {
			case RANGE:
				if (smallZulrahBufferedImages[0] == null) {
					smallZulrahBufferedImages[0] = getBufferedImage("zulrah_range.png");
				}

				return smallZulrahBufferedImages[0];
			case MAGIC:
				if (smallZulrahBufferedImages[1] == null) {
					smallZulrahBufferedImages[1] = getBufferedImage("zulrah_magic.png");
				}

				return smallZulrahBufferedImages[1];
			case MELEE:
				if (smallZulrahBufferedImages[2] == null) {
					smallZulrahBufferedImages[2] = getBufferedImage("zulrah_melee.png");
				}

				return smallZulrahBufferedImages[2];
			default:
				return null;
		}
	}

	static BufferedImage getProtectionPrayerBufferedImage(Prayer prayer) {
		switch(prayer) {
			case PROTECT_FROM_MAGIC:
				if (prayerBufferedImages[0] == null) {
					prayerBufferedImages[0] = getBufferedImage("protect_from_magic.png");
				}

				return prayerBufferedImages[0];
			case PROTECT_FROM_MISSILES:
				if (prayerBufferedImages[1] == null) {
					prayerBufferedImages[1] = getBufferedImage("protect_from_missiles.png");
				}

				return prayerBufferedImages[1];
			default:
				return null;
		}
	}

	private static BufferedImage getBufferedImage(String path) {
		return ImageUtil.getResourceStreamFromClass(AZulrahPlugin.class, path);
	}
}
