package com.jellyfish85.svnaccessor.getter

import com.jellyfish85.svnaccessor.manager.SVNManager
import com.jellyfish85.svnaccessor.bean.SVNRequestBean

import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core._
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
class SVNGetFiles[A <: SVNRequestBean] {

  val manager:    SVNManager    = new SVNManager
  var repository: SVNRepository = manager.repository

  /**
   * set repository url to other
   *
   *
   * @param _baseUrl
   */
  def reSetRepository(_baseUrl: String) {
    this.repository = manager.getRepository(_baseUrl)
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
  def simpleGetFiles(list: List[SVNRequestBean], folder: File) {
    FileUtils.cleanDirectory(folder)

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
  def simpleGetFiles(list: util.ArrayList[SVNRequestBean], folder: File) {
    var targetList: List[SVNRequestBean] = List()

    for (i <- 0 to list.size()-1) {
      val bean: SVNRequestBean = list.get(i)

      targetList ::= bean
    }

    simpleGetFiles(targetList, folder)
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
    //println(repository.getLatestRevision)

    val entry: SVNDirEntry  = repository.info(".", -1)
    val dirRevision: Long   = entry.getRevision
    println(dirRevision)

    val out: ByteArrayOutputStream = new ByteArrayOutputStream()
    println(entity.path)
    repository.getFile(
      entity.path,
      dirRevision,
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
  def simpleGetFilesWithDirectory(list: List[SVNRequestBean], folder: File, cleanFlag: Boolean = true) {
    if (cleanFlag) {
      FileUtils.cleanDirectory(folder)
    }

    val headRevision: Long = repository.getLatestRevision
    val entry: SVNDirEntry  = repository.info(".", -1)
    val dirRevision: Long   = entry.getRevision

    list.foreach {entity: SVNRequestBean =>
      val out: ByteArrayOutputStream = new ByteArrayOutputStream()
      println(entity.path)
      if (entity.revision == 0.toLong) entity.revision = headRevision
      //if (entity.revision == 0.toLong) entity.revision = dirRevision
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
   * download svn files by using list of bean
   *
   * @param list
   * @param folder
   * @throws org.tmatesoft.svn.core.SVNException
   */
  @throws(classOf[SVNException])
  def simpleGetFilesWithDirectory(list: util.ArrayList[SVNRequestBean], folder: File, cleanFlag: Boolean) {
    var targetList: List[SVNRequestBean] = List()

    for (i <- 0 to list.size()-1) {
      val bean: SVNRequestBean = list.get(i)

      targetList ::= bean
    }

    simpleGetFilesWithDirectory(targetList, folder, cleanFlag)
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

    val entry: SVNDirEntry  = repository.info(".", -1)
    val dirRevision: Long   = entry.getRevision

    val dirEntries:java.util.List[SVNDirEntry] = new java.util.ArrayList[SVNDirEntry]()
    repository.getDir(
      path,
      //repository.getLatestRevision,
      dirRevision,
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
        //repository.getLatestRevision,
        dirRevision,
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
    val entry: SVNDirEntry  = repository.info(".", -1)
    val dirRevision: Long   = entry.getRevision
    //println(repository.getLocation.toString + dirRevision.toString + "\t" + headRevision.toString)

    val dirEntries:java.util.List[SVNDirEntry] = new java.util.ArrayList[SVNDirEntry]()
    repository.getDir(
      path,
      //headRevision,
      dirRevision,
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
      //bean.revision = entry.getRevision.asInstanceOf[Int]
      bean.revision  = headRevision
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
   * add current revision's attributes to SVNRequestBean
   * by using  SVNRepository.getDir function
   *
   * @author wada shunsuke
   * @param list List of A as Generics of SVNRequestBean
   * @return List of SVNDiffBean
   */
  @throws(classOf[SVNException])
  def modifyAttribute2Current(list: List[A]): List[A] = {
    var resultSets: List[A] = List()

    val simpleDateFormatYMD: SimpleDateFormat = new SimpleDateFormat("yyyyMMdd")
    val simpleDateFormatHMS: SimpleDateFormat = new SimpleDateFormat("HHmmss")

    val headRevision: Long  = repository.getLatestRevision
    val entry: SVNDirEntry  = repository.info(".", -1)
    val dirRevision: Long   = entry.getRevision

    list.foreach {bean: A =>
      try {
        val result: A = bean

        val url: String = bean.path
        println(url)
        val modifiedEntry: SVNDirEntry = repository.info(url, -1)
        //println(repository.getLocation.toString + "\t" + modifiedEntry.getKind)

        //val modifiedEntry: SVNDirEntry = repository.info(bean.path, dirRevision)
        result.author       = modifiedEntry.getAuthor
        result.headRevision = headRevision
        result.revision     = modifiedEntry.getRevision
        result.fileName     = modifiedEntry.getName
        result.commitYmd    = simpleDateFormatYMD.format(modifiedEntry.getDate)
        result.commitHms    = simpleDateFormatHMS.format(modifiedEntry.getDate)
        result.comment      = modifiedEntry.getCommitMessage

        resultSets ::= result

      } catch {
        case e: NullPointerException =>
          println("[ERROR]" + bean.path)
      }
    }

    resultSets
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
  def modifyAttribute2Current(list: util.ArrayList[SVNRequestBean]): util.ArrayList[SVNRequestBean] = {
    var targetList: List[SVNRequestBean] = List()

    val headRevision: Long = repository.getLatestRevision
    val entry: SVNDirEntry  = repository.info(".", -1)
    val dirRevision: Long   = entry.getRevision

    val simpleDateFormatYMD: SimpleDateFormat = new SimpleDateFormat("yyyyMMdd")
    val simpleDateFormatHMS: SimpleDateFormat = new SimpleDateFormat("HHmmss")

    for (i <- 0 to list.size() -1) {

      val result: SVNRequestBean = list.get(i)
      try {

        //val modifiedEntry: SVNDirEntry = repository.info(result.path, headRevision)
        val modifiedEntry: SVNDirEntry = repository.info(result.path, dirRevision)
        result.author       = modifiedEntry.getAuthor
        result.headRevision = dirRevision //headRevision
        result.revision     = modifiedEntry.getRevision
        result.fileName     = result.fileName
        result.commitYmd    = simpleDateFormatYMD.format(modifiedEntry.getDate)
        result.commitHms    = simpleDateFormatHMS.format(modifiedEntry.getDate)

        targetList ::= result

      } catch {
        case e: NullPointerException =>
          println("[ERROR]" + result.path)
          e.printStackTrace()
      }
    }

    val resultList: util.ArrayList[SVNRequestBean] = new util.ArrayList[SVNRequestBean]()

    for (i <- 0 to targetList.length - 1) {
      resultList.add(targetList(i))
    }

    resultList
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


