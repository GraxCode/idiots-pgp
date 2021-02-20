package me.nov.idiotspgp.swing

import com.github.weisj.darklaf.components.border.DarkBorders
import com.github.weisj.darklaf.components.text.SearchTextFieldWithHistory
import io.subutai.pgp.KeyPair
import io.subutai.pgp.PGPEncryptionUtil
import io.subutai.pgp.PGPKeyUtil
import me.nov.idiotspgp.IdiotsPGP
import me.nov.idiotspgp.Key
import me.nov.idiotspgp.swing.Dialog
import me.nov.idiotspgp.swing.list.KeyList
import me.nov.idiotspgp.swing.textfield.LimitedSizedTextField
import me.nov.idiotspgp.swing.textfield.SizedPwdField
import me.nov.idiotspgp.util.DiffMath

import javax.swing.*
import javax.swing.border.CompoundBorder
import java.awt.*
import java.nio.charset.StandardCharsets

class KeyManagerPanel extends JPanel {

  KeyManagerPanel(KeyList list) {
    setLayout(new BorderLayout())
    addTopActionBar(list)
    this.add(new JScrollPane(list), BorderLayout.CENTER)
    addBottomActionBar(list)
  }

  void addTopActionBar(KeyList list) {
    JPanel topActionBar = new JPanel(new BorderLayout())
    topActionBar.setBorder(new CompoundBorder(DarkBorders.createLineBorder(0, 0, 1, 0),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)))

    def searchTf = new SearchTextFieldWithHistory()

    searchTf.addSearchListener {
      def search = it.getText()

      list.setSelectedValue((list.getModel() as DefaultListModel)
              .elements().toList().max { Math.max(0, DiffMath.confidencePercent(search, (it as Key).name)) }, true)
    }

    topActionBar.add(searchTf, BorderLayout.CENTER)
    this.add(topActionBar, BorderLayout.PAGE_START)
  }

  void addBottomActionBar(KeyList list) {
    JPanel actionBar = new JPanel(new BorderLayout())
    actionBar.setBorder(new CompoundBorder(DarkBorders.createLineBorder(1, 0, 0, 0),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)))

    JPanel buttonBar = new JPanel(new GridBagLayout())
    GridBagConstraints c = new GridBagConstraints()
    c.weightx = c.weighty = 1.0
    c.anchor = GridBagConstraints.WEST

    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/add.svg"), "Generate a new public and secret key", {

      def fields = [new LimitedSizedTextField(16).withSample("My Key"), new LimitedSizedTextField(32).withSample("Work email identification"),
                    new LimitedSizedTextField(64).withSample("John Smith <smith@mymail.com>"), new SizedPwdField()]
      int result = JOptionPane.showConfirmDialog(IdiotsPGP.idiotsPGP,
              Dialog.fillInPanels(["Key name", "Key description", "Key pair user ID", "Private key passphrase"] as String[], fields as Component[]),
              "Generate a new key pair", JOptionPane.OK_CANCEL_OPTION)
      if (result == JOptionPane.OK_OPTION) {
        def userId = fields[2].getText()
        def pass = fields[3].getText()
        def keyPair = PGPEncryptionUtil.generateKeyPair(userId, pass, true)
        if (keyPair == null) {
          JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP,
                  "Key pair generation failed.", "Error", JOptionPane.ERROR_MESSAGE)
          return
        }

        def key = new Key(name: fields[0].getText(), description: fields[1].getText(), keyPair: keyPair)
        (list.getModel() as DefaultListModel).addElement(key)
      }
    }), c)
    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/blueKeyFile.svg"), "Import a public key from ASCII (armored) text", {
      try {
        PGPKeyUtil.readPublicKey(IdiotsPGP.idiotsPGP.editorPanel.textArea.getText())
      } catch (Exception ignored) {
        JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP,
                "Not a valid public key (or key ring) in the text area.", "Error", JOptionPane.ERROR_MESSAGE)
        return
      }
      def fields = [new LimitedSizedTextField(16), new LimitedSizedTextField(32)]
      int result = JOptionPane.showConfirmDialog(IdiotsPGP.idiotsPGP,
              Dialog.fillInPanels(["Key name", "Key description"] as String[], fields as Component[]),
              "Generate a new key pair", JOptionPane.OK_CANCEL_OPTION)
      if (result == JOptionPane.OK_OPTION) {
        def keyPair = new KeyPair()
        keyPair.pubKeyring = IdiotsPGP.idiotsPGP.editorPanel.textArea.getText().getBytes(StandardCharsets.UTF_8)
        def key = new Key(name: fields[0].getText(), description: fields[1].getText(), keyPair: keyPair, hasSecretKey: false)
        (list.getModel() as DefaultListModel).addElement(key)
        IdiotsPGP.idiotsPGP.editorPanel.textArea.setText("")
      }
    }), c)

    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/goldKeyFile.svg"), "Import a secret key from ASCII (armored) text", {
      def key = list.getSelectedValue()
      if (key != null && !key.hasSecretKey) {
        try {
          PGPKeyUtil.readSecretKeyRing(IdiotsPGP.idiotsPGP.editorPanel.textArea.getText().getBytes(StandardCharsets.UTF_8))
        } catch (Exception ignored) {
          JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP,
                  "Not a valid secret key (or key ring) in the text area.", "Error", JOptionPane.ERROR_MESSAGE)
          return
        }
        key.keyPair.secKeyring = IdiotsPGP.idiotsPGP.editorPanel.textArea.getText().getBytes(StandardCharsets.UTF_8)
        key.hasSecretKey = true
      } else {
        JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP,
                "No key selected to add a secret key, or key already has a secret key.", "Error", JOptionPane.ERROR_MESSAGE)
      }
    }), c)
    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/edit.svg"), "Edit a key pair", {
      def key = list.getSelectedValue()
      if (key != null) {
        def fields = [new LimitedSizedTextField(key.name, 16), new LimitedSizedTextField(key.description, 32)]
        int result = JOptionPane.showConfirmDialog(IdiotsPGP.idiotsPGP,
                Dialog.fillInPanels(["Key name", "Key description"] as String[], fields as Component[]),
                "Edit a key pair", JOptionPane.OK_CANCEL_OPTION)
        if (result == JOptionPane.OK_OPTION) {
          key.name = fields[0].getText()
          key.description = fields[1].getText()
          list.revalidate()
          list.repaint()
        }
      } else {
        JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP,
                "No key selected.", "Error", JOptionPane.ERROR_MESSAGE)
      }
    }), c)
    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/delete.svg"), "Delete a key pair", {

      int result = JOptionPane.showConfirmDialog(IdiotsPGP.idiotsPGP,
              "Are you sure you want to delete this key pair? This cannot be undone.",
              "Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)
      if (result == JOptionPane.OK_OPTION)
        (list.getModel() as DefaultListModel).removeElementAt(list.getSelectedIndex())

    }), c)

    actionBar.add(buttonBar, BorderLayout.WEST)
    this.add(actionBar, BorderLayout.PAGE_END)
  }
}
