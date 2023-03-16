package net.runelite.client.plugins.apker;

import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.plugins.Utils.Core;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.time.Duration;
import java.time.Instant;

import static java.awt.event.KeyEvent.VK_F2;

class PkToolsHotkeyListener extends MouseAdapter implements KeyListener {
    private Client client;

    private Instant lastPress;

    @Inject
    private PkToolsPlugin plugin;

    @Inject
    private PkToolsConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject Core core;

    @Inject
    private PkToolsHotkeyListener(Client client, PkToolsConfig config, PkToolsPlugin plugin) {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

		/*if (e.getKeyCode() == config.prayerKey().getKeyCode())
		{
			configManager.setConfiguration("apker", "autoPrayerSwitcherEnabled", !config.autoPrayerSwitcherEnabled());
		}*/

        try {
            if (lastPress != null && Duration.between(lastPress, Instant.now()).getNano() > 1000) {
                lastPress = null;
            }

            if (lastPress != null) {
                return;
            }

            int key_code = e.getKeyCode();

            if (key_code == config.key1().getKeyCode()) {
                addCommands(config.key1_script(), plugin);
            } else if (key_code == config.key2().getKeyCode()) {
                addCommands(config.key2_script(), plugin);
            } else if (key_code == config.key3().getKeyCode()) {
                addCommands(config.key3_script(), plugin);
            } else if (key_code == config.key4().getKeyCode()) {
                addCommands(config.key4_script(), plugin);
            } else if (key_code == config.key5().getKeyCode()) {
                addCommands(config.key5_script(), plugin);
            } else if (key_code == config.key6().getKeyCode()) {
                addCommands(config.key6_script(), plugin);
            } else if (key_code == config.key7().getKeyCode()) {
                addCommands(config.key7_script(), plugin);
            } else if (key_code == config.key8().getKeyCode()) {
                addCommands(config.key8_script(), plugin);
            }
        } catch (Throwable ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static String builder(final String scriptCommand)
    {
        switch (scriptCommand.toLowerCase())
        {
            case "inventory":
                return "inventory";
            case "dd":
                return "dd";
            case "rigour":
                return "rigour";
            case "augury":
                return "augury";
            case "piety":
                return "piety";
            //	case "maultoags":
            //		return new MaulAGSCommand();
            //	case "agstomaul":
            ///		return new agstomaul();
            //case "singlemaulags":
            //	return new singlemaulags();
            case "incrediblereflexes":
                return "incrediblereflexes";
            case "ultimatestrength":
                return "ultimatestrength";
            case "steelskin":
                return "steelskin";
            case "eagleeye":
                return "eagleeye";
            case "mysticmight":
                return "mysticmight";
            case "protectfrommagic":
                return "protectfrommagic";
            case "protectfrommelee":
                return "protectfrommelee";
            case "protectfrommissiles":
                return "protectfrommissiles";
            case "freeze":
                return "freeze";
            case "blood":
                return  "blood";
            case "firesurge":
                return  "firesurge";
            case "vengeance":
                return  "vengeance";
            case "superglass":
                return "superglass";
            case "humidify":
                return "humidify";
            case "string jewellery":
                return "string jewellery";
            case "plank make":
                return "plank make";
            case "alch":
                return "alch";
            case "teleblock":
                return  "teleblock";
            case "entangle":
                return  "entangle";
            case "spec":
                return  "spec";
            case "wait":
                return  "wait";
            case "clickenemy":
                return  "clickenemy";
            case "protectitem":
                return "protectitem";

            default:
                //if (scriptCommand.toLowerCase().startsWith("group"))
                //{
                //	return new GroupCommand(scriptCommand.replace("group", ""));
                //}
                if (scriptCommand.toLowerCase().startsWith("id_"))
                {
                    return scriptCommand.replace("id_", "id_");
                }
                else
                {
                    return "ERROR";
                }
        }
    }

    private void addCommands(String command, PkToolsPlugin plugin) {
        for (String c : command.split("\\s*\n\\s*")) {
            plugin.commandList.add(builder(c));
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}
