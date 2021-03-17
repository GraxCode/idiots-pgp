package me.nov.idiotspgp.swing.list

import me.nov.idiotspgp.IdiotsPGP
import me.nov.idiotspgp.Key
import me.nov.idiotspgp.swing.button.SlimButton

import javax.swing.*
import java.awt.*

class KeyList extends JList<Key> {

  KeyList() {
    setCellRenderer(new KeyListRenderer())
    def ad = new MyMouseAdapter(list: this)
    addMouseListener(ad)
    addMouseMotionListener(ad)
    addListSelectionListener {
      SlimButton.updateAllStates()
    }
  }

  @Override
  Dimension getMinimumSize() {
    def size = super.getMinimumSize()
    size.setSize(IdiotsPGP.idiotsPGP.getSize().getWidth() / 6 as int, size.getHeight())
    return size
  }
}
