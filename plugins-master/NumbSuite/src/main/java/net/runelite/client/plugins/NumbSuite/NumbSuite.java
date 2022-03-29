
package net.runelite.client.plugins.NumbSuite;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ExternalPluginsChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.http.api.ge.GrandExchangeClient;
import net.runelite.rs.api.RSClient;
import okhttp3.*;
import org.pf4j.Extension;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.Files;
import java.security.AllPermission;
import java.security.Key;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.awt.event.KeyEvent.VK_ENTER;
@Slf4j
@SuppressWarnings("unused")
@Singleton
@Extension
@PluginDescriptor(
        name = "NumbSuite",
        description = "Numb Plugin Suite"
)

public class NumbSuite extends Plugin {

    @Provides
    NumbSuiteConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(NumbSuiteConfig.class);
    }
    @Inject
    ExecutorService executorService;
    @Inject
    private ConfigManager configManager;
    @Inject
    private Client client;
    @Inject
    public ClientThread clientThread;
    @Inject
    private ChatMessageManager chatMessageManager;
    @Inject
    private ItemManager itemManager;
    @Inject
    private GrandExchangeClient grandExchangeClient;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-88");
    public static final MediaType JSON2 = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType JSON3 = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private final String DAX_API_URL = "https://api.dax.cloud/walker/generatePath";
    public List<WorldPoint> currentPath = new LinkedList<WorldPoint>();
    MenuEntry targetMenu;
    public WorldPoint nextPoint;
    public boolean randomEvent;
    public boolean iterating;
    public boolean webWalking;
    private int nextFlagDist = -1;
    private boolean consumeClick;
    private boolean modifiedMenu;
    private int modifiedItemID;
    private int modifiedItemIndex;
    private int modifiedOpCode;
    private int coordX;
    private int coordY;
    private int nextRunEnergy;
    private boolean walkAction;
    public boolean retrievingPath;
    public final Map<TileItem, Tile> spawnedItems = new HashMap<>();
    protected static final Random random = new Random();

    @Provides
    GrandExchangeClient provideGrandExchangeClient(OkHttpClient okHttpClient) { return new GrandExchangeClient(okHttpClient); }

    public void sendGameMessage(String message)
    {
        String chatMessage = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(message)
                .build();

        chatMessageManager
                .queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
    }

    public int[] stringToIntArray(String string)
    {
        return Arrays.stream(string.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
    }


    public List<Integer> stringToIntList(String string)
    {
        return (string == null || string.trim().equals("")) ? List.of(0) :
                Arrays.stream(string.split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
    }

    @Nullable
    public Player findNearestPlayer(String name){
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new PlayerQuery()
                .nameEquals(name)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public Player findNearestPlayer(){
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new PlayerQuery()
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GameObject findNearestGameObject(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new GameObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
    public ObjectComposition getImpostorDefinition(int id) {
        ObjectComposition def = client.getObjectDefinition(id);
        return def.getImpostorIds() != null ? def.getImpostor() : def;
    }

    @Nullable
    public GameObject findNearestGameObject(String name)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new GameObjectQuery()
                .filter(object -> object.getName() != null && object.getName().toLowerCase().contains(name.toLowerCase()))
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public GameObject getObjectsExceptName(String name, String name2, String name3, int... ids)
    {
        List<GameObject> itemList = getGameObjects(ids);
        itemList.removeIf(item -> item.getName() == name);
        itemList.removeIf(item -> item.getName()== name2);
        itemList.removeIf(item -> item.getName()== name3);
        return itemList.isEmpty() ? null : itemList.get(0);
    }
    public GameObject getSomething (int... ids)
    {
        List<GameObject> itemList = getGameObjects(ids);
        return itemList.isEmpty() ? null : itemList.get(0);
    }

    public GameObject getbyname (int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }
        return new GameObjectQuery()
                .filter (i -> client.getObjectDefinition(i.getId()).getImpostor().getName().equals("Rock"))
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public ObjectComposition getImpostor (int id)
    {
        return client.getObjectDefinition(id).getImpostor();
    }

    public String getImpostorName (ObjectComposition object)
    {
        return object.getName();
    }

    public GameObject findAbyssObject(int ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        String ImpostorName = client.getObjectDefinition(ids).getImpostor().getName();
        if (ImpostorName == "Rock" || ImpostorName == "Eyes"){
            return (GameObject) client.getObjectDefinition(ids).getImpostor();
        }
        return null;
    }
    public DecorativeObject findNearestDecorativeObject(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new DecorativeObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GameObject findNearestGameObjectWithin(WorldPoint worldPoint, int dist, int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new GameObjectQuery()
                .isWithinDistance(worldPoint, dist)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GameObject findNearestGameObjectWithin(WorldPoint worldPoint, int dist, Collection<Integer> ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new GameObjectQuery()
                .isWithinDistance(worldPoint, dist)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public NPC findNearestNpc(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new NPCQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public NPC findNearestNpc(String... names)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new NPCQuery()
                .nameContains(names)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public NPC findNearestNpcWithin(WorldPoint worldPoint, int dist, Collection<Integer> ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new NPCQuery()
                .isWithinDistance(worldPoint, dist)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public NPC findNearestNpcWithin(WorldPoint worldPoint, int dist, int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new NPCQuery()
                .isWithinDistance(worldPoint, dist)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public NPC findNearestAttackableNpcWithin(WorldPoint worldPoint, int dist, String name, boolean exactnpcname)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        if (exactnpcname)
        {
            return new NPCQuery()
                    .isWithinDistance(worldPoint, dist)
                    .filter(npc -> npc.getName() != null && npc.getName().equalsIgnoreCase(name) && npc.getInteracting() == null && npc.getHealthRatio() != 0)
                    .result(client)
                    .nearestTo(client.getLocalPlayer());
        }
        else
        {
            return new NPCQuery()
                    .isWithinDistance(worldPoint, dist)
                    .filter(npc -> npc.getName() != null && npc.getName().toLowerCase().contains(name.toLowerCase()) && npc.getInteracting() == null && npc.getHealthRatio() != 0)
                    .result(client)
                    .nearestTo(client.getLocalPlayer());
        }
    }

    @Nullable
    public NPC findNearestNpcTargetingLocal(String name, boolean exactnpcname)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        if (exactnpcname)
        {
            return new NPCQuery()
                    .filter(npc -> npc.getName() != null && npc.getName().equalsIgnoreCase(name) && npc.getInteracting() == client.getLocalPlayer() && npc.getHealthRatio() != 0)
                    .result(client)
                    .nearestTo(client.getLocalPlayer());
        }
        else
        {
            return new NPCQuery()
                    .filter(npc -> npc.getName() != null && npc.getName().toLowerCase().contains(name.toLowerCase()) && npc.getInteracting() == client.getLocalPlayer() && npc.getHealthRatio() != 0)
                    .result(client)
                    .nearestTo(client.getLocalPlayer());
        }

    }
    @Nullable
    public WallObject findNearestWallObject(int ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new WallObjectQuery()
                .filter(object -> object.getId() == ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public WallObject findNearestWallObject(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new WallObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
    @Nullable
    public WallObject findNearestWallObject(String name)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new WallObjectQuery()
                .filter(object -> object.getName() != null && object.getName().toLowerCase().contains(name.toLowerCase()))
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
    @Nullable
    public WallObject findWallObjectWithin(WorldPoint worldPoint, int radius, int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new WallObjectQuery()
                .isWithinDistance(worldPoint, radius)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public WallObject findWallObjectWithin(WorldPoint worldPoint, int radius, Collection<Integer> ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new WallObjectQuery()
                .isWithinDistance(worldPoint, radius)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public DecorativeObject findNearestDecorObject(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new DecorativeObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    @Nullable
    public GroundObject findNearestGroundObject(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new GroundObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
    public List<GameObject> getGameObjects(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return new ArrayList<>();
        }

        return new GameObjectQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    public List<GameObject> getGameObjects(String... names)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return new ArrayList<>();
        }

        return new GameObjectQuery()
                .nameEquals(names)
                .result(client)
                .list;
    }
    public List<GameObject> getLocalGameObjects(int distanceAway, int... ids)
    {
        if (client.getLocalPlayer() == null)
        {
            return new ArrayList<>();
        }
        List<GameObject> localGameObjects = new ArrayList<>();
        for (GameObject gameObject : getGameObjects(ids))
        {
            if (gameObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) < distanceAway)
            {
                localGameObjects.add(gameObject);
            }
        }
        return localGameObjects;
    }

    public List<NPC> getNPCs(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return new ArrayList<>();
        }

        return new NPCQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    public List<NPC> getNPCs(String... names)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return new ArrayList<>();
        }

        return new NPCQuery()
                .nameContains(names)
                .result(client)
                .list;
    }

    public NPC getFirstNPCWithLocalTarget()
    {
        assert client.isClientThread();

        List<NPC> npcs = client.getNpcs();
        for (NPC npc : npcs)
        {
            if (npc.getInteracting() == client.getLocalPlayer())
            {
                return npc;
            }
        }
        return null;
    }

    public List<WallObject> getWallObjects(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return new ArrayList<>();
        }

        return new WallObjectQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    public List<DecorativeObject> getDecorObjects(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return new ArrayList<>();
        }

        return new DecorativeObjectQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    public List<GroundObject> getGroundObjects(int... ids)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return new ArrayList<>();
        }

        return new GroundObjectQuery()
                .idEquals(ids)
                .result(client)
                .list;
    }

    @Nullable
    public TileObject findNearestObject(int... ids)
    {
        GameObject gameObject = findNearestGameObject(ids);

        if (gameObject != null)
        {
            return gameObject;
        }

        WallObject wallObject = findNearestWallObject(ids);

        if (wallObject != null)
        {
            return wallObject;
        }
        DecorativeObject decorativeObject = findNearestDecorObject(ids);

        if (decorativeObject != null)
        {
            return decorativeObject;
        }

        return findNearestGroundObject(ids);
    }

    @Nullable
    public GameObject findNearestBank()
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new GameObjectQuery()
                .idEquals(Banks.ALL_BANKS)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
    public GameObject findNearestBankNoDepositBoxes()
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return new GameObjectQuery()
                .idEquals(Banks.NO_DEPOSIT_BOXES)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public List<Item> getEquippedItems()
    {
        assert client.isClientThread();

        List<Item> equipped = new ArrayList<>();
        Item[] items = client.getItemContainer(InventoryID.EQUIPMENT).getItems();
        for (Item item : items)
        {
            if (item.getId() == -1 || item.getId() == 0)
            {
                continue;
            }
            equipped.add(item);
        }
        return equipped;
    }

    public boolean isItemEquipped(Collection<Integer> itemIds)
    {
        assert client.isClientThread();

        Item[] items = client.getItemContainer(InventoryID.EQUIPMENT).getItems();
        for (Item item : items)
        {
            if (itemIds.contains(item.getId()))
            {
                return true;
            }
        }
        return false;
    }

    public int getTabHotkey(Tab tab)
    {
        assert client.isClientThread();

        final int var = client.getVarbitValue(client.getVarps(), tab.getVarbit());
        final int offset = 111;

        switch (var)
        {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                return var + offset;
            case 13:
                return 27;
            default:
                return -1;
        }
    }

    public WidgetInfo getSpellWidgetInfo(String spell)
    {
        assert client.isClientThread();
        return Spells.getWidget(spell);
    }

    public WidgetInfo getPrayerWidgetInfo(String spell)
    {
        assert client.isClientThread();
        return PrayerMap.getWidget(spell);
    }

    public Widget getSpellWidget(String spell)
    {
        assert client.isClientThread();
        return client.getWidget(Spells.getWidget(spell));
    }

    public Widget getPrayerWidget(String spell)
    {
        assert client.isClientThread();
        return client.getWidget(PrayerMap.getWidget(spell));
    }

    public boolean pointOnScreen(Point check)
    {
        int x = check.getX(), y = check.getY();
        return x > client.getViewportXOffset() && x < client.getViewportWidth()
                && y > client.getViewportYOffset() && y < client.getViewportHeight();
    }

    public void typeString(String string)
    {
        assert !client.isClientThread();

        for (char c : string.toCharArray())
        {
            pressKey(c);
        }
    }

    public void pressKey(char key)
    {
        keyEvent(401, key);
        keyEvent(402, key);
        keyEvent(400, key);
    }

    public void pressKey(int key)
    {
        keyEvent(401, key);
        keyEvent(402, key);
        //keyEvent(400, key);
    }

    private void keyEvent(int id, char key)
    {
        KeyEvent e = new KeyEvent(
                client.getCanvas(), id, System.currentTimeMillis(),
                0, KeyEvent.VK_UNDEFINED, key
        );

        client.getCanvas().dispatchEvent(e);
    }

    private void keyEvent(int id, int key)
    {
        KeyEvent e = new KeyEvent(
                client.getCanvas(), id, System.currentTimeMillis(),
                0, key, KeyEvent.CHAR_UNDEFINED
        );
        client.getCanvas().dispatchEvent(e);
    }

    public int getRandomIntBetweenRange(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public boolean isMoving()
    {
        int camX = client.getCameraX2();
        int camY = client.getCameraY2();
        sleep(25);
        return (camX != client.getCameraX() || camY != client.getCameraY()) && client.getLocalDestinationLocation() != null;
    }

    public boolean isMoving(LocalPoint lastTickLocalPoint)
    {
        return !client.getLocalPlayer().getLocalLocation().equals(lastTickLocalPoint);
    }

    public boolean isInteracting()
    {
        sleep(25);
        return isMoving() || client.getLocalPlayer().getAnimation() != -1;
    }

    public boolean isAnimating()
    {
        return client.getLocalPlayer().getAnimation() != -1;
    }

    private void walkTile(int x, int y)
    {
        RSClient rsClient = (RSClient) client;
        rsClient.setSelectedSceneTileX(x);
        rsClient.setSelectedSceneTileY(y);
        rsClient.setViewportWalking(true);
        rsClient.setCheckClick(false);
    }
    public void walk(LocalPoint localPoint)
    {
        coordX = localPoint.getSceneX() + getRandomIntBetweenRange(-Math.abs(0), Math.abs(0));
        coordY = localPoint.getSceneY() + getRandomIntBetweenRange(-Math.abs(0), Math.abs(0));
        walkAction = true;
        clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WALK.getId(), 0, 0));
        walkTile(coordX, coordY);
    }

    public void walk(WorldPoint worldPoint)
    {
        LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
        if (localPoint != null)
        {
            coordX = localPoint.getSceneX() + getRandomIntBetweenRange(-Math.abs(0), Math.abs(0));
            coordY = localPoint.getSceneY() + getRandomIntBetweenRange(-Math.abs(0), Math.abs(0));
            walkAction = true;
            clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WALK.getId(), 0, 0));
            walkTile(coordX, coordY);
        }
        else
        {
            log.info("WorldPoint to LocalPoint coversion is null");
        }
    }


    public static String post(String url, String json) throws IOException
    {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, json); // new
        log.info("Sending POST request: {}", body);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("key", "sub_DPjXXzL5DeSiPf")
                .addHeader("secret", "PUBLIC-KEY")
                .post(body)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        return response.body().string();
    }

    public boolean util() throws IOException {
        int lines = 0;
        InetAddress ip = InetAddress.getLocalHost();
        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
        byte[] mac = network.getHardwareAddress();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
        }
        URL url = new URL("https://api.auth.gg/v1/");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "login");
        params.put("aid", "420713");
        params.put("apikey", "144716626862995161742345388379677778742362");
        params.put("secret", "4X6vj8O41QpwydJVo8mS6ujyVDaB33MuK15");
        params.put("username", NumbSuiteConfig.u());
        params.put("password", NumbSuiteConfig.p());
        params.put("hwid", sb.toString());
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", "124");
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            lines++;
            if (lines == 1) {
                if (inputLine.contains("success")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Inject
    private PluginManager manager;
    @Inject
    private EventBus eventBus;

    private ByteArrayClassLoader cl = null; //byte array class loader
    private List<Plugin> scannedPlugins; //the array which stores the instances of loaded plugins, used for dependencies (i think)

    /**
     * installPlugins
     *
     * this method will install plugins on run time from a File target
     * @param target either a jar file, or a directory of jar files.
     */
    public void install(File target, boolean delete) {
        if (!createWorkspace())
            return;

        ArrayList<Class<?>> loadedPlugins = new ArrayList<>();
        File[] pluginJars = { target };
        if (target.isDirectory())
            pluginJars = target.listFiles();

        for (File plugin : Objects.requireNonNull(pluginJars)) {
            try {
                byte[] pluginJarBytes = Files.readAllBytes(plugin.toPath());
                loadedPlugins.add(cl(pluginJarBytes));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (delete)
                    plugin.delete();
            }
        }
        ins(loadedPlugins);
    }

    public void mem(byte[] jarInBytes) {
        if (!createWorkspace())
            return;

        ArrayList<Class<?>> loadedPlugins = new ArrayList<>();
        try {
            loadedPlugins.add(cl(jarInBytes));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ins(loadedPlugins);
        }
    }

    public void no() throws IOException {
        no1();
        no2();
        no3();
        no4();
        no5();
        no6();
        no7();
        no8();
    }

    private void no1() throws IOException {
        go("https://github.com/NumbPlugins/download/raw/main/NFarming-0.0.2.jar");
    }
    private void no2() throws IOException {
        go("https://github.com/NumbPlugins/download/raw/main/NGatherer-0.0.1.jar");
    }
    private void no3() throws IOException {
        go("https://github.com/NumbPlugins/download/raw/main/NInfernoHelper-0.0.1.jar");
    }
    private void no4() throws IOException {
        go("https://github.com/NumbPlugins/download/raw/main/NQuickPot-0.0.1.jar");
    }
    private void no5() throws IOException {
        go("https://github.com/NumbPlugins/download/raw/main/NQuickFighter-0.0.1.jar");
    }
    private void no6() throws IOException {
        go("https://github.com/NumbPlugins/download/raw/main/NQuickEat-0.0.2.jar");
    }
    private void no7() throws IOException {
        go("https://github.com/NumbPlugins/download/raw/main/NQuickPray-0.0.1.jar");
    }
    private void no8() throws IOException {
        go("https://github.com/NumbPlugins/download/raw/main/NTempoross-0.0.1.jar");
    }

    private void yes() throws IOException {
        go("https://github.com/NumbPlugins/download/raw/main/NumbSuite-0.0.4.jar");
    }

    private void go(String L) throws IOException {
        if (util()) {
            try {
                mem(bA(L));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Resets the work space (new class loader and clears scanned plugins)
     */
    public void reset() {
        cl = null;
        scannedPlugins = null;
    }

    /**
     * Streams a .jar file to a byte[] array.
     *
     * @param L JAR url to stream
     * @return the bytes of the jar file. (can be an encrypted file, and decrypted before using #installPluginFromMemory
     * @throws IOException
     */
    public byte[] bA(String L) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            URL url = new URL(L);
            is = url.openStream();
            byte[] byteChunk = new byte[4096];
            int n;
            while ((n = is.read(byteChunk)) > 0) {
                baos.write(byteChunk, 0, n);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return baos.toByteArray();
    }

    /**
     * Decrypts 128bit AES encryption
     * @return
     */
    public static byte[] dec(byte[] encoded, String key) {
        if (encoded == null)
            return null;
        String alg = "AES";
        Key salt = new SecretKeySpec(key.getBytes(), alg);
        try {
            Cipher cipher = Cipher.getInstance(alg);
            cipher.init(Cipher.DECRYPT_MODE, salt);
            byte[] decodedValue = Base64.getDecoder().decode(new String(encoded));
            return cipher.doFinal(decodedValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean createWorkspace() {
        if (cl == null) {
            try {
                cl = new ByteArrayClassLoader(getClass().getClassLoader());
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        if (cl == null)
            return false;
        return true;
    }

    private void ins(ArrayList<Class<?>> plugs) {
        if (plugs != null) {
            try {
                scannedPlugins = manager.loadPlugins(plugs, null);
                SwingUtilities.invokeLater(() -> {
                    try {
                        for (Plugin p : scannedPlugins) {
                            if (p == null)
                                continue;
                            manager.loadDefaultPluginConfiguration(Collections.singleton(p));
                            manager.startPlugin(p);
                        }
                    } catch (PluginInstantiationException e) {
                        e.printStackTrace();
                    }
                });
                eventBus.post(new ExternalPluginsChanged(new ArrayList<>()));
            } catch (PluginInstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    private Class<?> cl(byte[] jarData) throws Exception {
        ZipInputStream jis = new ZipInputStream(new ByteArrayInputStream(jarData));
        fillEntry(cl, new ByteArrayInputStream(jarData));

        ZipEntry entry;
        while ((entry = jis.getNextEntry()) != null) {
            if (entry.isDirectory() || !entry.getName().toLowerCase().endsWith(".class") || entry.getName().contains("$")) {
                continue;
            }
            String className = entry.getName().substring(0, entry.getName().length() - 6);
            className = className.replace('/', '.');
            try {
                Class<?> c = cl.loadClass(className);
                if (c == null || c.getSuperclass() == null || !c.getSuperclass().getName().toLowerCase().endsWith("plugin")) {
                    continue;
                }
                jis.close();
                return c;//.asSubclass(Plugin.class);
            } catch (Throwable e) {
                //jis.close();
                e.printStackTrace();
            }
        }
        jis.close();
        return null;
    }

    private void fillEntry(ByteArrayClassLoader injector, InputStream in) {
        try {
            JarInputStream jis = new JarInputStream(in);
            JarEntry je;
            String entryName;
            while ((je = jis.getNextJarEntry()) != null) {
                if (je.isDirectory())
                    continue;

                entryName = je.getName();
                String canonicalName = entryName.replaceAll("/", ".").replaceAll(".class", "");


                if (entryName.toLowerCase().endsWith(".mf"))
                    continue;
                if (entryName.endsWith(".class")) {
                    byte[] classBytes = injector.readClass(jis);
                    injector.classes.put(canonicalName, classBytes);
                } else if (entryName.toLowerCase().endsWith(".jar")) {
                    fillEntry(injector, new ByteArrayInputStream(injector.readClass(jis)));
                } else {
                    byte[] classBytes = injector.readClass(jis);
                    injector.resources.put(entryName, new ByteArrayInputStream(classBytes));
                    //injector.classes.put(entryName, classBytes);
                }
            }
            jis.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ByteArrayClassLoader extends ClassLoader {

        public HashMap<String, byte[]> classes = new HashMap<>();
        public HashMap<String, InputStream> resources = new HashMap<>();

        public ByteArrayClassLoader(ClassLoader l) throws ClassNotFoundException, NoSuchMethodException {
            super(l);
        }

        public void log(String string) {
            System.out.println(string);
        }

        public class BytesHandler extends URLStreamHandler {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return new ByteUrlConnection(u);
            }
        }

        public class ByteUrlConnection extends URLConnection {

            public ByteUrlConnection(URL url) {
                super(url);
            }

            @Override
            public void connect() {
            }

            @Override
            public InputStream getInputStream() {
                log("URL getResource(): " + this.getURL().getPath().substring(1));
                byte[] res = classes.get(this.getURL().getPath().substring(1));
                if (res != null)
                    return new ByteArrayInputStream(res);
                else
                    return new ByteArrayInputStream(new byte[0]);
            }
        }

        @Override
        public URL getResource(String name) {
            try {
                return new URL(null, "bytes:///" + name, new BytesHandler());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public InputStream getResourceAsStream(String name)
        {
            log("InputStream getResourceAsStream(): " + name);
            if (resources.containsKey(name)) {
                return resources.get(name);
            }
            return super.getResourceAsStream(name);
        }

        public byte[] readClass(InputStream stream) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while(true){
                int qwe = stream.read();
                if(qwe == -1) break;
                baos.write(qwe);
            }
            return baos.toByteArray();
        }

        @Override
        public Class loadClass(String name) {
            try {
                return this.getParent().loadClass(name);
            } catch (ClassNotFoundException e) {
                return findClass(name);
            }
        }

        @Override
        public Class findClass(String name) {
            byte[] classBytes = classes.get(name);

            if (classBytes == null)
                return null;

            final ProtectionDomain classesPd =
                    new ProtectionDomain(ByteArrayClassLoader.class.getProtectionDomain().getCodeSource(), (Permissions) getAllPermissions(),
                            this,
                            ByteArrayClassLoader.class.getProtectionDomain().getPrincipals());

            //log("Defining class: " + name);

            return defineClass(name, classBytes, 0, classBytes.length, classesPd);
        }
    }

    public Object getSystemClassLoader() {
        Object cl = ClassLoader.getSystemClassLoader();
        ClassLoader parentClassLoader = ((ClassLoader)cl).getParent();
        if (parentClassLoader != null)
            return parentClassLoader;
        return cl;
    }

    public Object getAllPermissions() {
        final Permissions perms = new Permissions();
        return addPerms(perms);
    }

    private Object addPerms(Object perms) {
        ((Permissions)perms).add(new AllPermission());
        return perms;
    }

    public boolean isRunEnabled()
    {
        return client.getVarpValue(173) == 1;
    }

    public void handleRun(int minEnergy, int randMax)
    {
        assert client.isClientThread();
        if (nextRunEnergy < minEnergy || nextRunEnergy > minEnergy + randMax)
        {
            nextRunEnergy = getRandomIntBetweenRange(minEnergy, minEnergy + getRandomIntBetweenRange(0, randMax));
        }
        if (client.getEnergy() > nextRunEnergy ||
                client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0)
        {
            if (drinkStamPot(15 + getRandomIntBetweenRange(0, 30)))
            {
                return;
            }
            if (!isRunEnabled())
            {
                nextRunEnergy = 0;
                Widget runOrb = client.getWidget(WidgetInfo.MINIMAP_RUN_ORB);
                if (runOrb != null)
                {
                    enableRun(runOrb.getBounds());
                }
            }
        }
    }

    public void handleRun(int minEnergy, int randMax, int potEnergy)
    {
        assert client.isClientThread();
        if (nextRunEnergy < minEnergy || nextRunEnergy > minEnergy + randMax)
        {
            nextRunEnergy = getRandomIntBetweenRange(minEnergy, minEnergy + getRandomIntBetweenRange(0, randMax));
        }
        if (client.getEnergy() > (minEnergy + getRandomIntBetweenRange(0, randMax)) ||
                client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0)
        {
            if (drinkStamPot(potEnergy))
            {
                return;
            }
            if (!isRunEnabled())
            {
                nextRunEnergy = 0;
                Widget runOrb = client.getWidget(WidgetInfo.MINIMAP_RUN_ORB);
                if (runOrb != null)
                {
                    enableRun(runOrb.getBounds());
                }
            }
        }
    }

    public void enableRun(Rectangle runOrbBounds)
    {
        log.info("enabling run");
        clientThread.invoke(() -> client.invokeMenuAction("Toggle Run", "", 1, 57, -1, 10485783));
		/*executorService.submit(() ->
		{
			targetMenu = new MenuEntry("Toggle Run", "", 1, 57, -1, 10485783, false);
			delayMouseClick(runOrbBounds, getRandomIntBetweenRange(10, 250));
		});*/
    }

    public WidgetItem shouldStamPot(int energy)
    {
        if (!getInventoryItems(List.of(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4)).isEmpty()
                && client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0 && client.getEnergy() < energy && !isBankOpen())
        {
            return getInventoryWidgetItem(List.of(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3,
                    ItemID.STAMINA_POTION4, ItemID.ENERGY_POTION1, ItemID.ENERGY_POTION2, ItemID.ENERGY_POTION3, ItemID.ENERGY_POTION4));
        }
        else
        {
            return null;
        }
    }

    public boolean drinkStamPot(int energy)
    {
        WidgetItem staminaPotion = shouldStamPot(energy);
        if (staminaPotion != null)
        {
            clientThread.invoke(() -> client.invokeMenuAction("", "", staminaPotion.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), staminaPotion.getIndex(), 9764864));
            return true;
        }
        return false;
    }

    public void logout()
    {
        int param1 = (client.getWidget(WidgetInfo.LOGOUT_BUTTON) != null) ? 11927560 : 4522007;
        Widget logoutWidget = client.getWidget(WidgetInfo.LOGOUT_BUTTON);
        if (logoutWidget != null)
        {
            clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, param1));
        }
    }

    public boolean inventoryFull()
    {
        return getInventorySpace() <= 0;
    }

    public boolean inventoryEmpty()
    {
        return getInventorySpace() >= 28;
    }

    public int getInventorySpace()
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null)
        {
            return 28 - inventoryWidget.getWidgetItems().size();
        }
        else
        {
            return -1;
        }
    }

    public List<WidgetItem> getInventoryItems(Collection<Integer> ids)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        List<WidgetItem> matchedItems = new ArrayList<>();

        if (inventoryWidget != null)
        {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items)
            {
                if (ids.contains(item.getId()))
                {
                    matchedItems.add(item);
                }
            }
            return matchedItems;
        }
        return null;
    }

    public List<WidgetItem> getInventoryItems(String itemName)
    {
        return new InventoryWidgetItemQuery()
                .filter(i -> client.getItemComposition(i.getId())
                        .getName()
                        .toLowerCase()
                        .contains(itemName))
                .result(client)
                .list;
    }

    public Collection<WidgetItem> getAllInventoryItems()
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null)
        {
            return inventoryWidget.getWidgetItems();
        }
        return null;
    }

    public Collection<Integer> getAllInventoryItemIDs()
    {
        Collection<WidgetItem> inventoryItems = getAllInventoryItems();
        if (inventoryItems != null)
        {
            Set<Integer> inventoryIDs = new HashSet<>();
            for (WidgetItem item : inventoryItems)
            {
                if (inventoryIDs.contains(item.getId()))
                {
                    continue;
                }
                inventoryIDs.add(item.getId());
            }
            return inventoryIDs;
        }
        return null;
    }

    public List<Item> getAllInventoryItemsExcept(List<Integer> exceptIDs)
    {
        exceptIDs.add(-1); //empty inventory slot
        ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
        if (inventoryContainer != null)
        {
            Item[] items = inventoryContainer.getItems();
            List<Item> itemList = new ArrayList<>(Arrays.asList(items));
            itemList.removeIf(item -> exceptIDs.contains(item.getId()));
            return itemList.isEmpty() ? null : itemList;
        }
        return null;
    }

    public WidgetItem getInventoryWidgetItem(int... ids)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null)
        {
            return null;
        }

        for (WidgetItem item : inventoryWidget.getWidgetItems())
        {
            if (Arrays.stream(ids).anyMatch(i -> i == item.getId()))
            {
                return item;
            }
        }

        return null;
    }

    public WidgetItem getInventoryWidgetItem(int id)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null)
        {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items)
            {
                if (item.getId() == id)
                {
                    return item;
                }
            }
        }
        return null;
    }

    public WidgetItem getInventoryWidgetItem(Collection<Integer> ids)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null)
        {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items)
            {
                if (ids.contains(item.getId()))
                {
                    return item;
                }
            }
        }
        return null;
    }

    public Item getInventoryItemExcept(List<Integer> exceptIDs)
    {
        exceptIDs.add(-1); //empty inventory slot
        ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
        if (inventoryContainer != null)
        {
            Item[] items = inventoryContainer.getItems();
            List<Item> itemList = new ArrayList<>(Arrays.asList(items));
            itemList.removeIf(item -> exceptIDs.contains(item.getId()));
            return itemList.isEmpty() ? null : itemList.get(0);
        }
        return null;
    }

    public WidgetItem getInventoryItemMenu(ItemManager itemManager, String menuOption, int opcode, Collection<Integer> ignoreIDs)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null)
        {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items)
            {
                if (ignoreIDs.contains(item.getId()))
                {
                    continue;
                }
                String[] menuActions = itemManager.getItemComposition(item.getId()).getInventoryActions();
                for (String action : menuActions)
                {
                    if (action != null && action.equals(menuOption))
                    {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public WidgetItem getInventoryItemMenu(Collection<String> menuOptions)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null)
        {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items)
            {
                String[] menuActions = itemManager.getItemComposition(item.getId()).getInventoryActions();
                for (String action : menuActions)
                {
                    if (action != null && menuOptions.contains(action))
                    {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public WidgetItem getInventoryWidgetItemMenu(ItemManager itemManager, String menuOption, int opcode)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null)
        {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items)
            {
                String[] menuActions = itemManager.getItemComposition(item.getId()).getInventoryActions();
                for (String action : menuActions)
                {
                    if (action != null && action.equals(menuOption))
                    {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public int getInventoryItemCount(int id, boolean stackable)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        int total = 0;
        if (inventoryWidget != null)
        {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items)
            {
                if (item.getId() == id)
                {
                    if (stackable)
                    {
                        return item.getQuantity();
                    }
                    total++;
                }
            }
        }
        return total;
    }

    public int getInventoryItemStackableQuantity(int id)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        int total = 0;
        if (inventoryWidget != null)
        {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items)
            {
                if (item.getId() == id)
                {
                    total++;
                }
            }
        }
        return total;
    }

    public boolean inventoryContains(int itemID)
    {
        return new InventoryItemQuery(InventoryID.INVENTORY)
                .idEquals(itemID)
                .result(client)
                .size() >= 1;
    }

    public boolean inventoryContains(String itemName)
    {
        WidgetItem inventoryItem = new InventoryWidgetItemQuery()
                .filter(i ->  client.getItemComposition(i.getId())
                        .getName()
                        .toLowerCase()
                        .contains(itemName))
                .result(client)
                .first();

        return inventoryItem != null;
    }

    public boolean inventoryContainsStack(int itemID, int minStackAmount)
    {
        Item item = new InventoryItemQuery(InventoryID.INVENTORY)
                .idEquals(itemID)
                .result(client)
                .first();

        return item != null && item.getQuantity() >= minStackAmount;
    }

    public boolean inventoryItemContainsAmount(int id, int amount, boolean stackable, boolean exactAmount)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        int total = 0;
        if (inventoryWidget != null)
        {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items)
            {
                if (item.getId() == id)
                {
                    if (stackable)
                    {
                        total = item.getQuantity();
                        break;
                    }
                    total++;
                }
            }
        }
        return (!exactAmount || total == amount) && (total >= amount);
    }

    public boolean inventoryItemContainsAmount(Collection<Integer> ids, int amount, boolean stackable, boolean exactAmount)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        int total = 0;
        if (inventoryWidget != null)
        {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items)
            {
                if (ids.contains(item.getId()))
                {
                    if (stackable)
                    {
                        total = item.getQuantity();
                        break;
                    }
                    total++;
                }
            }
        }
        return (!exactAmount || total == amount) && (total >= amount);
    }


    public boolean inventoryContains(int... ids)
    {
        if (client.getItemContainer(InventoryID.INVENTORY) == null)
        {
            return false;
        }
        return new InventoryItemQuery(InventoryID.INVENTORY).idEquals(ids).result(client).size() > 0;
    }

    public boolean inventoryContains(Collection<Integer> itemIds)
    {
        if (client.getItemContainer(InventoryID.INVENTORY) == null)
        {
            return false;
        }
        return getInventoryItems(itemIds).size() > 0;
    }

    public boolean inventoryContainsAllOf(Collection<Integer> itemIds)
    {
        if (client.getItemContainer(InventoryID.INVENTORY) == null)
        {
            return false;
        }
        for (int item : itemIds)
        {
            if (!inventoryContains(item))
            {
                return false;
            }
        }
        return true;
    }

    public boolean inventoryContainsExcept(Collection<Integer> itemIds)
    {
        if (client.getItemContainer(InventoryID.INVENTORY) == null)
        {
            return false;
        }
        Collection<WidgetItem> inventoryItems = getAllInventoryItems();
        List<Integer> depositedItems = new ArrayList<>();

        for (WidgetItem item : inventoryItems)
        {
            if (!itemIds.contains(item.getId()))
            {
                return true;
            }
        }
        return false;
    }

    public void dropItem(WidgetItem item)
    {
        clientThread.invoke(() -> client.invokeMenuAction("", "", item.getId(), MenuAction.ITEM_FIFTH_OPTION.getId(), item.getIndex(), 9764864));
    }

    public void dropItems(Collection<Integer> ids, boolean dropAll, int minDelayBetween, int maxDelayBetween)
    {
        if (isBankOpen() || isDepositBoxOpen())
        {
            log.info("Bank open!");
            return;
        }
        Collection<WidgetItem> inventoryItems = getAllInventoryItems();
        executorService.submit(() ->
        {
            try
            {
                iterating = true;
                for (WidgetItem item : inventoryItems)
                {
                    if (ids.contains(item.getId()))
                    {
                        log.info("dropping item: " + item.getId());
                        sleep(minDelayBetween, maxDelayBetween);
                        dropItem(item);
                        if (!dropAll)
                        {
                            break;
                        }
                    }
                }
                iterating = false;
            }
            catch (Exception e)
            {
                iterating = false;
                e.printStackTrace();
            }
        });
    }

    public void dropAllExcept(Collection<Integer> ids, boolean dropAll, int minDelayBetween, int maxDelayBetween)
    {
        if (isBankOpen() || isDepositBoxOpen())
        {
            log.info("can't drop item, bank is open");
            return;
        }
        Collection<WidgetItem> inventoryItems = getAllInventoryItems();
        executorService.submit(() ->
        {
            try
            {
                iterating = true;
                for (WidgetItem item : inventoryItems)
                {
                    if (ids.contains(item.getId()))
                    {
                        log.info("not dropping item: " + item.getId());
                        continue;
                    }
                    sleep(minDelayBetween, maxDelayBetween);
                    dropItem(item);
                    if (!dropAll)
                    {
                        break;
                    }
                }
                iterating = false;
            }
            catch (Exception e)
            {
                iterating = false;
                e.printStackTrace();
            }
        });
    }

    public void dropInventory(boolean dropAll, int minDelayBetween, int maxDelayBetween)
    {
        if (isBankOpen() || isDepositBoxOpen())
        {
            log.info("can't drop item, bank is open");
            return;
        }
        Collection<Integer> inventoryItems = getAllInventoryItemIDs();
        dropItems(inventoryItems, dropAll, minDelayBetween, maxDelayBetween);
    }

    public void inventoryItemsInteract(Collection<Integer> ids, int opcode, boolean exceptItems, boolean interactAll, int minDelayBetween, int maxDelayBetween)
    {
        Collection<WidgetItem> inventoryItems = getAllInventoryItems();
        log.info(String.valueOf(inventoryItems.size()));
        executorService.submit(() ->
        {
            try
            {
                iterating = true;
                for (WidgetItem item : inventoryItems)
                {
                    if ((!exceptItems && ids.contains(item.getId()) || (exceptItems && !ids.contains(item.getId()))))
                    {
                        log.info("interacting inventory item: {}", item.getId());
                        sleep(minDelayBetween, maxDelayBetween);
                        clientThread.invoke(() -> client.invokeMenuAction("", "", item.getId(), opcode, item.getIndex(), WidgetInfo.INVENTORY.getId()));
                        if (!interactAll)
                        {
                            break;
                        }
                    } else {
                        log.info("failed check");
                    }
                }
                iterating = false;
            }
            catch (Exception e)
            {
                iterating = false;
                e.printStackTrace();
            }
        });
    }

    public boolean runePouchContains(int id)
    {
        Set<Integer> runePouchIds = new HashSet<>();
        if (client.getVar(Varbits.RUNE_POUCH_RUNE1) != 0)
        {
            runePouchIds.add(Runes.getRune(client.getVar(Varbits.RUNE_POUCH_RUNE1)).getItemId());
        }
        if (client.getVar(Varbits.RUNE_POUCH_RUNE2) != 0)
        {
            runePouchIds.add(Runes.getRune(client.getVar(Varbits.RUNE_POUCH_RUNE2)).getItemId());
        }
        if (client.getVar(Varbits.RUNE_POUCH_RUNE3) != 0)
        {
            runePouchIds.add(Runes.getRune(client.getVar(Varbits.RUNE_POUCH_RUNE3)).getItemId());
        }
        for (int runePouchId : runePouchIds)
        {
            if (runePouchId == id)
            {
                return true;
            }
        }
        return false;
    }

    public boolean runePouchContains(Collection<Integer> ids)
    {
        for (int runeId : ids)
        {
            if (!runePouchContains(runeId))
            {
                return false;
            }
        }
        return true;
    }

    public int runePouchQuanitity(int id)
    {
        Map<Integer, Integer> runePouchSlots = new HashMap<>();
        if (client.getVar(Varbits.RUNE_POUCH_RUNE1) != 0)
        {
            runePouchSlots.put(Runes.getRune(client.getVar(Varbits.RUNE_POUCH_RUNE1)).getItemId(), client.getVar(Varbits.RUNE_POUCH_AMOUNT1));
        }
        if (client.getVar(Varbits.RUNE_POUCH_RUNE2) != 0)
        {
            runePouchSlots.put(Runes.getRune(client.getVar(Varbits.RUNE_POUCH_RUNE2)).getItemId(), client.getVar(Varbits.RUNE_POUCH_AMOUNT2));
        }
        if (client.getVar(Varbits.RUNE_POUCH_RUNE3) != 0)
        {
            runePouchSlots.put(Runes.getRune(client.getVar(Varbits.RUNE_POUCH_RUNE3)).getItemId(), client.getVar(Varbits.RUNE_POUCH_AMOUNT3));
        }
        if (runePouchSlots.containsKey(id))
        {
            return runePouchSlots.get(id);
        }
        return 0;
    }

    public boolean isDepositBoxOpen()
    {
        return client.getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER) != null;
    }

    public boolean isBankOpen()
    {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    public void closeBank()
    {
        if (!isBankOpen())
        {
            return;
        }

        Widget bankCloseWidget = client.getWidget(WidgetInfo.BANK_PIN_EXIT_BUTTON);
        if (bankCloseWidget != null)
        {
            clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), 11, 786434));
            return;
        }
    }

    public int getBankMenuOpcode(int bankID)
    {
        return Banks.BANK_CHECK_BOX.contains(bankID) ? MenuAction.GAME_OBJECT_FIRST_OPTION.getId() :
                MenuAction.GAME_OBJECT_SECOND_OPTION.getId();
    }

    public boolean bankContains(String itemName)
    {
        if (isBankOpen())
        {
            ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

            for (Item item : bankItemContainer.getItems())
            {
                if (itemManager.getItemComposition(item.getId()).getName().equalsIgnoreCase(itemName))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean bankContainsAnyOf(int... ids)
    {
        if (isBankOpen())
        {
            ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

            return new BankItemQuery().idEquals(ids).result(client).size() > 0;
        }
        return false;
    }

    public boolean bankContainsAnyOf(Collection<Integer> ids)
    {
        if (isBankOpen())
        {
            ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);
            for (int id : ids)
            {
                if (new BankItemQuery().idEquals(ids).result(client).size() > 0)
                {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public boolean bankContains(String itemName, int minStackAmount)
    {
        if (isBankOpen())
        {
            ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

            for (Item item : bankItemContainer.getItems())
            {
                if (itemManager.getItemComposition(item.getId()).getName().equalsIgnoreCase(itemName) && item.getQuantity() >= minStackAmount)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean bankContains(int itemID, int minStackAmount)
    {
        if (isBankOpen())
        {
            ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);
            final WidgetItem bankItem;
            if (bankItemContainer != null)
            {
                for (Item item : bankItemContainer.getItems())
                {
                    if (item.getId() == itemID)
                    {
                        return item.getQuantity() >= minStackAmount;
                    }
                }
            }
        }
        return false;
    }

    public boolean bankContains2(int itemID, int minStackAmount)
    {
        if (isBankOpen())
        {
            clientThread.invokeLater(() -> {
                ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);
                final WidgetItem bankItem;
                if (client.isClientThread())
                {
                    bankItem = new BankItemQuery().idEquals(itemID).result(client).first();
                }
                else
                {
                    bankItem = new BankItemQuery().idEquals(itemID).result(client).first();
                }

                return bankItem != null && bankItem.getQuantity() >= minStackAmount;
            });
        }
        return false;
    }

    public Widget getBankItemWidget(int id)
    {
        if (!isBankOpen())
        {
            return null;
        }

        WidgetItem bankItem = new BankItemQuery().idEquals(id).result(client).first();
        if (bankItem != null)
        {
            return bankItem.getWidget();
        }
        else
        {
            return null;
        }
    }

    public Widget getBankItemWidgetAnyOf(int... ids)
    {
        if (!isBankOpen())
        {
            return null;
        }

        WidgetItem bankItem = new BankItemQuery().idEquals(ids).result(client).first();
        if (bankItem != null)
        {
            return bankItem.getWidget();
        }
        else
        {
            return null;
        }
    }

    public Widget getBankItemWidgetAnyOf(Collection<Integer> ids)
    {
        if (!isBankOpen() && !isDepositBoxOpen())
        {
            return null;
        }

        WidgetItem bankItem = new BankItemQuery().idEquals(ids).result(client).first();
        if (bankItem != null)
        {
            return bankItem.getWidget();
        }
        else
        {
            return null;
        }
    }

    public void depositAll()
    {
        if (!isBankOpen() && !isDepositBoxOpen())
        {
            return;
        }
        executorService.submit(() ->
        {
            Widget depositInventoryWidget = client.getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);

            if ((depositInventoryWidget != null))
            {
                clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, isDepositBoxOpen() ? 12582916 : 786474));
            }
        });
    }

    public void depositAllExcept(Collection<Integer> ids)
    {
        if (!isBankOpen() && !isDepositBoxOpen())
        {
            return;
        }
        Collection<WidgetItem> inventoryItems = getAllInventoryItems();
        List<Integer> depositedItems = new ArrayList<>();
        executorService.submit(() ->
        {
            try
            {
                iterating = true;
                for (WidgetItem item : inventoryItems)
                {
                    if (!ids.contains(item.getId()) && item.getId() != 6512 && !depositedItems.contains(item.getId())) //6512 is empty widget slot
                    {
                        log.info("depositing item: " + item.getId());
                        depositAllOfItem(item);
                        sleep(80, 200);
                        depositedItems.add(item.getId());
                    }
                }
                iterating = false;
                depositedItems.clear();
            }
            catch (Exception e)
            {
                iterating = false;
                e.printStackTrace();
            }
        });
    }

    public void depositAllOfItem(WidgetItem item)
    {
        assert !client.isClientThread();

        if (!isBankOpen() && !isDepositBoxOpen())
        {
            return;
        }
        boolean depositBox = isDepositBoxOpen();
        clientThread.invoke(() -> client.invokeMenuAction("", "", (depositBox) ? 1 : 8, MenuAction.CC_OP.getId(), item.getIndex(), (depositBox) ? 12582914 : 983043));
    }

    public void depositAllOfItem(int itemID)
    {
        if (!isBankOpen() && !isDepositBoxOpen())
        {
            return;
        }
        depositAllOfItem(getInventoryWidgetItem(itemID));
    }

    public void depositAllOfItems(Collection<Integer> itemIDs)
    {
        if (!isBankOpen() && !isDepositBoxOpen())
        {
            return;
        }
        Collection<WidgetItem> inventoryItems = getAllInventoryItems();
        List<Integer> depositedItems = new ArrayList<>();
        executorService.submit(() ->
        {
            try
            {
                iterating = true;
                for (WidgetItem item : inventoryItems)
                {
                    if (itemIDs.contains(item.getId()) && !depositedItems.contains(item.getId())) //6512 is empty widget slot
                    {
                        log.info("depositing item: " + item.getId());
                        depositAllOfItem(item);
                        sleep(80, 170);
                        depositedItems.add(item.getId());
                    }
                }
                iterating = false;
                depositedItems.clear();
            }
            catch (Exception e)
            {
                iterating = false;
                e.printStackTrace();
            }
        });
    }
    public String getTag(int itemId)
    {
        String tag = configManager.getConfiguration("inventorytags", "item_" + itemId);
        if (tag == null || tag.isEmpty())
        {
            return "";
        }

        return tag;
    }
    public void attackNPCDirect(NPC npc){
        try {
            this.clientThread.invoke(() -> client.invokeMenuAction("", "", npc.getIndex(), client.getSpellSelected() ? MenuAction.SPELL_CAST_ON_NPC.getId() : MenuAction.NPC_SECOND_OPTION.getId(), 0, 0));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void attackNPC(int npcID){
        try {
            this.clientThread.invoke(() -> client.invokeMenuAction("", "", findNearestNpc(npcID).getIndex(), client.getSpellSelected() ? MenuAction.SPELL_CAST_ON_NPC.getId() : MenuAction.NPC_SECOND_OPTION.getId(), 0, 0));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void equipTagGroup(int groupNumber){
        try {
            Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

            if (inventory == null) {
                return;
            }

            for (WidgetItem item : inventory.getWidgetItems()) {
                if (("Group " + groupNumber).equalsIgnoreCase(getTag(item.getId()))) {
                    this.clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
                }
            }
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void specialAttack(){
        try {
            boolean spec_enabled = (client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 1);

            if (spec_enabled) {
                return;
            }
            this.clientThread.invoke(() -> client.invokeMenuAction("Use <col=00ff00>Special Attack</col>", "", 1, MenuAction.CC_OP.getId(), -1, 38862884));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void clickSpell(WidgetInfo spellType) {
        try {
            Widget spell_widget = client.getWidget(spellType);
            if (spell_widget == null) {
                return;
            }

            this.clientThread.invoke(() -> client.invokeMenuAction(spell_widget.getTargetVerb(), spell_widget.getName(), 0, MenuAction.WIDGET_TYPE_2.getId(), spell_widget.getItemId(), spell_widget.getId()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    public void useWallObjectDirect(WallObject targetObject, long sleepDelay, int opcode)
    {
        if(targetObject!=null) {
            clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getId(),opcode,targetObject.getLocalLocation().getSceneX(),targetObject.getLocalLocation().getSceneY()));
        }
    }


    public void useGameObjectDirect(GameObject targetObject, long sleepDelay, int opcode)
    {
        if(targetObject!=null) {
            clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getId(),opcode,targetObject.getSceneMinLocation().getX(),targetObject.getSceneMinLocation().getY()));
        }
    }

    public void interactNPC(int opcode, long sleepDelay, int... id)
    {
        NPC targetObject = findNearestNpc(id);
        if(targetObject!=null){
            clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getIndex(), opcode, 0, 0));
        }
    }
    public Point getRandomNullPoint()
    {
        if(client.getWidget(161,34)!=null){
            Rectangle nullArea = client.getWidget(161,34).getBounds();
            return new Point ((int)nullArea.getX()+getRandomIntBetweenRange(0,nullArea.width), (int)nullArea.getY()+getRandomIntBetweenRange(0,nullArea.height));
        }

        return new Point(client.getCanvasWidth()-getRandomIntBetweenRange(0,2),client.getCanvasHeight()-getRandomIntBetweenRange(0,2));
    }
    public void useGameObject(int id, int opcode, long sleepDelay)
    {
        GameObject targetObject = findNearestGameObject(id);
        if(targetObject!=null){
            clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getId(),opcode,targetObject.getLocalLocation().getSceneX(),targetObject.getLocalLocation().getSceneY()));
        }
    }

    public void useGroundObject(int id, int opcode, long sleepDelay)
    {
        GroundObject targetObject = findNearestGroundObject(id);
        if(targetObject!=null){
            clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getId(),opcode,targetObject.getLocalLocation().getSceneX(),targetObject.getLocalLocation().getSceneY()));
        }
    }

    public void useDecorativeObject(int id, int opcode, long sleepDelay)
    {
        DecorativeObject decorativeObject = findNearestDecorativeObject(id);
        if(decorativeObject!=null){
            clientThread.invoke(() -> client.invokeMenuAction("", "", decorativeObject.getId(),opcode,decorativeObject.getLocalLocation().getSceneX(), decorativeObject.getLocalLocation().getSceneY()));
        }
    }

    public void depositOneOfItem(WidgetItem item)
    {
        if (!isBankOpen() && !isDepositBoxOpen() || item == null)
        {
            return;
        }
        boolean depositBox = isDepositBoxOpen();
        clientThread.invoke(() -> client.invokeMenuAction("", "", (client.getVarbitValue(6590) == 0) ? 2 : 3, MenuAction.CC_OP.getId(), item.getIndex(),(depositBox) ? 12582914 : 983043));
    }

    public void depositOneOfItem(int itemID)
    {
        if (!isBankOpen() && !isDepositBoxOpen())
        {
            return;
        }
        depositOneOfItem(getInventoryWidgetItem(itemID));
    }

    public void withdrawAllItem(Widget bankItemWidget)
    {
        clientThread.invoke(() -> client.invokeMenuAction("", "", 7, MenuAction.CC_OP.getId(), bankItemWidget.getIndex(), 786445));
    }

    public void withdrawAllItem(int bankItemID)
    {
        Widget item = getBankItemWidget(bankItemID);
        if (item != null)
        {
            withdrawAllItem(item);
        }
    }

    public void withdrawItem(Widget bankItemWidget)
    {
        clientThread.invoke(() -> client.invokeMenuAction("", "", (client.getVarbitValue(6590) == 0) ? 1 : 2, MenuAction.CC_OP.getId(), bankItemWidget.getIndex(), 786445));
    }

    public void withdrawItem(int bankItemID)
    {
        Widget item = getBankItemWidget(bankItemID);
        if (item != null)
        {
            withdrawItem(item);
        }
    }

    public void withdrawItemAmount(int bankItemID, int amount)
    {
        clientThread.invokeLater(() -> {
            Widget item = getBankItemWidget(bankItemID);
            if (item != null)
            {
                int identifier;
                switch (amount)
                {
                    case 1:
                        identifier = (client.getVarbitValue(6590) == 0) ? 1 : 2;
                        break;
                    case 5:
                        identifier = 3;
                        break;
                    case 10:
                        identifier = 4;
                        break;
                    default:
                        identifier = 6;
                        break;
                }
                clientThread.invoke(() -> client.invokeMenuAction("", "", identifier, MenuAction.CC_OP.getId(), item.getIndex(), 786445));

                if (identifier == 6)
                {
                    executorService.submit(() -> {
                        sleep(getRandomIntBetweenRange(1000, 1500));
                        typeString(String.valueOf(amount));
                        sleep(getRandomIntBetweenRange(80, 250));
                        pressKey(VK_ENTER);
                    });
                }
            }
        });
    }

    public void sleep(int minSleep, int maxSleep)
    {
        sleep(random(minSleep, maxSleep));
    }

    public void sleep(int toSleep)
    {
        try
        {
            long start = System.currentTimeMillis();
            Thread.sleep(toSleep);

            // Guarantee minimum sleep
            long now;
            while (start + toSleep > (now = System.currentTimeMillis()))
            {
                Thread.sleep(start + toSleep - now);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void sleep(long toSleep)
    {
        try
        {
            long start = System.currentTimeMillis();
            Thread.sleep(toSleep);

            // Guarantee minimum sleep
            long now;
            while (start + toSleep > (now = System.currentTimeMillis()))
            {
                Thread.sleep(start + toSleep - now);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    //Ganom's function, generates a random number allowing for curve and weight
    public long randomDelay(boolean weightedDistribution, int min, int max, int deviation, int target)
    {
        if (weightedDistribution)
        {
            return (long) clamp((-Math.log(Math.abs(random.nextGaussian()))) * deviation + target, min, max);
        }
        else
        {
            return (long) clamp(Math.round(random.nextGaussian() * deviation + target), min, max);
        }
    }

    private double clamp(double val, int min, int max)
    {
        return Math.max(min, Math.min(max, val));
    }

    public static double random(double min, double max) {
        return Math.min(min, max) + random.nextDouble() * Math.abs(max - min);
    }

    public static int random(int min, int max) {
        int n = Math.abs(max - min);
        return Math.min(min, max) + (n == 0 ? 0 : random.nextInt(n));
    }

    static void resumePauseWidget(int widgetId, int arg) {
        final int garbageValue = 1292618906;
        final String className = "ln";
        final String methodName = "hs";

        try
        {

            Class clazz = Class.forName(className);
            Method method = clazz.getDeclaredMethod(methodName, int.class, int.class, int.class);
            method.setAccessible(true);
            method.invoke(null, widgetId, arg, garbageValue);
        }
        catch (Exception ignored)
        {
            return;
        }
    }


    private void setSelectSpell(WidgetInfo info)
    {
        final Widget widget = client.getWidget(info);

        client.setSelectedSpellWidget(widget.getId());
        client.setSelectedSpellChildIndex(-1);
    }

    public void setMenuEntry(MenuEntry menuEntry)
    {
        targetMenu = menuEntry;
    }

    public void setMenuEntry(MenuEntry menuEntry, boolean consume)
    {
        targetMenu = menuEntry;
        consumeClick = consume;
    }

    public void setModifiedMenuEntry(MenuEntry menuEntry, int itemID, int itemIndex, int opCode)
    {
        targetMenu = menuEntry;
        modifiedMenu = true;
        modifiedItemID = itemID;
        modifiedItemIndex = itemIndex;
        modifiedOpCode = opCode;
    }

    @Subscribe
    private void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (event.getOpcode() == MenuAction.CC_OP.getId() && (event.getParam1() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
                event.getParam1() == 11927560 || event.getParam1() == 4522007 || event.getParam1() == 24772686))
        {
            return;
        }
        if (targetMenu != null)
        {
            client.setTempMenuEntry(targetMenu);
            if (modifiedMenu)
            {
                event.setModified();
            }
        }
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (event.getMenuAction() == MenuAction.CC_OP && (event.getWidgetId() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
                event.getWidgetId() == 11927560 || event.getWidgetId() == 4522007 || event.getWidgetId() == 24772686))
        {
            //Either logging out or world-hopping which is handled by 3rd party plugins so let them have priority
            log.info("Received world-hop/login related click. Giving them priority");
            targetMenu = null;
            return;
        }
        if (targetMenu != null)
        {
            if (consumeClick)
            {
                event.consume();
                log.info("Consuming a click and not sending anything else");
                consumeClick = false;
                targetMenu = null;
                return;
            }
            targetMenu = null;
        }
    }

    public void menuAction(MenuOptionClicked menuOptionClicked, String option, String target, int identifier, MenuAction menuAction, int param0, int param1)
    {
        menuOptionClicked.setMenuOption(option);
        menuOptionClicked.setMenuTarget(target);
        menuOptionClicked.setId(identifier);
        menuOptionClicked.setMenuAction(menuAction);
        menuOptionClicked.setActionParam(param0);
        menuOptionClicked.setWidgetId(param1);
        log.info(menuOptionClicked.toString());
    }

    public boolean pointIntersectIgnoringPlane(WorldPoint a, WorldPoint b){
        return a.getX() == b.getX() && a.getY() == b.getY();
    }

    public boolean areaIntersectIgnoringPlane(WorldArea a, WorldArea b){
        a = new WorldArea(new WorldPoint(a.getX(),a.getY(),0),a.getWidth(),a.getHeight());
        b = new WorldArea(new WorldPoint(b.getX(),b.getY(),0),b.getWidth(),b.getHeight());
        return a.intersectsWith(b);
    }


    //GROUND ITEM STUFF
    @Subscribe
    private void onItemSpawned(ItemSpawned itemSpawned)
    {
        spawnedItems.put(itemSpawned.getItem(), itemSpawned.getTile());
    }

    @Subscribe
    private void onItemDespawned(ItemDespawned itemDespawned)
    {
        spawnedItems.remove(itemDespawned.getItem());
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged gameStateChanged){
        spawnedItems.clear();
    }

}