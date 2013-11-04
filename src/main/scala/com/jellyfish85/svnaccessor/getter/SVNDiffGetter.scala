package com.jellyfish85.svnaccessor.getter

import com.jellyfish85.svnaccessor.bean.SVNDiffBean
import com.jellyfish85.svnaccessor.manager.SVNManager

import org.tmatesoft.svn.core.wc._
import org.tmatesoft.svn.core.{SVNNodeKind, SVNDepth, SVNURL}

import org.apache.commons.io.FilenameUtils

/**
 *
 *
 */
class SVNDiffGetter {

  /**
   *
   * @param path
   * @param oldRevision
   * @return
   */
  def get(path: String, oldRevision: Long): List[SVNDiffBean] = {

    val manager: SVNManager = new SVNManager

    var diffList: List[SVNDiffBean] = List()
    val status =
      new ISVNDiffStatusHandler (){

        def  handleDiffStatus (diffStatus: SVNDiffStatus) {

          if (diffStatus.getKind == SVNNodeKind.FILE) {
            val bean: SVNDiffBean = new SVNDiffBean

            bean.directoryType    = diffStatus.getKind()
            bean.modificationType = diffStatus.getModificationType()

            bean.path             = path + "/" + diffStatus.getPath
            bean.fileName         = FilenameUtils.getBaseName(bean.path)

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