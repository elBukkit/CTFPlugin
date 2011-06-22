package com.elmakers.mine.bukkit.plugins.CTF;

import org.bukkit.entity.Player;

public class RespawnAction implements Runnable
{
    protected Player player;
    protected CTFPlugin ctf;
    
    public RespawnAction(Player player, CTFPlugin plugin)
    {
        this.player = player;
        this.ctf = plugin;
    }

    public void run()
    {
        int teamId = this.ctf.playerTeams.get(player);
        if (teamId != -1)
        {
            this.ctf.joinTeam(player, teamId);
        }
    }

}
