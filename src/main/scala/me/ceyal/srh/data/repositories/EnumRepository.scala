package me.ceyal.srh.data.repositories

import me.ceyal.srh.util.EnumValueBase
import play.api.libs.json._

import java.io.{File, FileInputStream, IOException}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, FileVisitor, Files, Path}

private[repositories] class EnumRepository[T <: EnumValueBase](basePath: File) {
  if (!basePath.exists()) {
    basePath.mkdirs()
  }

  def loadAll(implicit r: Reads[T]): Unit = {
    Files.walkFileTree(basePath.toPath, new FileVisitor[Path] {
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = FileVisitResult.CONTINUE

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        println("Visiting " + file)
        val is = new FileInputStream(file.toFile)
        try {
          Json.parse(is) match {
            case JsArray(value) =>
              println("Found array " + value)
              value.foreach(r.reads(_)  match {
                case jserr @ JsError(err) =>
                  println("Error while loading file " + file + " : " + err)
                  jserr
                case other => other
              })
            case underlying: JsObject =>
              r.reads(underlying) match {
                case jserr @ JsError(err) =>
                  println("Error while loading file " + file + " : " + err)
                  jserr
                case other => other
              }
            case _ => ()
          }
        } catch {
          case io: IOException =>
            io.printStackTrace()
            JsError(io.getMessage)
          case other => other.printStackTrace() ; throw other
        } finally {
          is.close()
        }
        FileVisitResult.CONTINUE
      }

      override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = FileVisitResult.CONTINUE

      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = FileVisitResult.CONTINUE
    })
  }
}