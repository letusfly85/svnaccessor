package com.jellyfish85.svnaccessor.getter

import com.jellyfish85.svnaccessor.manager.SVNManager
import com.jellyfish85.svnaccessor.bean.SVNRequestBean
import java.io.{FileOutputStream, ByteArrayOutputStream, File}
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.{SVNNodeKind, SVNDirEntry, SVNException, SVNProperties}
import org.apache.commons.io.{FilenameUtils, FileUtils}
import org.apache.commons.io.filefilter.DirectoryFileFilter

/**
 * == Over View ==
 *
 * to get subversion files or a directory
 *
 */
class SVNGetFiles {

  /**
   * == Over View ==
   *
   * download svn files by using list of bean
   *
   * @param list
   * @param folder
   * @throws org.tmatesoft.svn.core.SVNException
   */
  @throws(classOf[SVNException])
  def simpleGetFiles(list: List[SVNRequestBean], folder: File) {
    FileUtils.cleanDirectory(folder)

    val manager   : SVNManager     = new SVNManager
    val repository: SVNRepository  = manager.repository

    list.foreach {entity: SVNRequestBean =>
      val out: ByteArrayOutputStream = new ByteArrayOutputStream()
      println(entity.path)
      repository.getFile(
        entity.path,
        entity.revision,
        SVNProperties.wrap(java.util.Collections.EMPTY_MAP),
        out
      )

      val data: Array[scala.Byte] = out.toByteArray
      if (!(folder).exists()) {
        FileUtils.forceMkdir(folder)
      }

      val fos: FileOutputStream = new FileOutputStream(new File(folder.getPath, entity.fileName))
      fos.write(data)
      fos.close()

    }
  }

  /**
   * == Over View ==
   *
   * download svn files by using list of bean
   *
   * @param list
   * @param folder
   * @throws org.tmatesoft.svn.core.SVNException
   */
  @throws(classOf[SVNException])
  def simpleGetFilesWithDirectory(list: List[SVNRequestBean], folder: File) {
    FileUtils.cleanDirectory(folder)

    val manager   : SVNManager     = new SVNManager
    val repository: SVNRepository  = manager.repository

    list.foreach {entity: SVNRequestBean =>
      val out: ByteArrayOutputStream = new ByteArrayOutputStream()
      println(entity.path)
      repository.getFile(
        entity.path,
        entity.revision,
        SVNProperties.wrap(java.util.Collections.EMPTY_MAP),
        out
      )

      val data: Array[scala.Byte] = out.toByteArray
      if (!(folder).exists()) {
        FileUtils.forceMkdir(folder)
      }

      val distPath: String = FilenameUtils.getFullPath(entity.path).replace(entity.fileName, "")
      val dist: File = new File(folder.getPath, distPath)
      if (!dist.exists()) {
        FileUtils.forceMkdir(dist)
      }
      val filePath: File = new File(dist.getPath, entity.fileName)

      //val fos: FileOutputStream = new FileOutputStream(new File(folder.getPath, entity.fileName))
      val fos: FileOutputStream = new FileOutputStream(filePath)
      fos.write(data)
      fos.close()

    }
  }

  /**
   * == Over View ==
   *
   * download subversion repository files recursively by using a repository path
   *
   * @param repository
   * @param folder
   * @param path
   * @param level
   * @param simpleFilter
   * @throws org.tmatesoft.svn.core.SVNException
   */
  @throws(classOf[SVNException])
  def simpleGetFilesRecursive(repository: SVNRepository,
                              folder: String, path: String, level: Int, 
                              simpleFilter: (SVNRequestBean => Boolean)) {

    // only first time, it is called to remove a work directory
    if (level == 0) {
      val work = new File(folder)
      if (work.exists()) FileUtils.cleanDirectory(work)
      FileUtils.forceMkdir(work)
    }

    val dirEntries:java.util.List[SVNDirEntry] = new java.util.ArrayList[SVNDirEntry]()
    repository.getDir(
      path,
      repository.getLatestRevision,
      SVNProperties.wrap(java.util.Collections.EMPTY_MAP),
      dirEntries
    )

    //convert SVNEntry to SVNRequestBean
    var list: List[SVNRequestBean] = List()
    for (i <- 0 to dirEntries.size() -1) {
      val entry: SVNDirEntry = dirEntries.get(i)

      val entity: SVNRequestBean = new SVNRequestBean
      entity.fileName = entry.getName
      entity.path     = new File(path,  entry.getRelativePath).getPath
      entity.path     = entity.path.replace('\\', '/')
      entity.revision = entry.getRevision.asInstanceOf[Int]

      if (entry.getKind == SVNNodeKind.FILE) {
        list ::= entity

      } else if (entry.getKind == SVNNodeKind.DIR) {
        val newFolder: String = (new File(folder, entry.getName)).getPath
        val newPath  : String = (new File(path, entry.getName)).getPath.replace('\\', '/')
        val newLevel: Int = level + 1

        simpleGetFilesRecursive(repository, newFolder, newPath, newLevel, simpleFilter)
      }
    }

    list.filter(entity => simpleFilter(entity)).foreach {entity: SVNRequestBean =>
      val out: ByteArrayOutputStream = new ByteArrayOutputStream()
      println("downloading ... " + entity.path)

      repository.getFile(
        entity.path,
        repository.getLatestRevision,
        SVNProperties.wrap(java.util.Collections.EMPTY_MAP),
        out
      )

      val data = out.toByteArray

      if (!(new File(folder).exists())) {
        FileUtils.forceMkdir(new File(folder))
      }

      val fos: FileOutputStream = new FileOutputStream(new File(folder, entity.fileName))
      fos.write(data)
      fos.close()
    }
    list = List()
  }
}
