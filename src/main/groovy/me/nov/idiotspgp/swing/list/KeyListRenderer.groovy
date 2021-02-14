package me.nov.idiotspgp.swing.list

import com.github.weisj.darklaf.components.border.DarkBorders
import me.nov.idiotspgp.Key
import me.nov.idiotspgp.swing.SwingUtils

import javax.swing.*
import javax.swing.border.CompoundBorder
import java.awt.*

class KeyListRenderer extends JLabel implements ListCellRenderer<Key> {

  def PRIVATE = SwingUtils.getIcon("/splitKey.svg")
  def PUBLIC = SwingUtils.getIcon("/blueKey.svg")
  def USER = SwingUtils.getIcon("/user.svg")

  @Override
  Component getListCellRendererComponent(JList<? extends Key> list, Key key, int index,
                                         boolean isSelected, boolean cellHasFocus) {
    JPanel panel = new JPanel(new BorderLayout(8, 8))
    panel.setBorder(new CompoundBorder(DarkBorders.createLineBorder(1, 0, 0, 0), BorderFactory.createEmptyBorder(4, 4, 4, 4)))

    JPanel center = new JPanel(new GridLayout(2 + key.getUserIds().size(), 1))
    center.setOpaque(false)

    def name = new JLabel(key.getName())
    name.setIcon(key.hasSecretKey ? PRIVATE : PUBLIC)
    name.setHorizontalAlignment(LEFT)
    center.add(name)


    key.getUserIds().each {
      def user = new JLabel(it)
      user.setIcon(USER)
      user.setHorizontalAlignment(LEFT)
      user.setFont(new Font(user.font.name, user.font.style, user.font.size - 2))
      center.add(user)
    }
    def keyDesc = key.getDescription()
    def desc = new JLabel(keyDesc)
    desc.setHorizontalAlignment(CENTER)
    desc.setFont(new Font(desc.font.name, Font.ITALIC, desc.font.size - 2))
    desc.setVisible(keyDesc != null && keyDesc.trim().length() > 0)
    center.add(desc)


    panel.add(center, BorderLayout.CENTER)

    JPanel infos = new JPanel(new GridBagLayout())
    infos.setOpaque(false)

    GridBagConstraints c = new GridBagConstraints()
    c.weightx = c.weighty = 1.0

    c.anchor = GridBagConstraints.WEST
    def bits = new JLabel(key.getPublicKey().getBitStrength().toString())
    bits.setHorizontalAlignment(LEFT)
    infos.add(bits, c)


    c.anchor = GridBagConstraints.EAST

    def date = new JLabel(key.dateString())
    date.setHorizontalAlignment(RIGHT)
    infos.add(date, c)


    infos.setBorder(new CompoundBorder(DarkBorders.createLineBorder(1, 0, 0, 0), BorderFactory.createEmptyBorder(4, 0, 0, 0)))
    panel.add(infos, BorderLayout.PAGE_END)

    setText(key.getName())
    panel.setToolTipText(key.hasSecretKey ? "Key pair contains an encrypted secret key and a public key" : "Key pair only consists of a public key")
    return panel
  }
}