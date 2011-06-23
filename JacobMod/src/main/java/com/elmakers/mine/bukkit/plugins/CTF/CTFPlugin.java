package com.elmakers.mine.bukkit.plugins.CTF;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
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
        
        CTFClass necroClass = new CTFClass("necromancer");
        necroClass.addSpell(Material.BONE, "Disintegrate your target");
        necroClass.addSpell(Material.PORTAL, "Create connected portals");
        necroClass.addSpell(Material.PUMPKIN, "Summon a monster to attack your enemy");
        addClass(necroClass);
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

        sFlagX = mapX + 12;
        gFlagX = mapX + 256 - 12;
        sFlagZ = mapZ + 128;
        gFlagZ = mapZ + 128;

        sZoneX = mapX + 12;
        gZoneX = mapX + 256 - 12;
        sZoneZ = mapZ + 128;
        gZoneZ = mapZ + 128;
        
        // hard-coding to sea level for now!
        int seaLevel = 63;
        
        sSpawnY = seaLevel + 2;
        gSpawnY = seaLevel + 2;
        sFlagY = seaLevel + 4;
        gFlagY = seaLevel + 4;
        sZoneY = seaLevel + 3;
        gZoneY = seaLevel + 3;
        
        for (int x = -3; x < 12; x++)
        {
            for (int z = -4; z < 4; z++)
            {
                for (int y = -2; y < 6; y++)
                {
                    // gold base clearing and floor
                    Block block = world.getBlockAt(gSpawnX - x, gSpawnY + y, gSpawnZ + z);
                    block.setType(y == -2 ? Material.BEDROCK : Material.AIR);
                    
                    // silver base clearing and floor
                    block = world.getBlockAt(sSpawnX + x, sSpawnY + y, sSpawnZ + z);
                    block.setType(y == -2 ? Material.BEDROCK : Material.AIR);
                }
            }
        }

        drawSquare(sFlagX, sFlagY - 2, sFlagZ, 1);
        drawSquare(gFlagX, gFlagY - 2, gFlagZ, 1);
        drawSquare(sFlagX, sFlagY - 3, sFlagZ, 2);
        drawSquare(gFlagX, gFlagY - 3, gFlagZ, 2);
        world.getBlockAt(sZoneX, sZoneY, sZoneZ).setType(Material.OBSIDIAN);
        world.getBlockAt(gZoneX, gZoneY, gZoneZ).setType(Material.OBSIDIAN);
        placeSilverFlag();
        placeGoldFlag();
        
        // Set up class signs
        int sSignX = mapX + 2;
        int gSignX = mapX + 256 - 2;
        int sSignZ = mapZ + 128;
        int gSignZ = mapZ + 128;
        int sSignY = sSpawnY;
        int gSignY = gSpawnY;
        int classCount = ctfClasses.values().size();
        sSignZ = sSignZ - classCount / 2;
        gSignZ = gSignZ - classCount / 2;
        for (CTFClass ctfClass : ctfClasses.values())
        {
            String className = ctfClass.getName();
            
            // Silver base
            Block signBlock = world.getBlockAt(sSignX, sSignY, sSignZ);
            Block wall = world.getBlockAt(sSignX - 1, sSignY, sSignZ++);
            wall.setType(Material.BEDROCK);
            wall = wall.getFace(BlockFace.DOWN);
            wall.setType(Material.BEDROCK);
            
            signBlock.setType(Material.WALL_SIGN);
            signBlock.setData((byte)5);
            if (signBlock.getState() instanceof Sign)
            {
                Sign sign = (Sign)signBlock.getState();
                sign.setLine(0, className);
            }
            
           // Gold base
           signBlock = world.getBlockAt(gSignX, gSignY, gSignZ);
           wall = world.getBlockAt(gSignX + 1, gSignY, gSignZ++);
           wall.setType(Material.BEDROCK);
           wall = wall.getFace(BlockFace.DOWN);
           wall.setType(Material.BEDROCK);
           
           signBlock.setType(Material.WALL_SIGN);
           signBlock.setData((byte)4);  
           if (signBlock.getState() instanceof Sign)
           {
               Sign sign = (Sign)signBlock.getState();
               sign.setLine(0, className);
           }
        }
 
        spawnsDone = true;
    }

    public void newMap()
    {
        server.broadcastMessage("Generating map...");

        initSpawns();
        
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
        if (id != -1)
        {
            playerClass.resupply(player);
        }
        else
        {
            player.getInventory().clear();
        }
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
