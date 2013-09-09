package com.jellyfish85.com.svnaccessor.editor

import org.tmatesoft.svn.core.io.{ISVNEditor, SVNRepository}
import org.tmatesoft.svn.core.{SVNProperties, SVNCommitInfo, SVNException, SVNNodeKind}
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator

import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import java.util.Collections

import com.jellyfish85.svnaccessor.bean.SVNRequestBean
import com.jellyfish85.svnaccessor.manager.SVNManager


class SVNEditor {

  /**
   * == Over View ==
   *
   * check if the path exists on repository or not
   *
   *
   * @param repository svn repository
   * @param path path from baseUrl
   * @return existence
   */
  @throws(classOf[SVNException])
  def entryExists(repository: SVNRepository, path: String): Boolean = {
    println("checking exist..." + path)
    val node: SVNNodeKind = repository.checkPath(path, -1L)

    if (node == SVNNodeKind.NONE || node == SVNNodeKind.DIR) {
      return false

    } else {
      return true
    }
  }

  /**
   * == Over View ==
   *
   * update svn entry
   *
   * @param repository  svn repository
   * @param path        svn entry path
   * @param fileData    new file data
   * @param message     svn commit message
   */
  def update(repository: SVNRepository, path: String, fileData: Array[Byte], message: String) {
    var commitInfo: SVNCommitInfo = null

    val oldOut: ByteArrayOutputStream = new ByteArrayOutputStream()
    repository.getFile(path, -1L, SVNProperties.wrap(Collections.EMPTY_MAP), oldOut)
    val oldData: Array[Byte] = oldOut.toByteArray

    val editor: ISVNEditor = repository.getCommitEditor(message, null, true, null)

    try {
      editor.openRoot(-1L)
      editor.openFile(path, -1L)
      editor.applyTextDelta(path, null)

      val deltaGenerator: SVNDeltaGenerator = new SVNDeltaGenerator()
      val checksum: String = deltaGenerator.sendDelta(
        path,
        new ByteArrayInputStream(oldData),
        0,
        new ByteArrayInputStream(fileData),
        editor,
        true
      )

      editor.closeFile(path, checksum)
      editor.closeDir()
      commitInfo = editor.closeEdit()

    } catch {
      case e: SVNException =>
        e.printStackTrace()
        editor.abortEdit()
    }

  }

  /**
   * == Over View ==
   *
   * add new entry to svn repository
   *
   * @param repository  svn repository
   * @param path        svn entry path
   * @param fileData    new file data
   * @param message     svn commit message
   *
   */
  def add(repository: SVNRepository, path: String, fileData: Array[Byte], message: String) {
    var commitInfo: SVNCommitInfo = null

    val editor: ISVNEditor = repository.getCommitEditor(message, null, true, null)

    try {
      editor.openRoot(-1L)
      editor.openFile(path, -1L)
      editor.applyTextDelta(path, null)

      val deltaGenerator: SVNDeltaGenerator = new SVNDeltaGenerator()
      val checksum: String = deltaGenerator.sendDelta(path, new ByteArrayInputStream(fileData), editor, true)

      editor.closeFile(path, checksum)
      editor.closeDir()
      commitInfo = editor.closeEdit()

    } catch {
      case e: SVNException =>
        e.printStackTrace()
        editor.abortEdit()
    }
  }

  /**
   * == Over View ==
   *
   * append svn files at once
   *
   * @param _repository  svn repository
   * @param entries     target entries
   * @param message     svn commit message
   */
  @throws(classOf[SVNException])
  def appendEntries(_repository: SVNRepository, entries: List[SVNRequestBean], message: String) {
    var commitInfo: SVNCommitInfo = null

    val manager: SVNManager = new SVNManager
    val repository: SVNRepository = manager.repository

    var targets: List[SVNRequestBean] = List()

    entries.foreach {entry: SVNRequestBean =>
      val nodeKind = repository.checkPath(entry.path, -1)
      entry.exist = !(nodeKind == SVNNodeKind.NONE || nodeKind == SVNNodeKind.DIR)

      if (entry.exist) {
        val oldOut = new ByteArrayOutputStream()
        repository.getFile(entry.path, -1, null, oldOut)
        entry.oldData = oldOut.toByteArray()
      }

      targets ::= entry
    }

    val editor: ISVNEditor =  repository.getCommitEditor(message, null, true, null)
    editor.openRoot(-1)
    try {
      targets.foreach {entry: SVNRequestBean =>

        if (entry.exist) {
          editor.openFile(entry.path, -1)
          editor.applyTextDelta(entry.path, null)

          val deltaGenerator: SVNDeltaGenerator = new SVNDeltaGenerator()
          val checksum: String = deltaGenerator.sendDelta(
            entry.path,
            new ByteArrayInputStream(entry.oldData),
            0,
            new ByteArrayInputStream(entry.fileData),
            editor,
            true
          )

          editor.textDeltaEnd(entry.path)
          editor.closeFile(entry.path, checksum)

        } else {

          editor.openDir(entry.path.replace(entry.fileName, ""), -1L)
          editor.addFile(entry.path, null, -1L)
          editor.applyTextDelta(entry.path, null)
          val deltaGenerator: SVNDeltaGenerator = new SVNDeltaGenerator()
          val checksum: String = deltaGenerator.sendDelta(entry.path, new ByteArrayInputStream(entry.fileData), editor, true)
          editor.textDeltaEnd(entry.path)
          editor.closeFile(entry.fileName, checksum)
        }
      }

      if (editor != null) {
        editor.closeDir()
      }
      commitInfo = editor.closeEdit()

    } catch {
      case e: SVNException =>
        if (editor != null) {
          editor.abortEdit()
        }
        e.printStackTrace()
    }
  }

}
