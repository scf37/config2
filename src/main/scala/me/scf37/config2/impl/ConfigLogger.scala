package me.scf37.config2.impl

private trait ConfigLogger {
  def info(msg: String): Unit
  def debug(msg: String): Unit
}