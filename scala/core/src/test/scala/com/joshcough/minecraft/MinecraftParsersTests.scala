package com.joshcough.minecraft

import org.scalacheck.Properties
import org.scalacheck.Prop._
import org.bukkit.entity.EntityType
import org.bukkit.Material
import Material._

object MinecraftParsersTests extends Properties("MinecraftParserTests") with TestHelpers {

  import MinecraftParsers._

  for(m <- Material.values) test(m.toString) {
    List(material(m.name), material(m.toString.toLowerCase), material(m.getId.toString)).forall(_.get == m)
  }

  for(e <- EntityType.values) test(e.toString) {
    List(entity(List(e.name)), entity(e.toString.toLowerCase)).forall(_.get == e)
  }

  test("(material or eof)(gold_ore)") {
    (material or eof)("gold_ore").get.left.get ?= GOLD_ORE
  }

  test("(material or eof)(Nil)") {
    (material or eof)(Nil).get.isRight
  }

  test("(material or eof)(werersd)") {
    val res = (material or eof)("werersd").fold(id)((p,r) =>
      "parser worked with bogus material type: werersd, but shouldnt have."
    )
    res ?= "invalid material-type: werersd or expected eof, but got: werersd"
  }

  test("((material or eof) <~ eof)(dirt 7)") {
    val res = ((material or eof) <~ eof)("dirt 7").fold(id)((_,_) =>
      "parser worked, but shouldnt have."
    )
    res ?= "expected eof, but got: 7"
  }
}