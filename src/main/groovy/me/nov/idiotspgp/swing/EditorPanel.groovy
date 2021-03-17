package me.nov.idiotspgp.swing


import com.github.weisj.darklaf.components.border.DarkBorders
import com.github.weisj.darklaf.components.text.NumberedTextComponent
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import io.subutai.pgp.PGPEncryptionUtil
import me.nov.idiotspgp.IdiotsPGP
import me.nov.idiotspgp.swing.Dialog
import me.nov.idiotspgp.swing.button.SlimButton
import me.nov.idiotspgp.swing.textfield.SizedPwdField
import me.rob.WrapLayout
import org.apache.commons.io.IOUtils

import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.undo.UndoManager
import java.awt.*
import java.nio.charset.StandardCharsets

class EditorPanel extends JPanel {
  final JTextArea textArea
  final UndoManager manager
  File inputFile
  Color standardBg

  EditorPanel() {
    setLayout(new BorderLayout())
    textArea = new JTextArea(IOUtils.toString(getClass().getResourceAsStream("/default-text.txt"), StandardCharsets.UTF_8))
    manager = new UndoManager()
    textArea.getDocument().addUndoableEditListener(manager)
    standardBg = textArea.getBackground()
    textArea.getDocument().addDocumentListener(new DocumentListener() {

      @Override
      void removeUpdate(DocumentEvent e) {
        areaChanged()
      }

      @Override
      void insertUpdate(DocumentEvent e) {
        areaChanged()
      }

      @Override
      void changedUpdate(DocumentEvent e) {
        areaChanged()
      }

    })
    addTopActionBar()

    this.add(new NumberedTextComponent(textArea), BorderLayout.CENTER)

    addBottomActionBar()
  }

  void areaChanged() {
    textArea.setBackground(standardBg)
    SlimButton.updateAllStates()
  }

  private void addTopActionBar() {
    JPanel actionBar = new JPanel(new BorderLayout())
    actionBar.setBorder(new CompoundBorder(DarkBorders.createLineBorder(0, 0, 1, 0),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)))

    JPanel buttonBar = new JPanel(new GridBagLayout())
    GridBagConstraints c = new GridBagConstraints()
    c.weightx = c.weighty = 1.0
    c.anchor = GridBagConstraints.WEST

    buttonBar.add(new SlimButton(SwingUtils.getIcon("/menu-open.svg"), "Load a file into the editor", {
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
    buttonBar.add(new SlimButton(SwingUtils.getIcon("/menu-saveall.svg"), "Save as a file", {
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
    buttonBar.add(new SlimButton(SwingUtils.getIcon("/undo.svg"), "Undo", { manager.undo() }).withCriteria { manager.canUndo() }, c)
    buttonBar.add(new SlimButton(SwingUtils.getIcon("/redo.svg"), "Redo", { manager.redo() }).withCriteria { manager.canRedo() }, c)

    buttonBar.add(new SlimButton(SwingUtils.getIcon("/delete.svg"), "Clear", { textArea.setText("") }), c)
    buttonBar.add(new SlimButton(SwingUtils.getIcon("/image.svg"), "Convert to a QR code", {
      JFileChooser jfc = inputFile == null ? new JFileChooser() : new JFileChooser(inputFile.getParentFile())
      jfc.setAcceptAllFileFilterUsed(false)
      jfc.setSelectedFile(inputFile)
      jfc.setDialogTitle("Export QR code")
      jfc.setFileFilter(new FileNameExtensionFilter("PNG file", "png"))
      int result = jfc.showSaveDialog(this)
      if (result == JFileChooser.APPROVE_OPTION) {
        File output = jfc.getSelectedFile()
        if (!output.getAbsolutePath().endsWith(".png"))
          output = new File(output.getAbsolutePath() + ".png")

        QRCodeWriter qrCodeWriter = new QRCodeWriter()
        BitMatrix bitMatrix = qrCodeWriter.encode(textArea.getText(), BarcodeFormat.QR_CODE, 1000, 1000)
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", output.toPath())
      }
    }).withCriteria { !textArea.getText().trim().isEmpty() }, c)

    actionBar.add(buttonBar, BorderLayout.WEST)
    this.add(actionBar, BorderLayout.PAGE_START)


  }

  private void addBottomActionBar() {
    JPanel actionBar = new JPanel(new BorderLayout())
    actionBar.setBorder(new CompoundBorder(DarkBorders.createLineBorder(1, 0, 0, 0),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)))

    JPanel buttonBar = new JPanel(new WrapLayout(FlowLayout.RIGHT, 0, 0))

    def getBytes = { (textArea.getText().trim() + "\n").getBytes(StandardCharsets.UTF_8) }

    def keyNonNull =  { IdiotsPGP.idiotsPGP.currentKey() != null }

    def keyPrivateNonNull =  {
      def key = IdiotsPGP.idiotsPGP.currentKey()
      return key != null && key.hasSecretKey
    }

    buttonBar.add(new SlimButton(SwingUtils.getIcon("/colBlueKey.svg"), "Encrypt", "<html>Encrypt text using <b>the receivers</b> public key", {
      def key = IdiotsPGP.idiotsPGP.currentKey()
      textArea.setText(new String(PGPEncryptionUtil.encrypt(getBytes(), key.getPublicKey(), true)))
    }).withCriteria (keyNonNull))
    buttonBar.add(new SlimButton(SwingUtils.getIcon("/colGoldKey.svg"), "Decrypt", "Decrypt text using a secret key", {
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
    }).withCriteria (keyPrivateNonNull))
    buttonBar.add(new SlimButton(SwingUtils.getIcon("/sign.svg"), "Clear Sign", "Sign a text using a secret key", {
      def key = IdiotsPGP.idiotsPGP.currentKey()
      if (key.assertPrivate()) {
        def pwdField = requestPassword()
        textArea.setText(new String(PGPEncryptionUtil.clearSign(getBytes(),
                key.getSecretKey(), pwdField.getPassword(), "SHA256")))
      }
    }).withCriteria(keyPrivateNonNull))
    buttonBar.add(new SlimButton(SwingUtils.getIcon("/verify.svg"), "Clear Verify", "Verify a signed text using the associated public key", {
      textArea.setBackground(standardBg)
      Color current = textArea.getBackground()
      def key = IdiotsPGP.idiotsPGP.currentKey()
      try {
        if (PGPEncryptionUtil.verifyClearSign(getBytes(), key.getPublicKeyRing())) {
          textArea.setBackground(new Color((int)(current.getRed() + 128) / 2, (int)(current.getGreen()+ 255) / 2, (int)(current.getBlue()+ 128) / 2))
          JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP, "Text successfully verified with this key.",
                  "Success", JOptionPane.INFORMATION_MESSAGE)
        }
      } catch (Exception e) {
        e.printStackTrace()
        textArea.setBackground(new Color((int)(current.getRed() + 255) / 2, (int)(current.getGreen()+ 128) / 2, (int)(current.getBlue()+ 128) / 2))
        JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP, "Text not verified with this key: " + e.toString(),
                "Error", JOptionPane.ERROR_MESSAGE)
      }
    }).withCriteria(keyNonNull))
    buttonBar.add(new SlimButton(SwingUtils.getIcon("/blueKey.svg"), "Sign & Encrypt", "Sign a text using a secret key and encrypt using <b>the receivers</b> public key.", {
      def key = IdiotsPGP.idiotsPGP.currentKey()
      if (key.assertPrivate()) {
        // TODO choose receivers public key
        textArea.setText(new String(PGPEncryptionUtil.signAndEncrypt(getBytes(),
                key.getSecretKey(), requestPassword().getText(), key.getPublicKey(), true)))
      }
    }).withCriteria(keyPrivateNonNull))
    buttonBar.add(new SlimButton(SwingUtils.getIcon("/goldKey.svg"), "Decrypt & Verify", "Verify a signed text using <b>the senders</b> public key and decrypt using your secret key.", {
      def key = IdiotsPGP.idiotsPGP.currentKey()
      // TODO choose senders public key
      try {
        textArea.setText(new String(PGPEncryptionUtil.decryptAndVerify(getBytes(), key.getSecretKey(), requestPassword().getText(), key.getPublicKey())))
        JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP, "Text successfully decrypted and verified with this key.",
                "Success", JOptionPane.INFORMATION_MESSAGE)
      } catch (Exception e) {
        e.printStackTrace()
        JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP, "Decrypt and verify unsuccessful: " + e.toString(),
                "Error", JOptionPane.ERROR_MESSAGE)
      }
    }).withCriteria(keyPrivateNonNull))
    actionBar.add(buttonBar, BorderLayout.CENTER)
    this.add(actionBar, BorderLayout.PAGE_END)
  }

  static SizedPwdField requestPassword() {
    def pwdField = new SizedPwdField()
    JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP, Dialog.fillInPanels(["Secret key passphrase"] as String[], pwdField as Component),
            "Password needed", JOptionPane.QUESTION_MESSAGE)
    return pwdField
  }
}
