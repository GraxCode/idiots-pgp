package me.nov.idiotspgp.util

import name.fraser.neil.plaintext.DiffMatchPatch

class DiffMath {

  static int codeDiffDist(String from, String to) {
    DiffMatchPatch dmp = new DiffMatchPatch()
    dmp.Diff_Timeout = 0.05f
    LinkedList<DiffMatchPatch.Diff> diff = dmp.diff_main(from, to)
    return dmp.diff_levenshtein(diff)
  }

  static float confidencePercent(String from, String to) {
    DiffMatchPatch dmp = new DiffMatchPatch()
    dmp.Diff_Timeout = 0.05f
    LinkedList<DiffMatchPatch.Diff> diff = dmp.diff_main(from, to)
    return lowerConfidence(dmp.diff_levenshtein(diff), diff.sum { DiffMatchPatch.Diff it -> it.text.length() } as int) * 100
  }

  /**
   * Lower end of 95% confidence interval of opposite percent
   */
  static float lowerConfidence(int edits, int length) {
    float match = 1 - (edits / (float) length)
    float confidence = (float) (1.96f * Math.sqrt((match * (1f - match)) / (float) length))
    return match - confidence
  }
}
