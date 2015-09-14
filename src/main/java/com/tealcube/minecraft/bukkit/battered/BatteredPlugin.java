/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.tealcube.minecraft.bukkit.battered;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class BatteredPlugin extends FacePlugin implements Listener {

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll((Listener) this);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        boolean damaged = false;

        for (int i = 0; i <= 8; i++) {
            if (inventory.getItem(i) == null){
                continue;
            }
            ItemStack itemStack = inventory.getItem(i).clone();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            if (!itemStack.getType().name().contains("SWORD") && !itemStack.getType().name().contains("AXE") &&
                    !itemStack.getType().name().contains("SPADE") && !itemStack.getType().name().contains("HOE")) {
                continue;
            }
            itemStack.getItemMeta().spigot().setUnbreakable(false);
            itemStack.setDurability((short) (itemStack.getDurability() + 5 + itemStack.getType().getMaxDurability() / 4));
            itemStack.getItemMeta().spigot().setUnbreakable(true);
            damaged = true;
            if (itemStack.getType().getMaxDurability() > 1 &&
                    itemStack.getDurability() >= itemStack.getType().getMaxDurability()) {
                player.sendMessage(ChatColor.RED + "Oh no! One of your tools has dropped below zero durability and " +
                        "was destroyed!");
                inventory.clear(i);
                continue;
            }
            if (itemStack.getType().getMaxDurability() > 1 && itemStack.getDurability() > itemStack.getType()
                    .getMaxDurability() * 0.65) {
                player.sendMessage(ChatColor.GOLD + "Watch out! One of your tools is low on durability and is in " +
                        "danger of breaking!");
            }
            inventory.setItem(i, itemStack);
        }

        for (int i = 36; i <= 39; i++) {
            if (inventory.getItem(i) == null){
                continue;
            }
            ItemStack itemStack = inventory.getItem(i).clone();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            if (!itemStack.getType().name().contains("BOOTS") && !itemStack.getType().name().contains("LEGGINGS") &&
                    !itemStack.getType().name().contains("CHESTPLATE") && !itemStack.getType().name().contains("HELMET")) {
                continue;
            }
            itemStack.getItemMeta().spigot().setUnbreakable(false);
            itemStack.setDurability((short)(itemStack.getDurability() + 5 + itemStack.getType().getMaxDurability()/5));
            itemStack.getItemMeta().spigot().setUnbreakable(true);
            damaged = true;
            if (itemStack.getType().getMaxDurability() > 1 &&
                    itemStack.getDurability() >= itemStack.getType().getMaxDurability()) {
                player.sendMessage(ChatColor.RED + "Oh no! A piece of your armor has dropped below zero durability " +
                        "and was destroyed!");
                inventory.clear(i);
                continue;
            }
            if (itemStack.getType().getMaxDurability() > 1 && itemStack.getDurability() > itemStack.getType()
                    .getMaxDurability() * 0.70) {
                player.sendMessage(ChatColor.GOLD + "Watch out! A piece of your armor is low on durability and is " +
                        "in danger of breaking!");
            }
            inventory.setItem(i, itemStack);
        }
        if (damaged) {
            player.sendMessage(ChatColor.YELLOW + "Your equipment lost some durability from dying!");
        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onHit(PlayerItemDamageEvent event) {
        event.setCancelled(true);
        event.setDamage(0);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathEventLowest(PlayerDeathEvent event) {
        event.setKeepInventory(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final World world = event.getEntity().getWorld();
        player.updateInventory();

        Inventory inventory = player.getInventory();
        for(int i = 0; i < inventory.getContents().length; i++) {
            ItemStack itemStack = inventory.getContents()[i];
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            if (i >= 0 && i <= 8) {
                if (!itemStack.getType().name().contains("SWORD") && !itemStack.getType().name().contains("AXE") &&
                        !itemStack.getType().name().contains("SPADE") && !itemStack.getType().name().contains("HOE")) {
                    int dropAmount = Math.max(1 , (int) (itemStack.getAmount() * 0.75));
                    int keepAmount = itemStack.getAmount() - dropAmount;
                    if (keepAmount > 0) {
                        itemStack.setAmount(keepAmount);
                        inventory.setItem(i, itemStack);
                    } else {
                        inventory.clear(i);
                    }
                    itemStack.setAmount(dropAmount);
                    world.dropItemNaturally(event.getEntity().getLocation(), itemStack);
                }
            } else {
                world.dropItemNaturally(event.getEntity().getLocation(), itemStack);
                inventory.clear(i);
            }
        }
    }

}
