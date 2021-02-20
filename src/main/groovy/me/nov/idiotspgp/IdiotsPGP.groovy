package me.nov.idiotspgp

import com.github.weisj.darklaf.settings.ThemeSettings
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.dweymouth.AES
import me.dweymouth.AES.InvalidPasswordException
import me.nov.idiotspgp.swing.EditorPanel
import me.nov.idiotspgp.swing.KeyManagerPanel
import me.nov.idiotspgp.swing.SwingUtils
import me.nov.idiotspgp.swing.laf.DarkLookAndFeel
import me.nov.idiotspgp.swing.list.KeyList
import me.nov.idiotspgp.swing.listener.ExitListener
import me.nov.idiotspgp.swing.textfield.SizedPwdField
import org.apache.commons.io.IOUtils

import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import java.awt.*
import java.awt.event.KeyEvent

class IdiotsPGP extends JFrame {

  static final Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create()
  private static RATIO = 4d / 3d
  static IdiotsPGP idiotsPGP
  KeyList keyList
  EditorPanel editorPanel
  File inputFile

  IdiotsPGP() {
    this.initBounds()
    this.setIconImage(SwingUtils.iconToFrameImage(SwingUtils.getIcon("/goldKey.svg"), this))
    this.setTitle("Idiot's PGP")
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
    this.addWindowListener(new ExitListener(this))
    this.initializeFrame()
    this.initializeMenu()
  }

  static void main(String[] args) {
    DarkLookAndFeel.setLookAndFeel()
    (idiotsPGP = new IdiotsPGP()).setVisible(true)
  }

  Key currentKey() {
    return keyList.getSelectedValue()
  }

  private void initializeMenu() {
    JMenuBar bar = new JMenuBar()
    JMenu file = new JMenu("File")
    JMenuItem load = new JMenuItem("Load key list")

    load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK))
    load.addActionListener {
      JFileChooser jfc = new JFileChooser()
      jfc.setAcceptAllFileFilterUsed(false)
      jfc.setDialogTitle("Load a key list")
      jfc.setFileFilter(new FileNameExtensionFilter("Idiot's PGP key list file (*.ipg)", "ipg"))
      int result = jfc.showOpenDialog(this)
      if (result == JFileChooser.APPROVE_OPTION) {
        File input = jfc.getSelectedFile()
        def listModel = keyList.getModel() as DefaultListModel
        def decrypted = new ByteArrayOutputStream()
        try {
          AES.decrypt(requestPassword().getPassword(),
                  new FileInputStream(input),
                  decrypted)
        } catch (InvalidPasswordException ignored) {
          JOptionPane.showMessageDialog(this, "Invalid password.",
                  "Error", JOptionPane.ERROR_MESSAGE)
          return
        }
        Key[] l = GSON.fromJson(new String(decrypted.toByteArray()), Key[].class)
        listModel.clear()
        l.each { listModel.addElement(it) }
        inputFile = input
      }
    }
    file.add(load)
    JMenuItem save = new JMenuItem("Save key list")

    save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK))
    save.addActionListener {
      if (inputFile != null) {
        inputFile.write(GSON.toJson((keyList.getModel() as DefaultListModel).toArray(), Object[].class), "UTF-8")
        return
      }
      JFileChooser jfc = new JFileChooser()
      jfc.setAcceptAllFileFilterUsed(false)
      jfc.setDialogTitle("Save a key list")
      jfc.setFileFilter(new FileNameExtensionFilter("Idiot's PGP key list file (*.ipg)", "ipg"))
      int result = jfc.showSaveDialog(this)
      if (result == JFileChooser.APPROVE_OPTION) {
        File output = jfc.getSelectedFile()
        if (!output.getAbsolutePath().endsWith(".ipg"))
          output = new File(output.getAbsolutePath() + ".ipg")

        AES.encrypt(256, requestPassword().getPassword(),
                IOUtils.toInputStream(GSON.toJson((keyList.getModel() as DefaultListModel).toArray(), Key[].class), "UTF-8"),
                new FileOutputStream(output))
        inputFile = output
      }
    }
    file.add(save)
    bar.add(file)
    JMenu help = new JMenu("Help")
    JMenuItem laf = new JMenuItem("Look and feel settings")
    laf.setIcon(ThemeSettings.getIcon())
    laf.addActionListener { ThemeSettings.showSettingsDialog(this) }
    JMenuItem about = new JMenuItem("About")
    about.addActionListener {
      JOptionPane.showMessageDialog(this,
              "<html>Idiot's PGP was made by <i>noverify</i> a.k.a <i>GraxCode</i> in 2021.<br><br>" +
                      "This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3" +
                      ".<br>You are welcome to contribute to this project on GitHub!",
              "About", JOptionPane.INFORMATION_MESSAGE)
    }
    help.add(about)
    help.add(laf)
    bar.add(help)
    this.setJMenuBar(bar)
  }


  private void initializeFrame() {
    JPanel content = new JPanel(new BorderLayout())
    def split = new JSplitPane()
    keyList = new KeyList()
    def model = new DefaultListModel()
    keyList.setModel(model)
    split.setLeftComponent(new KeyManagerPanel(keyList))
    split.setRightComponent(editorPanel = new EditorPanel())

    content.add(split, BorderLayout.CENTER)
    setContentPane(content)
  }

  private void initBounds() {
    Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds()
    int w = screenSize.width as int
    int h = screenSize.height as int
    int height = h / 2 as int
    int width = (height * RATIO) as int
    setBounds(w / 2 - width / 2 as int, h / 2 - height / 2 as int, width, height)
    setMinimumSize(new Dimension(width / 1.25 as int, height / 1.25 as int))
  }

  static SizedPwdField requestPassword() {
    def pwdField = new SizedPwdField()
    JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP, me.nov.idiotspgp.swing.Dialog.fillInPanels(["Key list passphrase"] as String[], pwdField as Component),
            "Password needed", JOptionPane.QUESTION_MESSAGE)
    return pwdField
  }
}
