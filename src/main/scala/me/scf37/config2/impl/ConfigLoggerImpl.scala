package me.scf37.config2.impl

private class ConfigLoggerImpl(logger: String => Unit, verbose: Boolean) extends ConfigLogger {
  def info(msg: String):Unit = logger(msg)
  def debug(msg: String):Unit = if (verbose) logger(msg)
}