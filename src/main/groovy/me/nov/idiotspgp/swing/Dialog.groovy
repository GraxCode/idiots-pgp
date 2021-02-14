package me.nov.idiotspgp.swing

import javax.swing.*
import java.awt.*

class Dialog {
  static JPanel fillInPanels(String[] fieldNames, Component[] fieldList) {
    JPanel panel = new JPanel(new BorderLayout(8, 8))
    JPanel inputNames = new JPanel(new GridLayout(fieldNames.length, 1))
    fieldNames.each { inputNames.add(new JLabel(it + ":")) }
    JPanel fields = new JPanel(new GridLayout(fieldNames.length, 1))
    fieldList.each { fields.add(it) }
    panel.add(inputNames, BorderLayout.WEST)
    panel.add(fields, BorderLayout.CENTER)
    return panel
  }
}
