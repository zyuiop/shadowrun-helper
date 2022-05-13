package me.ceyal.srh.data.repositories

import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.gui2.dialogs.{FileDialogBuilder, MessageDialogBuilder, MessageDialogButton}
import me.ceyal.srh.data.entities.GameEntity
import me.ceyal.srh.ui.reactive.ReactiveValue
import play.api.libs.json.{Format, JsError, JsResult, JsSuccess, Json, OFormat, Reads, Writes}

import java.io.{File, FileInputStream, FileOutputStream, IOException}

private[repositories] class BaseRepository[T](entityName: String, basePath: File)(implicit format: Format[T]) {
  val default: Map[String, T] = Map()

  if (!basePath.exists()) {
    basePath.mkdirs()
    default.foreach(pair => saveToFile(pair._2, new File(basePath, pair._1)))
  }

  private[repositories] def jsonFromFile[A](file: File)(implicit r: Reads[A]): JsResult[A] = {
    val is = new FileInputStream(file)
    try {
      val jsV = Json.parse(is)
      r.reads(jsV)
    } catch {
      case io: IOException => JsError(io.getMessage)
    } finally {
      is.close()
    }
  }

  private[repositories] def jsonToFile[A](value: A, file: File)(implicit w: Writes[A]): Unit = {
    val os = new FileOutputStream(file)

    try {
      val jsV = w.writes(value)
      os.write(Json.prettyPrint(jsV).getBytes("utf-8"))
    } finally os.close()
  }

  /**
   * Prompts the user for a file path and loads it
   * @param gui
   * @return
   */
  def load(gui: WindowBasedTextGUI): Option[(JsResult[T], File)] = {
    val f = new FileDialogBuilder()
      .setActionLabel("Charger")
      .setDescription(s"Choisir $entityName à charger")
      .setTitle(s"Charger $entityName")
      .setSelectedFile(basePath)
      .build().showDialog(gui)

    if (f != null) {
      Some(readFromFile(f) -> f)
    } else {
      None
    }
  }

  /**
   * Prompts the user for a file path, loads it, and return a reactive value for it that will update the file when
   * modified
   */
  def loadWithAutosave(gui: WindowBasedTextGUI): Option[ReactiveValue[T]] = load(gui) match {
    case Some((JsSuccess(t: T, _), filePath)) => Some(wrapAutosave(t, filePath))
    case _ => None
  }

  /**
   * Wrap a value so that it is automatically saved to a given file when it changes
   */
  def wrapAutosave(value: T, file: File): ReactiveValue[T] =
    ReactiveValue.of(value).addListener((nv, _) => saveToFile(nv, file))

  /**
   * Add a listener to a reactive value to autosave it when it changes
   */
  def autosave(value: ReactiveValue[T], file: File): ReactiveValue[T] =
    value.addListener((nv, _) => saveToFile(nv, file))

  /**
   * Prompts the user for a file-path to save an entity to, saves it and returns the entity wrapped in a reactive value
   * that will save the entity when it changes
   */
  def saveAndAutosave(e: T, gui: WindowBasedTextGUI, originalFile: Option[File] = None): Option[ReactiveValue[T]] =
    save(e, gui, originalFile).map(f => wrapAutosave(e, f))

  /**
   * Prompts the user for a file-path to save an entity to and saves it
   */
  def save(e: T, gui: WindowBasedTextGUI, originalFile: Option[File] = None): Option[File] = {
    val f = new FileDialogBuilder()
      .setActionLabel("Sauvegarder")
      .setDescription("Choisir un fichier pour sauvegarder")
      .setTitle(s"Sauvegarder $entityName")
      .setSelectedFile(originalFile.getOrElse(basePath))
      .build().showDialog(gui)

    val cont = if (f != null && f.exists()) new MessageDialogBuilder()
      .setTitle("Le fichier existe")
      .setText("Le fichier '" + f.getName + "' existe déjà. Voulez vous le remplacer ?")
      .addButton(MessageDialogButton.Yes)
      .addButton(MessageDialogButton.No)
      .build().showDialog(gui) == MessageDialogButton.Yes
    else true

    if (cont && f != null) {
      saveToFile(e, f)
      Some(f)
    } else None
  }

  /**
   *
   * @param f
   * @return
   */
  def readFromFile(f: File): JsResult[T] = jsonFromFile[T](f)
  /**
   *
   * @param f
   * @return
   */
  def readWithAutosave(f: File): Option[ReactiveValue[T]] = readFromFile(f).asOpt.map(e => wrapAutosave(e, f))

  def saveToFile(e: T, f: File): Unit = jsonToFile(e, f)
}
