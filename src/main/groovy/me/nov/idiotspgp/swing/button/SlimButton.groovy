package me.nov.idiotspgp.swing.button

import com.github.weisj.darklaf.ui.button.DarkButtonUI

import javax.swing.*
import java.awt.*
import java.awt.event.ActionListener
import java.util.function.BooleanSupplier

class SlimButton extends JButton {

  SlimButton(Icon icon, String tooltip, ActionListener l) {
    super(icon)
    enableSlim()
    putClientProperty(DarkButtonUI.KEY_SQUARE, true)
    if (tooltip != null)
      setToolTipText(tooltip)
    if (l != null)
      addActionListener(l)
  }

  SlimButton(Icon icon, String text, String tooltip, ActionListener l) {
    super(text, icon)
    enableSlim()

    setFont(new Font(font.name, Font.PLAIN, font.size - 1))
    if (tooltip != null)
      setToolTipText(tooltip)
    if (l != null)
      addActionListener(l)
  }

  void enableSlim() {
    putClientProperty(DarkButtonUI.KEY_NO_BORDERLESS_OVERWRITE, true)
    putClientProperty(DarkButtonUI.KEY_VARIANT, DarkButtonUI.VARIANT_BORDERLESS)
    putClientProperty(DarkButtonUI.KEY_THIN, true)
  }

  JButton withCriteria(BooleanSupplier p) {
    if (!btnEnabledCriteria.containsKey(this))
      btnEnabledCriteria.put(this, p)
    return this
  }


  @Override
  void repaint() {
    if (btnEnabledCriteria.containsKey(this))
      setEnabled(btnEnabledCriteria.get(this).getAsBoolean())

    super.repaint()
  }

  private static final Map<JButton, BooleanSupplier> btnEnabledCriteria = new LinkedHashMap<>()
  static void updateAllStates() {
    btnEnabledCriteria.forEach { jb, bs -> jb.setEnabled(bs.getAsBoolean()) }
  }

}
