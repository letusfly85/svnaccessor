package com.jellyfish85.svnaccessor.getter

import com.jellyfish85.svnaccessor.bean.SVNDiffBean
import com.jellyfish85.svnaccessor.manager.SVNManager

import org.tmatesoft.svn.core.wc._
import org.tmatesoft.svn.core.{SVNNodeKind, SVNDepth, SVNURL}

import org.apache.commons.io.FilenameUtils
import java.util


/**
 *
 *
 */
class SVNDiffGetter {

  def _getDiffSummary(path1: String, revision1: Long, path2: String, revision2: Long): util.ArrayList[SVNDiffBean] = {
    val list = getDiffSummary(path1, revision1, path2, revision2)

    val ary: util.ArrayList[SVNDiffBean] = new util.ArrayList[SVNDiffBean]()
    list.foreach(x => ary.add(x))

    ary
  }

  /**
   * get differences between two url by using path and revision
   *
   * @param path1
   * @param revision1
   * @param path2
   * @param revision2
   * @return
   */
  def getDiffSummary(path1: String, revision1: Long, path2: String, revision2: Long): List[SVNDiffBean] = {

    val manager: SVNManager = new SVNManager
    var diffList: List[SVNDiffBean] = List()
    val baseUrl: String = manager.repository.getLocation.toString
    val status =
      new ISVNDiffStatusHandler (){
        def  handleDiffStatus (diffStatus: SVNDiffStatus) {

          if (diffStatus.getKind == SVNNodeKind.FILE) {
            val bean: SVNDiffBean = new SVNDiffBean

            bean.directoryType    = diffStatus.getKind
            bean.modificationType = diffStatus.getModificationType
            bean.path             = diffStatus.getURL.toString.replace(baseUrl, "")
            bean.url              = diffStatus.getURL.toString
            bean.fileName         = FilenameUtils.getName(bean.path)

            /*
            println("============================")
            println("=")
            println("=\t" + diffStatus.getURL.toString + "================")
            println("=\t" + bean.path)
            println("============================")
            */

            diffList ::= bean
          }
        }
      }

    val diffClient: SVNDiffClient = manager.diffClient
    diffClient.doDiffStatus(
      SVNURL.parseURIEncoded(path1),
      SVNRevision.create(revision1),
      SVNURL.parseURIEncoded(path2),
      SVNRevision.create(revision2),
      SVNDepth.INFINITY,
      true,
      status
    )

    val _list = shrink(diffList)

    val modifier = new SVNGetFiles[SVNDiffBean]
    modifier.reSetRepository(path2)
    modifier.modifyAttribute2Current(_list)
  }

  /**
   *
   * @param path
   * @param oldRevision
   * @return
   */
  def get(path: String, oldRevision: Long): List[SVNDiffBean] = {

    val manager: SVNManager = new SVNManager

    val baseUrl: String = manager.repository.getLocation.toString

    var diffList: List[SVNDiffBean] = List()
    val status =
      new ISVNDiffStatusHandler (){

        def  handleDiffStatus (diffStatus: SVNDiffStatus) {

          if (diffStatus.getKind == SVNNodeKind.FILE) {
            val bean: SVNDiffBean = new SVNDiffBean

            bean.directoryType    = diffStatus.getKind()
            bean.modificationType = diffStatus.getModificationType()

            bean.path             = path.replace(baseUrl, "") + "/" + diffStatus.getPath
            bean.fileName         = FilenameUtils.getName(bean.path)

            diffList ::= bean
          }
        }
      }

    val diffClient: SVNDiffClient = manager.diffClient
    diffClient.doDiffStatus(
      SVNURL.parseURIEncoded(path),
      SVNRevision.HEAD,
      SVNURL.parseURIEncoded(path),
      SVNRevision.create(oldRevision),
      SVNDepth.INFINITY,
      true,
      status
    )

    shrink(diffList)
  }

  /**
   *
   *
   * @param diffList
   * @return
   */
  def shrink(diffList: List[SVNDiffBean]): List[SVNDiffBean] = {
    var list: List[SVNDiffBean] = List()

    diffList.foreach {bean =>
       if (bean.modificationType == SVNStatusType.STATUS_DELETED) {
          if (list.contains(bean)) {
            list = list.filter(x => x.path != bean.path)

            list ::= bean
          }

       } else {
         if (!list.contains(bean)) {
           list ::= bean
         }
       }
    }

    list
  }
}