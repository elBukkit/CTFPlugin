package com.elmakers.mine.bukkit.plugins.CTF;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


public class CTFPlugin extends JavaPlugin{
    private CTFPlayerListener playerListener;
    private CTFEntityListener entityListener;
    
    private Server server;
    private World world = null;
    private int mapZ = 0;
    public boolean spawnsDone = false;
    public int mapX = 0;
    
    public int sSpawnX;
    public int sSpawnY;
    public int sSpawnZ;
    public int gSpawnX;
    public int gSpawnY;
    public int gSpawnZ;

    public int sFlagX;
    public int sFlagY;
    public int sFlagZ;
    public int gFlagX;
    public int gFlagY;
    public int gFlagZ;

    public boolean sFlagTaken;
    public boolean gFlagTaken;
    public int maxCaptures = 5;
    public int sCaptures;
    public int gCaptures;

    public int sZoneX;
    public int sZoneY;
    public int sZoneZ;
    public int gZoneX;
    public int gZoneY;
    public int gZoneZ;
    public HashMap<Player, Integer> playerTeams = new HashMap<Player, Integer>(20);
    public HashMap<Player, CTFClass> playerClasses = new HashMap<Player, CTFClass>();
    public HashMap<String, CTFClass> ctfClasses = new HashMap<String, CTFClass>();
    public CTFClass defaultClass = null;
    public Player sFlagCarrier;
    public Player gFlagCarrier;
    
    public CTFPlugin()
    {
        playerListener = new CTFPlayerListener(this);
        entityListener = new CTFEntityListener(this);
        
        CTFClass scoutClass = new CTFClass("scout");
        scoutClass.addSpell(Material.WEB, "Short-range teleport");
        scoutClass.addSpell(Material.LEATHER_BOOTS, "Long jump / flight");
        scoutClass.addSpell(Material.ARROW, "Rapid-fire arrows");
        addClass(scoutClass);
        defaultClass = scoutClass;
        
        CTFClass medicClass = new CTFClass("medic");
        medicClass.addSpell(Material.BREAD, "Heal yourself or others");
        medicClass.addSpell(Material.RAILS, "Push creatures or items");
        medicClass.addSpell(Material.FISHING_ROD, "Pull creatures or items");
        addClass(medicClass);
        
        CTFClass alchemistClass = new CTFClass("alchemist");
        alchemistClass.addSpell(Material.CLAY_BALL, "Create a blob of material");
        alchemistClass.addSpell(Material.WATCH, "Undo previous spells");
        alchemistClass.addSpell(Material.WOOD_SPADE, "Modify existing materials");
        alchemistClass.addMaterial(Material.SAND);
        alchemistClass.addMaterial(Material.SOUL_SAND);
        alchemistClass.addMaterial(Material.STATIONARY_WATER);
        addClass(alchemistClass);
        
        CTFClass heavyClass = new CTFClass("heavy");
        heavyClass.addSpell(Material.NETHERRACK, "Launch a fireball");
        heavyClass.addSpell(Material.TNT, "Toss a grenade");
        heavyClass.addSpell(Material.BOW, "Fire a splitter arrow");
        addClass(heavyClass);
        
        CTFClass elementalistClass = new CTFClass("elementalist");
        elementalistClass.addSpell(Material.SNOW_BALL, "Spread cold and hurt creatures");
        elementalistClass.addSpell(Material.COOKED_FISH, "Strike a bolt of lightning");
        elementalistClass.addSpell(Material.FLINT_AND_STEEL, "Start a fire");
        addClass(elementalistClass);
        
        CTFClass engineerClass = new CTFClass("engineer");
        engineerClass.addSpell(Material.GOLD_SPADE, "Fill a volume with two clicks");
        engineerClass.addSpell(Material.GOLD_AXE, "Raise the ground into a pillar");
        engineerClass.addSpell(Material.GOLD_HOE, "Extend the ground into a bridge");
        engineerClass.addMaterial(Material.COBBLESTONE);
        engineerClass.addMaterial(Material.GLOWSTONE);
        engineerClass.addMaterial(Material.GLASS);
        addClass(engineerClass);
    }
    
    public void addClass(CTFClass newClass)
    {
        ctfClasses.put(newClass.getName(), newClass);
    }
    
    public void placeSilverFlag()
    {
        world.getBlockAt(sFlagX, sFlagY, sFlagZ).setTypeId(42);
    }
    public void placeGoldFlag()
    {
        world.getBlockAt(gFlagX, gFlagY, gFlagZ).setTypeId(41);
    }
    public void dropFlag(Player player)
    {
        if(gFlagCarrier == player)
        {
            getServer().broadcastMessage("- "+player.getDisplayName()+" "+ChatColor.YELLOW+"dropped the flag!");
            placeGoldFlag();
            gFlagTaken = false;
            gFlagCarrier = null;
        }
        else if(sFlagCarrier == player)
        {
            getServer().broadcastMessage("- "+player.getDisplayName()+" "+ChatColor.YELLOW+"dropped the flag!");
            placeSilverFlag();
            sFlagTaken = false;
            sFlagCarrier = null;
        }
    }
    private void prepSpawn()
    {
        int i1 = 12;
        for (int i2 = -i1; i2 <= i1; ++i2) {
            for (int i3 = -i1; i3 <= i1; ++i3) {
                world.loadChunk(i2, i3);
            }
        }
    }
    private void drawSquare(int x, int y, int z, int s)
    {
        for(int dx = x - s; dx <= x + s; dx++)
        {
            for(int dz = z - s; dz <= z + s; dz++)
            {
                world.getBlockAt(dx, y, dz).setTypeId(7);
            }
        }
    }

    public void initSpawns()
    {
        prepSpawn();
        sSpawnX = mapX + 4;
        gSpawnX = mapX + 256 - 4;
        sSpawnZ = mapZ + 128;
        gSpawnZ = mapZ + 128;

        sFlagX = mapX + 20;
        gFlagX = mapX + 256 - 20;
        sFlagZ = mapZ + 128 + 8;
        gFlagZ = mapZ + 128 + 8;

        sZoneX = mapX + 20;
        gZoneX = mapX + 256 - 20;
        sZoneZ = mapZ + 128 - 8;
        gZoneZ = mapZ + 128 - 8;
        
        for(int y = 126; y >= 0; y--)
        {
            int b = world.getBlockAt(sSpawnX, y, sSpawnZ).getTypeId();
            if(b != 0 && b != 18 && b != 17)
            {
                sSpawnY = y + 2;
                break;
            }
        }
        for(int y = 126; y >= 0; y--)
        {
            int b = world.getBlockAt(gSpawnX, y, gSpawnZ).getTypeId();
            if(b != 0 && b != 18 && b != 17)
            {
                gSpawnY = y + 2;
                break;
            }
        }

        for(int y = 126; y >= 0; y--)
        {
            int b = world.getBlockAt(sFlagX, y, sFlagZ).getTypeId();
            if(b != 0 && b != 42 && b != 7 && b != 18 && b != 17)
            {
                sFlagY = y + 3;
                break;
            }
        }
        for(int y = 126; y >= 0; y--)
        {
            int b = world.getBlockAt(gFlagX, y, gFlagZ).getTypeId();
            if(b != 0 && b != 41 && b != 7 && b != 18 && b != 17)
            {
                gFlagY = y + 3;
                break;
            }
        }

        for(int y = 126; y >= 0; y--)
        {
            int b = world.getBlockAt(sZoneX, y, sZoneZ).getTypeId();
            if(b != 0 && b != 49 && b != 18 && b != 17)
            {
                sZoneY = y + 1;
                break;
            }
        }
        for(int y = 126; y >= 0; y--)
        {
            int b = world.getBlockAt(gZoneX, y, gZoneZ).getTypeId();
            if(b != 0 && b != 49 && b != 18 && b != 17)
            {
                gZoneY = y + 1;
                break;
            }
        }
        placeSilverFlag();
        placeGoldFlag();
        world.getBlockAt(sZoneX, sZoneY, sZoneZ).setType(Material.OBSIDIAN);
        world.getBlockAt(gZoneX, gZoneY, gZoneZ).setType(Material.OBSIDIAN);
        spawnsDone = true;
    }

    public void newMap()
    {
        server.broadcastMessage("Generating map...");

        initSpawns();

        drawSquare(sSpawnX, sSpawnY - 2, sSpawnZ, 1);
        drawSquare(gSpawnX, gSpawnY - 2, gSpawnZ, 1);

        drawSquare(sZoneX, sZoneY, sZoneZ, 1);
        world.getBlockAt(sZoneX, sZoneY, sZoneZ).setTypeId(0);
        drawSquare(sZoneX, sZoneY - 1, sZoneZ, 1);
        drawSquare(gZoneX, gZoneY, gZoneZ, 1);
        world.getBlockAt(gZoneX, gZoneY, gZoneZ).setTypeId(0);
        drawSquare(gZoneX, gZoneY - 1, gZoneZ, 1);

        drawSquare(sFlagX, sFlagY - 2, sFlagZ, 1);
        world.getBlockAt(sFlagX, sFlagY, sFlagZ).setTypeId(42);
        world.getBlockAt(sFlagX, sFlagY - 1, sFlagZ).setTypeId(7);

        drawSquare(gFlagX, gFlagY - 2, gFlagZ, 1);
        world.getBlockAt(gFlagX, gFlagY, gFlagZ).setTypeId(41);
        world.getBlockAt(gFlagX, gFlagY - 1, gFlagZ).setTypeId(7);

        for(int z = mapZ; z < mapZ + 256; z++)
        {
            for(int y = 126; y >= 0; y--)
            {
                int b = world.getBlockAt(mapX + 127, y, z).getTypeId();
                if(b != 0)
                {
                    world.getBlockAt(mapX + 127, y, z).setTypeId(42);
                    break;
                }
            }
        }

        for(int z = mapZ; z < mapZ + 256; z++)
        {
            for(int y = 126; y >= 0; y--)
            {
                int b = world.getBlockAt(mapX + 128, y, z).getTypeId();
                if(b != 0)
                {
                    world.getBlockAt(mapX + 128, y, z).setTypeId(41);
                    break;
                }
            }
        }

        for(int z = mapZ; z < mapZ + 256; z++)
        {
            for(int y = 128; y >= 0; y--)
            {
                world.getBlockAt(mapX, y, z).setTypeId(7);
                world.getBlockAt(mapX + 256, y, z).setTypeId(7);
            }
        }

        for(int x = mapX; x < mapX + 256; x++)
        {
            for(int y = 128; y >= 0; y--)
            {
                world.getBlockAt(x, y, mapZ).setTypeId(7);
                world.getBlockAt(x, y, mapZ + 256).setTypeId(7);
            }
        }

        server.broadcastMessage("Say /silver or /gold to join a team");
        System.err.println("Done preparing map");
    }
    public void showScore()
    {
        server.broadcastMessage("- "+ChatColor.GREEN+"Current score: Silver has "+sCaptures+" captures; gold has "+gCaptures+" captures.");
    }
    public void newGame()
    {
        server.broadcastMessage("- "+ChatColor.YELLOW+"Starting new game!");
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        sFlagCarrier = null;
        sFlagTaken = false;
        gFlagCarrier = null;
        gFlagTaken = false;
        for(Player p : server.getOnlinePlayers())
        {
            joinTeam(p, -1);
            p.setHealth(20);
        }
        sCaptures = 0;
        gCaptures = 0;
        placeSilverFlag();
        placeGoldFlag();
    }
    public void joinTeam(Player player, int id)
    {
        boolean isNew = playerTeams.get(player) != id;
        playerTeams.put(player, id);
        CTFClass playerClass = playerClasses.get(player);
        if (playerClass == null)
        {
            playerClass = defaultClass;
            playerClasses.put(player, defaultClass);
        }
        playerClass.resupply(player);
        String name = null;
        if(id == 0)
        {
            name = "silver";
            player.teleport(new Location(world, sSpawnX, sSpawnY, sSpawnZ, 0, 0));
            player.setDisplayName(ChatColor.GRAY+ChatColor.stripColor(player.getDisplayName()));
        }
        else if(id == 1)
        {
            name = "gold";
            player.teleport(new Location(world, gSpawnX, gSpawnY, gSpawnZ, 0, 0));
            player.setDisplayName(ChatColor.GOLD+ChatColor.stripColor(player.getDisplayName()));
        }
        else if(id == -1)
        {
            player.teleport(new Location(world, world.getSpawnLocation().getX(), world.getSpawnLocation().getY() + 2, world.getSpawnLocation().getZ()));
            player.setDisplayName(ChatColor.stripColor(player.getDisplayName()));
        }
        if(name != null && isNew)
            server.broadcastMessage("- "+player.getDisplayName()+ChatColor.YELLOW+" joined the "+name+" team");
    }

    public void onEnable() {
        server = getServer();
        world = server.getWorlds().get(0);
        server.getPluginManager().registerEvent(Type.PLAYER_JOIN, playerListener, Priority.High, this);
        server.getPluginManager().registerEvent(Type.PLAYER_RESPAWN, playerListener, Priority.High, this);
        server.getPluginManager().registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.High, this);
        server.getPluginManager().registerEvent(Type.PLAYER_CHAT, playerListener, Priority.High, this);
        server.getPluginManager().registerEvent(Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.High, this);
        server.getPluginManager().registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.High, this);
        server.getPluginManager().registerEvent(Type.ENTITY_DEATH, entityListener, Priority.High, this);
        server.getPluginManager().registerEvent(Type.PLAYER_PICKUP_ITEM, playerListener, Priority.High, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        Logger.getLogger("Minecraft").info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
    }

    public int getTeam(Player p)
    {
        return playerTeams.get(p);
    }
    public boolean isOp(Player p)
    {
        return p.isOp();
    }

    public void onDisable() {

    }
}
