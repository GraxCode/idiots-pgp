package me.nov.idiotspgp.swing.textfield

import javax.swing.*
import java.awt.*

class SizedPwdField extends JPasswordField {

  SizedPwdField() {
    super("", 32)
    putClientProperty("JPasswordField.showViewIcon", true)
  }

  @Override
  Dimension getMinimumSize() {
    return new Dimension(150, super.getSize().getHeight() as int)
  }

  @Override
  Dimension getPreferredSize() {
    return getMinimumSize()
  }
}
