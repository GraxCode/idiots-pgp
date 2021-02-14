package me.nov.idiotspgp.swing

import com.github.weisj.darklaf.components.border.DarkBorders
import com.github.weisj.darklaf.components.text.NumberedTextComponent
import io.subutai.pgp.PGPEncryptionUtil
import me.nov.idiotspgp.IdiotsPGP
import me.nov.idiotspgp.swing.Dialog
import me.nov.idiotspgp.swing.textfield.SizedPwdField

import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.undo.UndoManager
import java.awt.*
import java.nio.charset.StandardCharsets

class EditorPanel extends JPanel {
  JTextArea textArea
  UndoManager manager
  File inputFile

  EditorPanel() {
    setLayout(new BorderLayout())
    textArea = new JTextArea()
    manager = new UndoManager()
    textArea.getDocument().addUndoableEditListener(manager)

    addTopActionBar()

    this.add(new NumberedTextComponent(textArea), BorderLayout.CENTER)

    addBottomActionBar()
  }

  private void addTopActionBar() {
    JPanel actionBar = new JPanel(new BorderLayout())
    actionBar.setBorder(new CompoundBorder(DarkBorders.createLineBorder(0, 0, 1, 0),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)))

    JPanel buttonBar = new JPanel(new GridBagLayout())
    GridBagConstraints c = new GridBagConstraints()
    c.weightx = c.weighty = 1.0
    c.anchor = GridBagConstraints.WEST

    def getBytes = { (textArea.getText().trim() + "\n").getBytes(StandardCharsets.UTF_8) }

    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/menu-open.svg"), "Load a file into the editor", {
      JFileChooser jfc = new JFileChooser()
      jfc.setAcceptAllFileFilterUsed(true)
      jfc.setDialogTitle("Load a file into the editor")
      int result = jfc.showOpenDialog(this)
      if (result == JFileChooser.APPROVE_OPTION) {
        File input = jfc.getSelectedFile()
        textArea.setText(input.getText("UTF-8"))
        inputFile = input
      }
    }), c)
    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/menu-saveall.svg"), "Save as a file", {
      JFileChooser jfc = inputFile == null ? new JFileChooser() : new JFileChooser(inputFile.getParentFile())
      jfc.setAcceptAllFileFilterUsed(true)
      jfc.setSelectedFile(inputFile)
      jfc.setDialogTitle("Save text as file")
      int result = jfc.showSaveDialog(this)
      if (result == JFileChooser.APPROVE_OPTION) {
        File output = jfc.getSelectedFile()
        output.write(textArea.getText(), "UTF-8")
      }
    }), c)
    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/undo.svg"), "Undo", { manager.undo() }), c)
    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/redo.svg"), "Redo", { manager.redo() }), c)

    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/delete.svg"), "Clear", { textArea.setText("") }), c)
    actionBar.add(buttonBar, BorderLayout.WEST)
    this.add(actionBar, BorderLayout.PAGE_START)
  }

  private void addBottomActionBar() {
    JPanel actionBar = new JPanel(new BorderLayout())
    actionBar.setBorder(new CompoundBorder(DarkBorders.createLineBorder(1, 0, 0, 0),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)))

    JPanel buttonBar = new JPanel(new GridBagLayout())
    GridBagConstraints c = new GridBagConstraints()
    c.weightx = c.weighty = 1.0
    c.anchor = GridBagConstraints.EAST

    def getBytes = { (textArea.getText().trim() + "\n").getBytes(StandardCharsets.UTF_8) }

    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/colBlueKey.svg"), "Encrypt", "<html>Encrypt text using <b>the receivers</b> public key", {
      def key = IdiotsPGP.idiotsPGP.currentKey()
      textArea.setText(new String(PGPEncryptionUtil.encrypt(getBytes(), key.getPublicKey(), true)))
    }), c)
    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/colGoldKey.svg"), "Decrypt", "Decrypt text using a secret key", {
      def key = IdiotsPGP.idiotsPGP.currentKey()
      if (key.assertPrivate()) {
        try {
          def pwdField = requestPassword()
          textArea.setText(new String(PGPEncryptionUtil.decrypt(getBytes(),
                  key.getSecretKeyRing(), pwdField.getText())))
        } catch (Exception e) {
          e.printStackTrace()
          JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP, "Decryption failed, " + e.toString(),
                  "Error", JOptionPane.ERROR_MESSAGE)
        }
      }
    }), c)
    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/sign.svg"), "Clear Sign", "Sign a text using a secret key", {
      def key = IdiotsPGP.idiotsPGP.currentKey()
      if (key.assertPrivate()) {
        def pwdField = requestPassword()
        textArea.setText(new String(PGPEncryptionUtil.clearSign(getBytes(),
                key.getSecretKey(), pwdField.getPassword(), "SHA256")))
      }
    }), c)
    buttonBar.add(SwingUtils.createSlimButton(SwingUtils.getIcon("/verify.svg"), "Verify Signature", "Verify a signed text using the associated public key", {
      def key = IdiotsPGP.idiotsPGP.currentKey()
      try {
        if (PGPEncryptionUtil.verifyClearSign(getBytes(), key.getPublicKeyRing())) {
          JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP, "Text successfully verified with this key.",
                  "Success", JOptionPane.INFORMATION_MESSAGE)
          return
        }
      } catch (Exception e) {
        e.printStackTrace()
      }
      JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP, "Text not verified with this key.",
              "Error", JOptionPane.ERROR_MESSAGE)
    }), c)

    actionBar.add(buttonBar, BorderLayout.EAST)
    this.add(actionBar, BorderLayout.PAGE_END)
  }

  static SizedPwdField requestPassword() {
    def pwdField = new SizedPwdField()
    JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP, Dialog.fillInPanels(["Secret key passphrase"] as String[], pwdField as Component),
            "Password needed", JOptionPane.QUESTION_MESSAGE)
    return pwdField
  }
}
