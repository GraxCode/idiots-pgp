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
    return new Dimension(200, Math.max(25, super.size.height as int))
  }
}
