package com.jellyfish85.svnaccessor.getter

import com.jellyfish85.svnaccessor.manager.SVNManager
import com.jellyfish85.svnaccessor.bean.{SVNDiffBean, SVNRequestBean}

import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.{SVNNodeKind, SVNDirEntry, SVNException, SVNProperties}
import org.apache.commons.io.{FilenameUtils, FileUtils}

import java.text.SimpleDateFormat
import java.io.{FileOutputStream, ByteArrayOutputStream, File}
import java.util

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
   * @param entity
   * @param folder
   * @throws org.tmatesoft.svn.core.SVNException
   */
  @throws(classOf[SVNException])
  def simpleGetFile(entity: SVNRequestBean, folder: File, removePath: String) {
    val manager   : SVNManager     = new SVNManager
    val repository: SVNRepository  = manager.repository
    println(repository.getLatestRevision)

    val out: ByteArrayOutputStream = new ByteArrayOutputStream()
    println(entity.path)
    repository.getFile(
      entity.path,
      repository.getLatestRevision,
      SVNProperties.wrap(java.util.Collections.EMPTY_MAP),
      out
    )

    val target  = new File(entity.path.replace(removePath, ""))
    val target2 = new File(folder.getPath, target.getPath)
    val data: Array[scala.Byte] = out.toByteArray
    if (!(target2.getParentFile.exists())) {
      FileUtils.forceMkdir(target2.getParentFile)
    }

    val fos: FileOutputStream = new FileOutputStream(target2)
    fos.write(data)
    fos.close()
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
                              simpleFilter: (SVNRequestBean => Boolean),
                               removePath: String) {

    simpleGetFilesRecursiveWithRemovePath(repository, folder, path, level, simpleFilter, removePath)

  }

  /**
   * == Over View ==
   *
   * download subversion repository files recursively by using a repository path
   *
   * @param folder
   * @param path
   * @param level
   * @param obj
   * @throws org.tmatesoft.svn.core.SVNException
   */
  @throws(classOf[SVNException])
  def simpleGetFilesRecursive[A <: SVNFilter](
                              folder: String, path: String, level: Int,
                              obj: A, removePath: String
                                               ) {

    val svn: SVNManager = new SVNManager
    val repository = svn.repository

    def filter(bean: SVNRequestBean): Boolean = obj.filter(bean)
    simpleGetFilesRecursive(repository, folder, path, level, filter(_), removePath)

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
   * @param removePath
   * @throws org.tmatesoft.svn.core.SVNException
   */
  @throws(classOf[SVNException])
  def simpleGetFilesRecursiveWithRemovePath(repository: SVNRepository,
                              folder: String, path: String, level: Int,
                              simpleFilter: (SVNRequestBean => Boolean),
                              removePath: String) {

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
        val newPath  : String = (new File(path, entry.getName)).getPath.replace('\\', '/')
        val newLevel: Int = level + 1

        simpleGetFilesRecursive(repository, folder, newPath, newLevel, simpleFilter, removePath)
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

      val target  = new File(entity.path.replace(removePath, ""))
      val target2 = new File(folder, target.getPath)
      if (!(target2.getParentFile.exists())) {
        FileUtils.forceMkdir(target2.getParentFile)
      }

      val fos: FileOutputStream = new FileOutputStream(new File(target2.getParentFile, entity.fileName))
      fos.write(data)
      fos.close()
    }
    list = List()
  }

  /**
   *
   *
   * @param path
   * @param simpleFilter
   * @return
   */
  def getSVNInfo(
      path: String,
      simpleFilter: (SVNRequestBean => Boolean)
                  ): List[SVNRequestBean] = {
    val manager: SVNManager = new SVNManager
    val repository: SVNRepository = manager.repository

    val list: List[SVNRequestBean] = getSVNInfo(repository, path, simpleFilter)

    list
  }

  /**
   * == getSVNInfo ==
   *
   * @param repository
   * @param path
   * @param simpleFilter
   * @return
   */
  private def getSVNInfo(
                         repository: SVNRepository,
                         path      : String,
                         simpleFilter: (SVNRequestBean => Boolean)
                         ): List[SVNRequestBean] = {

    var resultSets: List[SVNRequestBean] = List()

    val simpleDateFormatYMD: SimpleDateFormat = new SimpleDateFormat("yyyyMMdd")
    val simpleDateFormatHMS: SimpleDateFormat = new SimpleDateFormat("HHmmss")

    val headRevision = repository.getLatestRevision
    val dirEntries:java.util.List[SVNDirEntry] = new java.util.ArrayList[SVNDirEntry]()
    repository.getDir(
      path,
      headRevision,
      SVNProperties.wrap(java.util.Collections.EMPTY_MAP),
      dirEntries
    )

    //convert SVNEntry to SVNRequestBean
    for (i <- 0 to dirEntries.size() -1) {
      val entry: SVNDirEntry = dirEntries.get(i)

      val bean: SVNRequestBean = new SVNRequestBean
      bean.fileName = entry.getName
      bean.path     = new File(path,  entry.getRelativePath).getPath
      bean.path     = bean.path.replace('\\', '/')
      bean.revision = entry.getRevision.asInstanceOf[Int]
      bean.headRevision = headRevision

      bean.author    = entry.getAuthor
      bean.commitYmd = simpleDateFormatYMD.format(entry.getDate)
      bean.commitHms = simpleDateFormatHMS.format(entry.getDate)

      if (entry.getKind == SVNNodeKind.FILE) {
        resultSets ::= bean

      } else if (entry.getKind == SVNNodeKind.DIR) {
        val newPath  : String = (new File(path, entry.getName)).getPath.replace('\\', '/')

        resultSets :::= getSVNInfo(repository, newPath, simpleFilter)
      }
    }

    resultSets.filter(x => simpleFilter(x))
  }

  /**
   * == modifyAttribute2Current ==
   *
   * add current revision's attributes to SVNDiffBean
   * by using  SVNRepository.getDir function
   *
   * @author wada shunsuke
   * @param list List of SVNDiffBean
   * @return List of SVNDiffBean
   */
  @throws(classOf[SVNException])
  def modifyAttribute2Current(list: List[SVNDiffBean]): List[SVNDiffBean] = {
    var resultSets: List[SVNDiffBean] = List()

    val simpleDateFormatYMD: SimpleDateFormat = new SimpleDateFormat("yyyyMMdd")
    val simpleDateFormatHMS: SimpleDateFormat = new SimpleDateFormat("HHmmss")

    val manager: SVNManager = new SVNManager
    val repository: SVNRepository = manager.repository

    list.foreach {bean: SVNDiffBean =>
      val result: SVNDiffBean = bean

      val modifiedEntry: SVNDirEntry = repository.info(bean.path, repository.getLatestRevision)
      result.author    = modifiedEntry.getAuthor
      result.revision  = modifiedEntry.getRevision
      result.commitYmd = simpleDateFormatYMD.format(modifiedEntry.getDate)
      result.commitHms = simpleDateFormatHMS.format(modifiedEntry.getDate)

      resultSets ::= result
    }

    resultSets
  }
}

/**
 * == SVNFilter ==
 *
 * this trait will be used by java code to generate instance which has filtering function.
 *
 */
trait SVNFilter {
  def filter(bean: SVNRequestBean): Boolean
}


