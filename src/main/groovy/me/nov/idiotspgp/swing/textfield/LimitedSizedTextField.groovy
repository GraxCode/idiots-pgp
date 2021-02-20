package me.nov.idiotspgp.swing.textfield

import com.github.weisj.darklaf.ui.text.DarkTextFieldUI

import javax.swing.*
import javax.swing.text.AttributeSet
import javax.swing.text.BadLocationException
import javax.swing.text.PlainDocument
import java.awt.*

class LimitedSizedTextField extends JTextField {

  LimitedSizedTextField(int maxChars) {
    super("", 32)
    setDocument(new PlainDocument() {
      @Override
      void insertString(int offs, String str, AttributeSet a)
              throws BadLocationException {
        if (str == null)
          return

        if ((getLength() + str.length()) <= maxChars)
          super.insertString(offs, str, a)

      }
    })
  }

  def withSample(String text) {
    putClientProperty(DarkTextFieldUI.KEY_DEFAULT_TEXT, text)
    return this
  }

  LimitedSizedTextField(String s, int maxChars) {
    this(maxChars)
    setText(s)
  }

  @Override
  Dimension getMinimumSize() {
    return new Dimension(200, Math.max(25, super.size.height as int))
  }
}
