package com.jellyfish85.svnaccessor.bean

class SVNRequestBean {

  var path:     String = _
  var revision: Long   = _

  var fileName: String = _

  var fileData: Array[Byte] = _
  var oldData:  Array[Byte] = _

  var exist: Boolean = _

}
