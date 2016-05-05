package me.scf37.config2

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.control.NoStackTrace
import scala.util.control.NonFatal

/**
  * Flags - support for nicely handling string configuration map obtained from [[Config]].
  *
  * Features:
  * - get typed values out of config map
  * - error handling, combining all errors to single validation error list
  * - [[usageString]] to show user available parameters
  * - [[flags]] to show loaded config on startup and/or advanced reporting
  *
  * @param source map of string configuration
  */
class Flags(source: Map[String, String]) {
  private[this] val flags_ = mutable.Buffer.empty[Flag]
  private[this] val errors_ = mutable.Buffer.empty[String]
  private[this] val resolvedSource = mutable.Map.empty[String, String]

  /** errors occurred extracting [[flags]] from [[source]] */
  def errors: Seq[String] = synchronized(errors_.clone())

  /** list of known flags along with values */
  def flags: Seq[Flag] = synchronized(flags_.clone())

  def apply[T](name: String, description: String, defaultValue: Option[String] = None)
      (mapper: String => T)(implicit tag: ClassTag[T]): T = synchronized {

    try {
      source.get(name).orElse(defaultValue) match {
        case Some(value) =>
          resolvedSource += name -> value
          val mappedValue = mapper(value)
          flags_ += Flag(name, description, defaultValue, Success(value), Success(mappedValue))
          mappedValue
        case None =>
          errors_ += s"Parameter '$name' is required"
          val e = new IllegalArgumentException(s"Parameter '$name' is required") with NoStackTrace
          flags_ += Flag(name, description, defaultValue, Failure(e), Failure(e))
          tag.newArray(1)(0)
      }

    } catch {
      case NonFatal(e) =>
        errors_ += s"Unable to read '$name': " + e.getMessage
        flags_ += Flag(name, description, defaultValue, Failure(e), Failure(e))
        tag.newArray(1)(0)
    }
  }

  def usageString = synchronized( "usage:\n" +
    flags_.map(f => s"  ${f.name}={${f.default.getOrElse("")}}  ${f.description}\n").mkString)

}

case class Flag private[config2] (
  name: String,
  description: String,
  default: Option[String],
  value: Try[String],
  mappedValue: Try[Any]
)