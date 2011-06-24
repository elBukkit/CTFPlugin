package com.elmakers.mine.bukkit.plugins.CTF;


import net.minecraft.server.InventoryPlayer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitScheduler;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


public class CTFPlayerListener extends PlayerListener{
    @Override
    public void onPlayerPickupItem(PlayerPickupItemEvent event)
    {
        Integer teamId = ctf.playerTeams.get(event.getPlayer());
        if (teamId != null && teamId != -1)
        {
            event.setCancelled(true);
        }
    }
    private CTFPlugin ctf;
    public CTFPlayerListener(CTFPlugin ctf)
    {
        this.ctf = ctf;
    }
    @Override
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        Player player = e.getPlayer();
        ctf.playerTeams.put(player, -1);
        if(ctf.spawnsDone == false)
            ctf.initSpawns();
        player.sendMessage("- "+ChatColor.GREEN+"Use /ctf for CTF help");
    }
    @Override
    public void onPlayerRespawn(PlayerRespawnEvent e)
    {
        Player player = e.getPlayer();
        int currentTeam = ctf.playerTeams.get(player);
        if (currentTeam != -1)
        {
            player.sendMessage("- "+ChatColor.YELLOW+"Respawning in 10 seconds. Use /quit to leave CTF");
            BukkitScheduler sched = ctf.getServer().getScheduler();
            sched.scheduleSyncDelayedTask(ctf, new RespawnAction(player, ctf), 200);
        }
    }
    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e)
    {
        Player player = e.getPlayer();
        String[] split = e.getMessage().split(" ");
        String cmd = split[0];
        boolean isOp = ctf.isOp(player);
        if(cmd.equals("/newmap") && isOp)
        {
            ctf.newMap();
            e.setCancelled(true);
        }
        else if(cmd.equals("/silver"))
        {
            if(ctf.getTeam(player) == -1)
            {
                ctf.joinTeam(player, 0);
                ctf.dropFlag(player);
            }
            e.setCancelled(true);
        }
        else if(cmd.equals("/gold"))
        {
            if(ctf.getTeam(player) == -1)
            {
                ctf.joinTeam(player, 1);
                ctf.dropFlag(player);
            }
            e.setCancelled(true);
        }
        else if(cmd.equals("/quit"))
        {
            if(ctf.getTeam(player) != -1)
            {
                ctf.joinTeam(player, -1);
                ctf.dropFlag(player);
            }
            e.setCancelled(true);
        }
        else if(cmd.equals("/respawn"))
        {
            if(ctf.getTeam(player) != -1)
            {
                player.damage(100);
                ctf.dropFlag(player);
            }
            e.setCancelled(true);
        }
        else if(cmd.equals("/class"))
        {
            if(ctf.getTeam(player) != -1)
            {
                if (split.length > 1)
                {
                    changeClass(player, split[1]);
                }
                else
                {
                    player.sendMessage("- "+ChatColor.BLUE+"Use: /class <class>");
                    String classList = "";
                    for (CTFClass ctfClass : ctf.ctfClasses.values())
                    {
                        if (classList.length() > 0)
                        {
                            classList += ", ";
                        }
                        classList += ctfClass.getName();
                    }
                    player.sendMessage("- "+ChatColor.WHITE+classList);
                }
            }
            e.setCancelled(true);
        }
        else if(cmd.equals("/newgame") && isOp)
        {
            ctf.newGame();
            e.setCancelled(true);
        }
        else if(cmd.equals("/status"))
        {
            int silverPlayers = 0;
            int goldPlayers = 0;
            String hasOurFlag = null;
            String hasOtherFlag = null;
            for(Player p : ctf.getServer().getOnlinePlayers())
            {
                if(ctf.getTeam(p) == 0)
                    silverPlayers++;
                else if(ctf.getTeam(p) == 1)
                    goldPlayers++;
                if(p == ctf.sFlagCarrier)
                {
                    if(ctf.getTeam(player) == 0)
                        hasOurFlag = p.getDisplayName();
                    else
                        hasOtherFlag = p.getDisplayName();
                }
                else if(p == ctf.gFlagCarrier)
                {
                    if(ctf.getTeam(player) == 0)
                        hasOtherFlag = p.getDisplayName();
                    else
                        hasOurFlag = p.getDisplayName();
                }
            }
            if(hasOurFlag == null)
                hasOurFlag = "No one";
            if(hasOtherFlag == null)
                hasOtherFlag = "No one";
            player.sendMessage("- "+ChatColor.BLUE+silverPlayers+" players on silver:");
            String silverMsg = "";
            for(Player p : ctf.getServer().getOnlinePlayers())
            {
                if(ctf.getTeam(p) == 0)
                    silverMsg += p.getDisplayName()+" | ";
            }
            if(!silverMsg.equals(""))
            {
                String[] lines = CTFUtil.wrapText(silverMsg, 60);
                for(String l : lines)
                    player.sendMessage("- "+l);
            }

            player.sendMessage("- "+ChatColor.BLUE+goldPlayers+" players on gold:");
            String goldMsg = "";
            for(Player p : ctf.getServer().getOnlinePlayers())
            {
                if(ctf.getTeam(p) == 1)
                    goldMsg += p.getDisplayName()+" | ";
            }
            if(!goldMsg.equals(""))
            {
                String[] lines = CTFUtil.wrapText(goldMsg, 60);
                for(String l : lines)
                    player.sendMessage("- "+l);
            }
            player.sendMessage("- "+hasOtherFlag+ChatColor.YELLOW+" has the other flag");
            player.sendMessage("- "+hasOurFlag+ChatColor.YELLOW+" has your flag");
            player.sendMessage("- "+ChatColor.RED+"Silver: "+ctf.sCaptures+" | Gold: "+ctf.gCaptures);
            e.setCancelled(true);
        }
        else if(cmd.equals("/tc"))
        {
            for(Player p : ctf.getServer().getOnlinePlayers())
            {
                if(ctf.getTeam(p) == ctf.getTeam(player))
                {
                    p.sendMessage("(TEAM) "+player.getDisplayName()+ChatColor.WHITE+": "+e.getMessage().replace("/tc ", ""));
                }
            }
            e.setCancelled(true);
        }
        else if(cmd.equals("/ctf"))
        {
            player.sendMessage("- "+ChatColor.YELLOW+" Try to capture the other flag and return it to your side");
            player.sendMessage("- "+ChatColor.YELLOW+" Click the flag to take it and your capture point to return");
            player.sendMessage("- "+ChatColor.YELLOW+" If you get killed while holding the flag, it will be returned to the enemy base.");
            player.sendMessage("- "+ChatColor.YELLOW+" A new game will start once one team reaches 5 captures.");
            player.sendMessage("- "+ChatColor.YELLOW+" Say /silver or /gold to join a team and start playing.");
            player.sendMessage("- "+ChatColor.YELLOW+" Say /class to change classes");
            e.setCancelled(true);
        }
    }
    
    public double getDistance(Location source, int x, int y, int z)
    {
        return Math.sqrt
        (
            Math.pow(source.getX() - x, 2) 
        +   Math.pow(source.getY() - y, 2)
        +   Math.pow(source.getZ() - z, 2)
        );
    }
    
    public void changeClass(Player player, String className)
    {
        Location playerLocation = player.getLocation();
        double distance = 0;
        if(ctf.getTeam(player) == 0)
        {
            distance = getDistance(playerLocation, ctf.sSpawnX, playerLocation.getBlockY(), ctf.sSpawnZ);
        } 
        else
        {
            distance = getDistance(playerLocation, ctf.gSpawnX, playerLocation.getBlockY(), ctf.gSpawnZ);
        }
        
        if (distance > 16)
        {
            player.sendMessage("- "+ChatColor.RED+" Too far from spawn");
            return;
        }
            
        CTFClass ctfClass = null;
        for (CTFClass candidate : ctf.ctfClasses.values())
        {
            if (className.equals(candidate.getName()))
            {
                ctfClass = candidate;
                break;
            }
        }
        if (ctfClass == null)
        {
            player.sendMessage("- "+ChatColor.RED+" Unknown class " + className);
            return;
        }
        
        ctf.playerClasses.put(player, ctfClass);
        ctfClass.resupply(player);
        player.sendMessage("- "+ChatColor.GREEN+" Changed to " + ctfClass.getName());
        ctfClass.printSpells(player);
    }
    
    @Override
    public void onPlayerChat(PlayerChatEvent e)
    {
        String msg = e.getMessage();
        Player p = e.getPlayer();
        System.err.println(p.getName()+": "+msg);
        ctf.getServer().broadcastMessage(p.getDisplayName()+ChatColor.WHITE+": "+msg);
        e.setCancelled(true);
    }
    @Override
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if(e.getAction() == Action.LEFT_CLICK_BLOCK)
        {            
            Player player = e.getPlayer();
            Block block = e.getClickedBlock();
            if (ctf.getTeam(player) == -1) return;
            
            if (block.getState() instanceof Sign)
            {
                Sign sign = (Sign)block.getState();
                String signText = sign.getLine(0);
                CTFClass ctfClass = ctf.ctfClasses.get(signText);
                if (ctfClass != null)
                {
                    changeClass(player, signText);
                    e.setCancelled(true);
                    return;
                }
            }
            if(block.getX() == ctf.sZoneX && block.getY() == ctf.sZoneY && block.getZ() == ctf.sZoneZ)
            {
                if(ctf.getTeam(player) == 0 && ctf.gFlagCarrier == player && ctf.gFlagTaken)
                {
                    ctf.gFlagCarrier = null;
                    ctf.gFlagTaken = false;
                    ctf.getServer().broadcastMessage("- "+player.getDisplayName()+" "+ChatColor.YELLOW+"returned the gold flag!");
                    ctf.placeGoldFlag();
                    ctf.sCaptures++;
                    ctf.showScore();
                    if(ctf.sCaptures >= ctf.maxCaptures)
                    {
                        ctf.getServer().broadcastMessage("- "+ChatColor.LIGHT_PURPLE+"The silver team wins!");
                        ctf.newGame();
                    }
                }
                e.getPlayer().getWorld().getBlockAt(block.getX(), block.getY(), block.getZ()).setTypeId(49);
            }
            else if(block.getX() == ctf.gZoneX && block.getY() == ctf.gZoneY && block.getZ() == ctf.gZoneZ)
            {
                if(ctf.getTeam(player) == 1 && ctf.sFlagCarrier == player && ctf.sFlagTaken)
                {
                    ctf.sFlagCarrier = player;
                    ctf.sFlagTaken = false;
                    ctf.getServer().broadcastMessage("- "+player.getDisplayName()+" "+ChatColor.YELLOW+"returned the silver flag!");
                    ctf.placeSilverFlag();
                    ctf.gCaptures++;
                    ctf.showScore();
                    if(ctf.gCaptures >= ctf.maxCaptures)
                    {
                        ctf.getServer().broadcastMessage("- "+ChatColor.LIGHT_PURPLE+"The gold team wins!");
                        ctf.newGame();
                    }
                }
                e.getPlayer().getWorld().getBlockAt(block.getX(), block.getY(), block.getZ()).setTypeId(49);
            }
            else if(!ctf.gFlagTaken && block.getX() == ctf.gFlagX && block.getY() == ctf.gFlagY && block.getZ() == ctf.gFlagZ)
            {
                if(ctf.getTeam(player) == 0)
                {
                    ctf.gFlagCarrier = player;
                    ctf.gFlagTaken = true;
                    ctf.getServer().broadcastMessage("- "+player.getDisplayName()+" "+ChatColor.YELLOW+"took the gold flag!");
                    e.getPlayer().getWorld().getBlockAt(block.getX(), block.getY(), block.getZ()).setTypeId(0);
                }
                else
                {
                    e.setCancelled(true);
                    ctf.placeGoldFlag();
                }
            }
            else if(!ctf.sFlagTaken && block.getX() == ctf.sFlagX && block.getY() == ctf.sFlagY && block.getZ() == ctf.sFlagZ)
            {
                if(ctf.getTeam(player) == 1)
                {
                    ctf.sFlagCarrier = player;
                    ctf.sFlagTaken = true;
                    ctf.getServer().broadcastMessage("- "+player.getDisplayName()+" "+ChatColor.YELLOW+"took the silver flag!");
                    e.getPlayer().getWorld().getBlockAt(block.getX(), block.getY(), block.getZ()).setTypeId(0);
                }
                else
                {
                    e.setCancelled(true);
                    ctf.placeSilverFlag();
                }
            }
        }
    }
    @Override
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Player p = event.getPlayer();
        int x = p.getLocation().getBlockX();
        int z = p.getLocation().getBlockZ();
        if (ctf.isInArena(x, z))
        {
            Integer team = ctf.playerTeams.get(event.getPlayer());
            if (team == null || team == -1) return;
            
            InventoryPlayer inventory = ((CraftInventoryPlayer)p.getInventory()).getInventory();
            if (ctf.isInBase(x, z))
            {
                if (inventory.itemInHandIndex != 9)
                {
                    inventory.itemInHandIndex = 9;
                }
            }
           else
           {
               if (inventory.itemInHandIndex != 0)
               {
                   inventory.itemInHandIndex = 0;
               }
           }
        }
    }
}
