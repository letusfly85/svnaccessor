package com.jellyfish85.svnaccessor.bean

import org.tmatesoft.svn.core.wc.SVNStatusType
import org.tmatesoft.svn.core.SVNNodeKind

class SVNDiffBean extends SVNRequestBean {

  var modificationType: SVNStatusType = _

  var directoryType:    SVNNodeKind   = _

}
