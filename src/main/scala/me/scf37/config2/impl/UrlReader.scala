package me.scf37.config2.impl

import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import java.util.Properties

import scala.collection.JavaConversions
import scala.collection.mutable.ArrayBuffer
import scala.util.Failure
import scala.util.Success
import scala.util.Try

private[impl] class UrlReader(url0: String, logger: ConfigLogger) {
  private val baseUrl = normalizeUrl(url0)
  
  def readFile(file: String): Option[InputStream] = {
    val url = makeUrl(file)
    
    logger.debug(s"Trying $url")
    
    val result = readUrl(url)
    
    result match {
      case Success(r) =>
        logger.info(s"Loaded $url")
        return Some(r)
      case Failure(e: FileNotFoundException) =>
        logger.debug(s"Not found $url")
        None
      case Failure(e) =>
        logger.info(s"Unable to load $url: $e")
        None
    }
  }

  def readProperties(file: String): Option[Map[String, String]] = {
    val url = makeUrl(file)
    
    logger.debug(s"Trying $url")
    
    readUrl(url).map { is =>
       val prop = new Properties()
       prop.load(is)
       is.close()
       prop
    } match {
      case Success(r) =>
        logger.info(s"Loaded $url")
        Some(JavaConversions.mapAsScalaMap(r)
          .toList
          .filter(e => e._1 != null && e._2 != null)
          .map(e => (e._1.toString, e._2.toString))
          .toMap)
      case Failure(e: FileNotFoundException) =>
        logger.debug(s"Not found $url")
        None
      case Failure(e) =>
        logger.info(s"Unable to load $url: $e")
        None
    }
  }

  def readText(file: String, encoding: String): Option[String] = {
    val url = makeUrl(file)
      
    logger.debug(s"Trying $url")
    
    val result = readUrl(url).flatMap(readFully(_, encoding))
    
    result match {
      case Success(r) =>
        logger.info(s"Loaded $url")
        return Some(r)
      case Failure(e: FileNotFoundException) =>
        logger.debug(s"Not found $url")
        None
      case Failure(e) =>
        logger.info(s"Unable to load $url: $e")
        None
    }
       
  }

  def path(file: String): Option[String] = {
    val url = makeUrl(file)
    readUrl(url) match {
      case Success(is) =>
        Try(is.close())
        Some(url)
      case Failure(e) => None
    }
  }
  
  private def readFully(is: InputStream, encoding: String): Try[String] = {
    
    val out = Try {
      
      val buf = Array.ofDim[Byte](8192)
      val result = ArrayBuffer[Byte]()
      
      var len: Int = 0
      do {
        
        len = is.read(buf)
        if (len > 0)
          result ++= buf.view(0, len)
      } while (len > 0)
        
      new String(result.toArray, encoding)
    }

    is.close()
    
    out
  }
  
  private def readUrl(url: String): Try[InputStream] = {
      if (url.startsWith("classpath:")) {
        classpathUrl(url.substring("classpath:".length()))
      } else {
          genericUrl(new URL(url))
      }
  }
  
  private def genericUrl(u: URL ): Try[InputStream] = {
    Try {
      val connection = u.openConnection()
      
      connection.setUseCaches(false)
      connection.getInputStream()
    }
  }

  private def classpathUrl(location: String): Try[InputStream] = {
    val normalizedLoc = if (location.startsWith("/")) {
      location.substring(1);
    } else location
    
    val is = Thread.currentThread().getContextClassLoader().getResourceAsStream(normalizedLoc);
    
    if (is == null) {
      Failure(new FileNotFoundException(s"Resource not found on classpath: $location"))
    } else {
      Success(is)
    }
    
  }
  
  private def makeUrl(file: String): String = {
    if (file.startsWith("/"))
      baseUrl + file
    else
      baseUrl + '/' + file
  }
  
  private def normalizeUrl(url0: String) = {
    var url = url0
    
    if (url.endsWith("/")) {
      url = url.dropRight(1)
    }
    
    if (url.indexOf(':') < 0 || 
        url.indexOf(':') == 1) { //windows absolute path
      url = "file:" + url;
    }
    
    url = url.replaceAll("\\\\", "/");
    
    url
  }
}