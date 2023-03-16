package net.runelite.client.plugins.apker.ScriptCommand;

import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.NUtils.PUtils;
import net.runelite.client.plugins.apker.PkToolsConfig;
import net.runelite.client.plugins.apker.PkToolsPlugin;

import javax.inject.Inject;

import static net.runelite.client.plugins.apker.PkToolsHotkeyListener.getTag;

public interface ScriptCommand {

    void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager);

    default void clickPrayer(WidgetInfo widgetInfo, Client client, PkToolsPlugin plugin) {
        try {
            Widget prayer_widget = client.getWidget(widgetInfo);

            if (prayer_widget == null) {
                return;
            }

            if (client.getBoostedSkillLevel(Skill.PRAYER) <= 0) {
                return;
            }

            //plugin.entryList.add(new MenuEntry("Activate", prayer_widget.getName(), 1, MenuAction.CC_OP.getId(), -1, prayer_widget.getId(), false));
            plugin.clientThread.invoke(() -> client.invokeMenuAction("Activate", prayer_widget.getName(), 1, MenuAction.CC_OP.getId(), -1, prayer_widget.getId()));
            //click(client);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //use this for spells that are one click cast
    default void castSpell(WidgetInfo widgetInfo, Client client, PkToolsPlugin plugin) {
        try {
            Widget spell_widget = client.getWidget(widgetInfo);

            if (spell_widget == null) {
                return;
            }

            //plugin.entryList.add(new MenuEntry(spell_widget.getTargetVerb(), spell_widget.getName(), 1, MenuAction.CC_OP.getId(), spell_widget.getItemId(), spell_widget.getId(), false));
            plugin.clientThread.invoke(() -> client.invokeMenuAction(spell_widget.getTargetVerb(), spell_widget.getName(), 1, MenuAction.CC_OP.getId(), spell_widget.getItemId(), spell_widget.getId()));
            //click(client);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //use this for spells that are cast on a player or item
    default void clickSpell(WidgetInfo widgetInfo, Client client, PkToolsPlugin plugin) {
        try {
            Widget spell_widget = client.getWidget(widgetInfo);

            if (spell_widget == null) {
                return;
            }

            //plugin.entryList.add(new MenuEntry(spell_widget.getTargetVerb(), spell_widget.getName(), 0, MenuAction.WIDGET_TYPE_2.getId(), spell_widget.getItemId(), spell_widget.getId(), false));
            plugin.clientThread.invoke(() -> client.invokeMenuAction(spell_widget.getTargetVerb(), spell_widget.getName(), 0, MenuAction.WIDGET_TARGET.getId(), spell_widget.getItemId(), spell_widget.getId()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class RigourCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        if (client.getVar(Prayer.RIGOUR.getVarbit()) == 1) {
            return;
        }
        clickPrayer(WidgetInfo.PRAYER_RIGOUR, client, plugin);
    }
}

class AuguryCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        if (client.getVar(Prayer.AUGURY.getVarbit()) == 1) {
            return;
        }

        clickPrayer(WidgetInfo.PRAYER_AUGURY, client, plugin);
    }
}

class PietyCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        if (client.getVar(Prayer.PIETY.getVarbit()) == 1) {
            return;
        }

        clickPrayer(WidgetInfo.PRAYER_PIETY, client, plugin);
    }
}

class IncredibleReflexesCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        if (client.getVar(Prayer.INCREDIBLE_REFLEXES.getVarbit()) == 1) {
            return;
        }

        clickPrayer(WidgetInfo.PRAYER_INCREDIBLE_REFLEXES, client, plugin);
    }
}

class UltimateStrengthCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        if (client.getVar(Prayer.ULTIMATE_STRENGTH.getVarbit()) == 1) {
            return;
        }

        clickPrayer(WidgetInfo.PRAYER_ULTIMATE_STRENGTH, client, plugin);
    }
}

class SteelSkinCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        if (client.getVar(Prayer.STEEL_SKIN.getVarbit()) == 1) {
            return;
        }

        clickPrayer(WidgetInfo.PRAYER_STEEL_SKIN, client, plugin);
    }
}

class EagleEyeCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        if (client.getVar(Prayer.EAGLE_EYE.getVarbit()) == 1) {
            return;
        }

        clickPrayer(WidgetInfo.PRAYER_EAGLE_EYE, client, plugin);
    }
}

class MysticMightCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        if (client.getVar(Prayer.MYSTIC_MIGHT.getVarbit()) == 1) {
            return;
        }

        clickPrayer(WidgetInfo.PRAYER_MYSTIC_MIGHT, client, plugin);
    }
}

class ProtectFromMagicCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        if (client.getVar(Prayer.PROTECT_FROM_MAGIC.getVarbit()) == 1) {
            return;
        }

        clickPrayer(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC, client, plugin);
    }
}

class ProtectFromMissilesCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        if (client.getVar(Prayer.PROTECT_FROM_MISSILES.getVarbit()) == 1) {
            return;
        }

        clickPrayer(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES, client, plugin);
    }
}

class ProtectFromMeleeCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        if (client.getVar(Prayer.PROTECT_FROM_MELEE.getVarbit()) == 1) {
            return;
        }

        clickPrayer(WidgetInfo.PRAYER_PROTECT_FROM_MELEE, client, plugin);
    }
}

class ProtectItemCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        if (client.getVar(Prayer.PROTECT_ITEM.getVarbit()) == 1) {
            return;
        }

        clickPrayer(WidgetInfo.PRAYER_PROTECT_ITEM, client, plugin);
    }
}

class BloodCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        try {
            int boosted_level = client.getBoostedSkillLevel(Skill.MAGIC);

            if (boosted_level >= 80 && boosted_level < 92) {
                clickSpell(WidgetInfo.SPELL_BLOOD_BLITZ, client, plugin);
            } else {
                clickSpell(WidgetInfo.SPELL_BLOOD_BARRAGE, client, plugin);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class FreezeCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        try {
            int boosted_level = client.getBoostedSkillLevel(Skill.MAGIC);

            if (boosted_level >= 82 && boosted_level < 94) {
                clickSpell(WidgetInfo.SPELL_ICE_BLITZ, client, plugin);
            } else {
                clickSpell(WidgetInfo.SPELL_ICE_BARRAGE, client, plugin);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class FireSurgeCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        try {
            int boosted_level = client.getBoostedSkillLevel(Skill.MAGIC);

            if (boosted_level >= 95) {
                clickSpell(WidgetInfo.SPELL_FIRE_SURGE, client, plugin);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class VengeanceCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        try {
            if (client.getBoostedSkillLevel(Skill.MAGIC) < 94) {
                return;
            }

            castSpell(WidgetInfo.SPELL_VENGEANCE, client, plugin);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class TeleBlockCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        try {
            if (client.getBoostedSkillLevel(Skill.MAGIC) < 85) {
                return;
            }

            clickSpell(WidgetInfo.SPELL_TELE_BLOCK, client, plugin);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class EntangleCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        try {
            if (client.getBoostedSkillLevel(Skill.MAGIC) < 79) {
                return;
            }

            clickSpell(WidgetInfo.SPELL_ENTANGLE, client, plugin);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class SpecCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        try {
            boolean spec_enabled = (client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 1);

            if (spec_enabled) {
                return;
            }

            //plugin.entryList.add(new MenuEntry("Use <col=00ff00>Special Attack</col>", "", 1, MenuAction.CC_OP.getId(), -1, 38862884, false));
            plugin.clientThread.invoke(() -> client.invokeMenuAction("Use <col=00ff00>Special Attack</col>", "", 1, MenuAction.CC_OP.getId(), -1, 38862884));
            //click(client);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class GroupCommand implements ScriptCommand {
    int groupNumber;
    PUtils utils;
    GroupCommand(String groupNumberString) {
        try {
            this.groupNumber = Integer.parseInt(groupNumberString);
        } catch (Exception e) {
            //ignored
        }
    }

    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        try {
            /*Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
            if (inventoryWidget == null)
            {
                return;
            }*/

            for (WidgetItem item : utils.getAllInventoryItems()) {
                if (("Group " + groupNumber).equalsIgnoreCase(getTag(configManager, item.getId()))) {
                    //plugin.entryList.add(new MenuEntry("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false));
                    utils.useItem(item.getId(),"wield");
                    //plugin.clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
                }
            }
            //click(client);
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
class singlemaulags implements ScriptCommand{
    PUtils utils;
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager){
        try{
            plugin.SingleMaulAgs = true;
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class agstomaul implements ScriptCommand{
    PUtils utils;
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager){
        try{
            plugin.AgsToMaul = true;
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class MaulAGSCommand implements ScriptCommand{
    PUtils utils;
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager){
        try{
            plugin.MaultoAgs = true;
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class ItemCommand implements ScriptCommand {
    int itemId;
    PUtils utils;
    ItemCommand(String itemIdString) {
        try {
            this.itemId = Integer.parseInt(itemIdString);
        } catch (Exception e) {
            //ignored
        }
    }

    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        try {
            Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
            if (inventoryWidget == null) {
                return;
            }
            plugin.clientThread.invoke(() -> client.invokeMenuAction("", "", itemId, MenuAction.ITEM_SECOND_OPTION.getId(), utils.getInventoryWidgetItem(itemId).getIndex(), 9764864));
            //WidgetItem item = utils.getInventoryWidgetItem(Collections.singletonList(itemId));
          //  utils.useItem(itemId, "wield", "wear");
            //plugin.entryList.add(new MenuEntry("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false));
            //plugin.clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
            //click(client);
            return;
        }
        catch (Throwable e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class OpenInventoryWidget implements ScriptCommand{
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        try {
            plugin.clientThread.invoke(() -> client.invokeMenuAction("", "", 1, 57,  -1, 10747958));
        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}


class DeathDot implements ScriptCommand{
    PUtils utils;
    MenuEntry targetMenu;
    @Inject
    private ItemManager itemManager;
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        try {
            if (plugin.lastEnemy != null) {
                Player enemy = utils.findNearestPlayer(plugin.lastEnemy.getName());
                utils.walk(enemy.getWorldLocation());
                //plugin.clientThread.invoke(() -> client.invokeMenuAction("", "", enemy.getPlayerId(), MenuAction.WALK.getId(),  enemy.getWorldLocation().getX(), enemy.getWorldLocation().getX()));
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class ClickEnemyCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        try {
            if (plugin.lastEnemy != null) {
                //plugin.entryList.add(new MenuEntry("", "", plugin.lastEnemy.getPlayerId(), client.getSpellSelected() ? MenuAction.SPELL_CAST_ON_PLAYER.getId() : MenuAction.PLAYER_SECOND_OPTION.getId(), 0, 0, false));
                plugin.clientThread.invoke(() -> client.invokeMenuAction("", "", plugin.lastEnemy.getPlayerId(), client.getSpellSelected() ? MenuAction.WIDGET_TARGET_ON_PLAYER.getId() : MenuAction.PLAYER_SECOND_OPTION.getId(), 0, 0));
                //click(client);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class WaitCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        try {
            //Thread.sleep(500);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class ExceptionCommand implements ScriptCommand {
    public void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager) {
        System.out.println("Command could not be read.");
    }
}
