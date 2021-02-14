package me.nov.idiotspgp.swing.textfield

import javax.swing.*
import javax.swing.text.AttributeSet
import javax.swing.text.BadLocationException
import javax.swing.text.PlainDocument
import java.awt.*

class LimitedSizedTextField extends JTextField {

  LimitedSizedTextField(int maxChars) {
    super("", maxChars)
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

  LimitedSizedTextField(String s, int maxChars) {
    this(maxChars)
    setText(s)
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
