package me.nov.idiotspgp.swing.list

import javax.swing.*
import javax.swing.event.MouseInputAdapter
import java.awt.event.MouseEvent

class MyMouseAdapter extends MouseInputAdapter {
  private boolean mouseDragging = false
  private int dragSourceIndex
  JList list

  @Override
  void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      dragSourceIndex = list.getSelectedIndex()
      mouseDragging = true
    }
  }

  @Override
  void mouseReleased(MouseEvent e) {
    mouseDragging = false
  }

  @Override
  void mouseDragged(MouseEvent e) {
    if (mouseDragging) {
      int currentIndex = list.locationToIndex(e.getPoint())
      if (currentIndex != dragSourceIndex && dragSourceIndex != -1) {
        int dragTargetIndex = list.getSelectedIndex()
        if(dragTargetIndex == -1)
          return
        DefaultListModel myListModel = (DefaultListModel) list.getModel()
        Object dragElement = myListModel.get(dragSourceIndex)
        myListModel.remove(dragSourceIndex)
        myListModel.add(dragTargetIndex, dragElement)
        dragSourceIndex = currentIndex
      }
    }
  }
}
