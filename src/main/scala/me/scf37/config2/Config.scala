package me.scf37.config2

import java.io.InputStream

import me.scf37.config2.impl.ConfigImpl

/**
 * Main entry point of Config2 library
 */
object Config {
  /**
   * Define location to read configuration files from. <p>
   * Supported protocols are classpath:, file: and no protocol (which means file:)
   */
  def from(url: String): Config = ConfigImpl(url)

  def from(props: java.util.Map[_, _]): Config = ConfigImpl(props)

  def from(props: Map[String, String]): Config = ConfigImpl(props)

}

/**
 * Configured Config (pun intended) instance. <p>
 * Can be used to load Properties and files, using paths and settings provided by ConfigBuilder
 */
trait Config {
  /**
    * Override previous location(s) with this one. Loaded properties will also be merged, taking this location with higher precedence.
    */
  def overrideWith(url: String): Config
  /**
    * Override previous location(s) with this set of properties. <p>
    * Typical values here are System.getProperties(), System.env() or Properties obtained from other sources
    */
  def overrideWith(props: java.util.Map[_, _]): Config

  /**
    * Override previous location(s) with this set of properties.
    */
  def overrideWith(props: Map[String, String]): Config

  /**
    * Configure output and verbosity of Config2 logging. <p>
    * By default, information on all loaded files is printed to stdout. Verbose mode also prints information
    * on tried, but not loaded files. <p>
    * Generally this method cannot use Slf4j because Config2 can be used to configure logging itself!
    */
  def log(logger: String => Unit = println, verbose: Boolean = false): Config

  /**
   * Read properties from standard Java Properties file. Multiple paths are tried as configured before.
   * Resulting Properties will be merged from all available sources
   * @file relative file name
   */
  def properties(file: String): Option[Map[String, String]]
  
  /**
   * Read text file with specified encoding. Merge is not supported, so file with most precedence wins.
   * @file relative file name
   */
  def text(file: String, encoding: String = "UTF-8"): Option[String]
  
  /**
   * Read text file with specified encoding. Merge is not supported, so file with most precedence wins.
   * Caller is responsible for closing the stream.
   * @param file relative file name
   */
  def file(file: String): Option[InputStream]

  /**
    * URL of resolved configuration file or None if there is no such file
    * @param file relative file name
    */
  def url(file: String): Option[String]
}