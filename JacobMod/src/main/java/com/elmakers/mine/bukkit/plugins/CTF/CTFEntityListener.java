package com.elmakers.mine.bukkit.plugins.CTF;


import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


public class CTFEntityListener extends EntityListener{
    private CTFPlugin ctf;
    public CTFEntityListener(CTFPlugin ctf)
    {
        this.ctf = ctf;
    }
    @Override
    public void onEntityDamage(EntityDamageEvent e)
    {
        if(e instanceof EntityDamageByEntityEvent)
        {
            EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e;
            if(ee.getEntity() instanceof Player)
            {
                Player defender = (Player) ee.getEntity();
                Entity attacker = ee.getDamager();
                if(attacker instanceof Player)
                {
                    if(ctf.getTeam((Player)attacker) == ctf.getTeam((Player)defender))
                    {
                        ((Player)attacker).sendMessage("- "+ChatColor.YELLOW+"You can't hurt "+((Player)defender).getDisplayName());
                        ee.setCancelled(true);
                    }
                    else if(ctf.getTeam((Player)attacker) == -1 || ctf.getTeam((Player)defender) == -1)
                    {
                        ee.setCancelled(true);
                        ((Player)attacker).sendMessage("- "+ChatColor.YELLOW+"You can't hurt "+((Player)defender).getDisplayName());
                    }
                }
            }
        }
    }
    @Override
    public void onEntityDeath(EntityDeathEvent e)
    {
        Entity ent = e.getEntity();
        if(ent instanceof Player)
        {
            Player player = (Player) ent;
            ctf.dropFlag(player);
        }
    }
}
