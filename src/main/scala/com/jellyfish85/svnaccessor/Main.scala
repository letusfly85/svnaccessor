package com.jellyfish85.svnaccessor

import com.jellyfish85.svnaccessor.getter.SVNDiffGetter

object Main {

  def main(args: Array[String]) {
    val getter: SVNDiffGetter = new SVNDiffGetter

    getter.getter("******", 1111111)
  }

}
