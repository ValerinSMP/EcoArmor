package com.willfp.ecoarmor.commands

import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.ecoarmor.plugin
import com.willfp.libreforge.refreshHolders
import com.willfp.libreforge.toDispatcher
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.Damageable

object CommandDamage : Subcommand(
    plugin,
    "damage",
    "ecoarmor.command.give",
    true
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        val player = sender as? Player ?: return
        val mode = args.firstOrNull()?.lowercase() ?: "break"

        val armor = player.inventory.armorContents.filterNotNull()
        val held = player.inventory.itemInMainHand

        val targets = if (armor.isNotEmpty()) armor else listOf(held)

        if (mode == "break") {
            for (item in targets) {
                val meta = item.itemMeta as? Damageable ?: continue
                val maxDur = if (meta.hasMaxDamage()) meta.maxDamage else item.type.maxDurability.toInt()
                if (maxDur <= 0) continue
                meta.damage = maxDur - 1
                item.itemMeta = meta
            }
            player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 0.9f)
            player.toDispatcher().refreshHolders()
            player.sendMessage("§cArmadura dejada a 1 HP de durabilidad (Estado Roto).")
        } else {
            val amount = mode.toIntOrNull() ?: 100
            for (item in targets) {
                val meta = item.itemMeta as? Damageable ?: continue
                val maxDur = if (meta.hasMaxDamage()) meta.maxDamage else item.type.maxDurability.toInt()
                if (maxDur <= 0) continue

                val projectedDamage = meta.damage + amount
                if (projectedDamage >= maxDur - 1) {
                    meta.damage = maxDur - 1
                    item.itemMeta = meta
                    player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 0.9f)
                } else {
                    meta.damage = projectedDamage
                    item.itemMeta = meta
                }
            }
            player.toDispatcher().refreshHolders()
            player.sendMessage("§aAplicado $amount de daño de durabilidad a la armadura!")
        }
    }
}
