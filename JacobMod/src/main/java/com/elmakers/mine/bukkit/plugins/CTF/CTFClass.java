package com.elmakers.mine.bukkit.plugins.CTF;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CTFClass
{
    public class CTFSpell
    {
        public Material material;
        public String description;
        
        public CTFSpell(Material mat, String desc)
        {
            material = mat;
            description = desc;
        }
    };
    
    protected String name;
    protected List<CTFSpell> spells = new ArrayList<CTFSpell>();
    protected List<Material> materials = new ArrayList<Material>();
    
    public CTFClass(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void addSpell(Material icon, String description)
    {
        spells.add(new CTFSpell(icon, description));
    }

    public void addMaterial(Material mat)
    {
        materials.add(mat);
    }
    
    public void resupply(Player p)
    {
        Inventory inv = p.getInventory();
        inv.clear();
        ItemStack wandItem = new ItemStack(Material.STICK, 1);
        inv.setItem(0, wandItem);
        int index = 1;
        for (CTFSpell spell : spells)
        {
            inv.setItem(index++, new ItemStack(spell.material, 1));
        }
        index = 8;
        for (Material mat : materials)
        {
            inv.setItem(index--, new ItemStack(mat, 1));
        }
    }
    
    public void printSpells(Player player)
    {
        for (CTFSpell spell : spells)
        {
            String matName = spell.material.name().toLowerCase();
            matName.replace("_", " ");
            player.sendMessage("- "+ChatColor.GREEN+matName+ChatColor.WHITE+" : " + spell.description);
        }
    }
}
