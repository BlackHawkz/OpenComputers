package li.cil.oc.integration.ic2

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import ic2.api.item.ElectricItem
import ic2.api.item.IElectricItem
import ic2.api.item.ISpecialElectricItem
import ic2.core.item.tool.ItemToolWrench
import li.cil.oc.api.event.RobotUsedToolEvent
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

object EventHandlerIndustrialCraft2 {
  @SubscribeEvent
  def onRobotApplyDamageRate(e: RobotUsedToolEvent.ApplyDamageRate) {
    val optManagerBefore = e.toolBeforeUse.getItem match {
      case item: ISpecialElectricItem => Option(item.getManager(e.toolBeforeUse))
      case item: IElectricItem => Option(ElectricItem.manager)
      case _ => None
    }
    val optManagerAfter = e.toolAfterUse.getItem match {
      case item: ISpecialElectricItem => Option(item.getManager(e.toolAfterUse))
      case item: IElectricItem => Option(ElectricItem.manager)
      case _ => None
    }
    (optManagerBefore, optManagerAfter) match {
      case (Some(managerBefore), Some(managerAfter)) =>
        val damage = managerBefore.getCharge(e.toolBeforeUse) - managerAfter.getCharge(e.toolAfterUse)
        if (damage > 0) {
          val actualDamage = damage * e.getDamageRate
          val repairedDamage =
            if (e.robot.player.getRNG.nextDouble() > 0.5)
              damage - math.floor(actualDamage).toInt
            else
              damage - math.ceil(actualDamage).toInt
          managerAfter.charge(e.toolAfterUse, repairedDamage, Int.MaxValue, true, false)
        }
      case _ =>
    }
  }

  def getDurability(stack: ItemStack): Double = {
    stack.getItem match {
      case item: ISpecialElectricItem => item.getManager(stack).getCharge(stack) / item.getMaxCharge(stack)
      case item: IElectricItem => ElectricItem.manager.getCharge(stack) / item.getMaxCharge(stack)
      case _ => Double.NaN
    }
  }

  def useWrench(player: EntityPlayer, x: Int, y: Int, z: Int, changeDurability: Boolean): Boolean = {
    player.getCurrentEquippedItem.getItem match {
      case wrench: ItemToolWrench =>
        if (changeDurability) {
          wrench.damage(player.getHeldItem, 1, player)
          true
        }
        else wrench.canTakeDamage(player.getHeldItem, 1)
      case _ => false
    }
  }
}
