package com.jellyfish85.svnaccessor.bean

class SVNRequestBean {

  var path:     String = _
  var revision: Long   = _

  var fileName: String = _

  var fileData: Array[Byte] = _
  var oldData:  Array[Byte] = _

  var exist: Boolean = _

  def setPath(_path: String) =  path = _path

  def setRevision(_revision: Long) = revision = _revision

  def setFileName(_fileName: String) = fileName = _fileName

  def setFileData(_fileData: Array[Byte]) = fileData = _fileData

  def setOldData(_oldData: Array[Byte]) = oldData = _oldData

  def setExist(_exist: Boolean) = exist = _exist

}
