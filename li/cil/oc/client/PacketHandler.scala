package li.cil.oc.client

import cpw.mods.fml.common.network.Player
import li.cil.oc.common.PacketType
import li.cil.oc.common.tileentity.Computer
import li.cil.oc.common.tileentity.Rotatable
import li.cil.oc.common.tileentity.Screen
import li.cil.oc.common.{PacketHandler => CommonPacketHandler}
import li.cil.oc.server.component.Redstone
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.ForgeDirection

class PacketHandler extends CommonPacketHandler {
  protected override def world(player: Player, dimension: Int) = {
    val world = player.asInstanceOf[EntityPlayer].worldObj
    if (world.provider.dimensionId == dimension) Some(world)
    else None
  }

  override def dispatch(p: PacketParser) =
    p.packetType match {
      case PacketType.ScreenResolutionChange => onScreenResolutionChange(p)
      case PacketType.ScreenSet => onScreenSet(p)
      case PacketType.ScreenFill => onScreenFill(p)
      case PacketType.ScreenCopy => onScreenCopy(p)
      case PacketType.ScreenBufferResponse => onScreenBufferResponse(p)
      case PacketType.ComputerStateResponse => onComputerStateResponse(p)
      case PacketType.RotatableStateResponse => onRotatableStateResponse(p)
      case PacketType.RedstoneStateResponse => onRedstoneStateResponse(p)
      case _ => // Invalid packet.
    }

  def onScreenResolutionChange(p: PacketParser) =
    p.readTileEntity[Screen]() match {
      case Some(t) => {
        val w = p.readInt()
        val h = p.readInt()
        t.instance.resolution = (w, h)
      }
      case _ => // Invalid packet.
    }

  def onScreenSet(p: PacketParser) =
    p.readTileEntity[Screen]() match {
      case Some(t) => {
        val col = p.readInt()
        val row = p.readInt()
        val s = p.readUTF()
        t.instance.set(col, row, s)
      }
      case _ => // Invalid packet.
    }

  def onScreenFill(p: PacketParser) =
    p.readTileEntity[Screen]() match {
      case Some(t) => {
        val col = p.readInt()
        val row = p.readInt()
        val w = p.readInt()
        val h = p.readInt()
        val c = p.readChar()
        t.instance.fill(col, row, w, h, c)
      }
      case _ => // Invalid packet.
    }

  def onScreenCopy(p: PacketParser) =
    p.readTileEntity[Screen]() match {
      case Some(t) => {
        val col = p.readInt()
        val row = p.readInt()
        val w = p.readInt()
        val h = p.readInt()
        val tx = p.readInt()
        val ty = p.readInt()
        t.instance.copy(col, row, w, h, tx, ty)
      }
      case _ => // Invalid packet.
    }

  def onScreenBufferResponse(p: PacketParser) =
    p.readTileEntity[Screen]() match {
      case Some(t) =>
        val w = p.readInt()
        val h = p.readInt()
        t.instance.resolution = (w, h)
        p.readUTF.split('\n').zipWithIndex.foreach {
          case (line, i) => t.instance.set(0, i, line)
        }
      case _ => // Invalid packet.
    }

  def onComputerStateResponse(p: PacketParser) =
    p.readTileEntity[Computer]() match {
      case Some(t) => {
        t.isOn = p.readBoolean()
      }
      case _ => // Invalid packet.
    }

  def onRotatableStateResponse(p: PacketParser) =
    p.readTileEntity[Rotatable]() match {
      case Some(t) =>
        t.pitch = p.readDirection()
        t.yaw = p.readDirection()
      case _ => // Invalid packet.
    }

  def onRedstoneStateResponse(p: PacketParser) =
    p.readTileEntity[TileEntity with Redstone]() match {
      case Some(t) =>
        t.isOutputEnabled = p.readBoolean()
        for (d <- ForgeDirection.VALID_DIRECTIONS) {
          t.output(d, p.readByte())
        }
      case _ => // Invalid packet.
    }
}