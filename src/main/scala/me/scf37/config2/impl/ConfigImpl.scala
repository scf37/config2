package me.scf37.config2.impl

import java.io.InputStream
import java.util

import me.scf37.config2.Config

import scala.collection.JavaConversions

private[config2] object ConfigImpl {
  private[this] val defaultLogger = new ConfigLoggerImpl(println, false)
  def apply(url: String): Config = new ConfigImpl(Nil, defaultLogger).overrideWith(url)
  def apply(props: Map[String, String]): Config = new ConfigImpl(Nil, defaultLogger).overrideWith(props)
  def apply(props: util.Map[_, _]): Config = new ConfigImpl(Nil, defaultLogger).overrideWith(props)
}

private[config2] case class ConfigSource(
  url: Option[String] = None,
  props: Option[Map[String, String]] = None
)

private[config2] class ConfigImpl(
  sources: List[ConfigSource],
  logger: ConfigLogger
) extends Config {

  /**
    * Override previous location(s) with this one. Loaded properties will also be merged, taking this location with higher precedence.
    */
  override def overrideWith(url: String): Config =
    new ConfigImpl(ConfigSource(url = Some(url)) :: sources, logger)

  /**
    * Override previous location(s) with this set of properties. <p>
    * Typical values here are System.getProperties(), System.env() or Properties obtained from other sources
    */
  override def overrideWith(props: util.Map[_, _]): Config =
    overrideWith(JavaConversions.mapAsScalaMap(props)
      .toList
      .filter(e => e._1 != null && e._2 != null)
      .map(e => (e._1.toString, e._2.toString))
      .toMap)

  /**
    * Override previous location(s) with this set of properties. <p>
    * Typical values here are System.getProperties(), System.env() or Properties obtained from other sources
    */
  override def overrideWith(props: Map[String, String]): Config =
    new ConfigImpl(ConfigSource(props = Some(props)) :: sources, logger)

  /**
    * Configure output and verbosity of Config2 logging. <p>
    * By default, information on all loaded files is printed to stdout. Verbose mode also prints information
    * on tried, but not loaded files. <p>
    * Generally this method cannot use Slf4j because Config2 can be used to configure logging itself!
    */
  override def log(logger: (String) => Unit, verbose: Boolean): Config =
    new ConfigImpl(sources, new ConfigLoggerImpl(logger, verbose))

  /**
    * Read text file with specified encoding. Merge is not supported, so file with most precedence wins.
    * @file relative file name
    */
  override def text(file: String, encoding: String): Option[String] = {
    var result: Option[String] = None

    logger.debug(s"reading text '$file'")

    sources.find {
      case ConfigSource(Some(url), _) =>
        val reader = new UrlReader(url, logger)
        result = reader.readText(file, encoding)
        result.isDefined
      case _ => false
    }

    if (!result.isDefined) {
      logger.info(s"Unable to locate '$file'!")
    }

    result
  }

  /**
    * Read text file with specified encoding. Merge is not supported, so file with most precedence wins.
    * Caller is responsible for closing the stream.
    * @param file relative file name
    */
  override def file(file: String): Option[InputStream] = {
    var result: Option[InputStream] = None

    logger.debug(s"reading binary '$file'")

    sources.find {
      case ConfigSource(Some(url), _) =>
        val reader = new UrlReader(url, logger)
        result = reader.readFile(file)
        result.isDefined
      case _ => false
    }

    if (result.isEmpty) {
      logger.info(s"Unable to locate '$file'!")
    }
    result
  }

  /**
    * URL of resolved configuration file or None if there is no such file
    * @param file relative file name
    */
  override def url(file: String): Option[String] = {
    var result: Option[String] = None

    sources.find {
      case ConfigSource(Some(url), _) =>
        val reader = new UrlReader(url, logger)
        result = reader.path(file)
        result.isDefined
      case _ => false
    }

    result
  }

  /**
    * Read properties from standard Java Properties file. Multiple paths are tried as configured before.
    * Resulting Properties will be merged from all available sources
    * @file relative file name
    */
  override def properties(file: String): Option[Map[String, String]] = {
    var result = Map.empty[String, String]
    var hasResult = false

    logger.debug(s"reading properties '$file'")

    sources.reverse.foreach {
      case ConfigSource(Some(url), _) =>
        val reader = new UrlReader(url, logger)
        val props = reader.readProperties(file)
        props.foreach { p =>
          result ++= p
          hasResult = true
        }
      case ConfigSource(_, Some(props)) =>
        result ++= props
        hasResult = true
    }

    if (hasResult) {
      Some(result)
    } else {
      logger.info(s"Unable to locate '$file'!")
      None
    }
  }

  override def properties(): Map[String, String] = {
    var result = Map.empty[String, String]

    sources.reverse.foreach {
      case ConfigSource(_, Some(props)) =>
        result ++= props
      case _ =>

    }

    result

  }

}
