package com.jellyfish85.svnaccessor.getter

import com.jellyfish85.svnaccessor.bean.SVNRequestBean
import org.tmatesoft.svn.core.wc.{SVNDiffStatus, ISVNDiffStatusHandler, SVNRevision, SVNDiffClient}
import com.jellyfish85.svnaccessor.manager.SVNManager
import org.tmatesoft.svn.core.{SVNDepth, SVNURL}
import org.tmatesoft.svn.core.io.SVNRepository
import java.io.ByteArrayOutputStream

class SVNDiffGetter {

  /**
   *
   * @param path
   * @param oldRevision
   * @return
   */
  def getter(path: String, oldRevision: Long) {
    var list: List[SVNRequestBean] = List()

    val manager: SVNManager = new SVNManager
    val repository: SVNRepository = manager.repository

    val out: ByteArrayOutputStream = new ByteArrayOutputStream()

    /**
     * @example
     *
       public void doDiffStatus(
           SVNURL url1,
           SVNRevision rN,
           SVNURL url2,
           SVNRevision rM,
           SVNDepth depth,
           boolean useAncestry,
           ISVNDiffStatusHandler handler)
        throws SVNException
     *
     */
    val status =
      new ISVNDiffStatusHandler (){
        var _diffStatus: SVNDiffStatus = null

        def  handleDiffStatus (diffStatus: SVNDiffStatus) {
          this._diffStatus = diffStatus
          System.out.println ("\n\nDiff Status > " + diffStatus);
          System.out.println ("Path > " + diffStatus.getPath ());
          System.out.println ("File > " + diffStatus.getFile ());
          System.out.println ("Kind > " + diffStatus.getKind ());
          System.out.println ("Modification Type > " + diffStatus.getModificationType ());
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
    println(status._diffStatus.getModificationType)
  }

}
