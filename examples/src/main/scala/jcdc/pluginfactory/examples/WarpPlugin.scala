package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{CommandsPlugin, SingleClassDBPlugin}
import org.bukkit.{Location, World}
import org.bukkit.entity.Player
import scala.collection.JavaConversions._

class WarpPlugin extends CommandsPlugin with SingleClassDBPlugin[Warp]{

  val dbClass = classOf[Warp]

  val commands = List(
    Command("warps", "List all warps.",
      noArgs(p => warpsFor(p).foreach { w => p.sendMessage(w.toString) })),
    Command("warp", "Warp to the given warp location.",
      args(warp){ case p ~ w => p.teleport(w.location(p.world)) }),
    Command("delete-warp", "Delete a warp location.",
      args(warp){ case p ~ w => db.delete(w); p ! ("deleted warp: " + w.name) }),
    Command("delete-all", "Delete all warps in the database.",
      opOnly(noArgs(p => db.foreach { w => p ! ("deleting: " + w); db.delete(w) }))),
    Command("set-warp", "Create a new warp location.", args(warp||anyString){
      case p ~ Left(w)  =>
        // TODO: can i use an update here?
        // TODO: well, i need to make a case class out of warp
        // then i need to put the annotations on its constructor params... have to test that.
        db.delete(w)
        db.insert(createWarp(w.name, p))
        p ! ("updated warp: " + w.name)
      case p ~ Right(name) => db.insert(createWarp(name, p)); p ! "created warp: " + name
    })
  )

  def warp = token("warp"){ (p, s) => warpsFor(p).find(_.name == s) }

  def createWarp(n: String, p: Player): Warp = {
    val w = new Warp(); w.name = n; w.player = p.name; w.x = p.x; w.y = p.y; w.z = p.z; w
  }

  def warpsFor(p: Player) = db.query.where.ieq("player", p.getName).findList()
}

@javax.persistence.Entity
class Warp {
  @javax.persistence.Id
  @javax.persistence.GeneratedValue
  var id = 0
  var name: String = ""
  var player = ""
  var x = 0d; var y = 0d; var z = 0d
  def location(world: World) = new Location(world, x, y, z)
  override def toString = player + "." + name + (x, y, z)
}
