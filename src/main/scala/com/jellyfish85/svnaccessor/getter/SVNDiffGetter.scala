package com.jellyfish85.svnaccessor.getter

import com.jellyfish85.svnaccessor.bean.SVNDiffBean
import org.tmatesoft.svn.core.wc.{SVNDiffStatus, ISVNDiffStatusHandler, SVNRevision, SVNDiffClient}
import com.jellyfish85.svnaccessor.manager.SVNManager
import org.tmatesoft.svn.core.{SVNNodeKind, SVNDepth, SVNURL}
import org.apache.commons.io.FilenameUtils

class SVNDiffGetter {

  /**
   *
   * @param path
   * @param oldRevision
   * @return
   */
  def getter(path: String, oldRevision: Long): List[SVNDiffBean] = {

    val manager: SVNManager = new SVNManager

    var diffList: List[SVNDiffBean] = List()
    val status =
      new ISVNDiffStatusHandler (){

        def  handleDiffStatus (diffStatus: SVNDiffStatus) {
          /*
          System.out.println ("\n\nDiff Status > " + diffStatus);
          System.out.println ("Path > " + diffStatus.getPath ());
          System.out.println ("File > " + diffStatus.getFile ());
          System.out.println ("Kind > " + diffStatus.getKind ());
          System.out.println ("Modification Type > " + diffStatus.getModificationType ());
          */

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

    diffList
  }
}
