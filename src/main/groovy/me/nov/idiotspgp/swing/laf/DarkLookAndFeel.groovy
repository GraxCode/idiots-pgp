package me.nov.idiotspgp.swing.laf


import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.*
import com.github.weisj.darklaf.theme.info.ColorToneRule
import com.github.weisj.darklaf.theme.info.ContrastRule
import com.github.weisj.darklaf.theme.info.DefaultThemeProvider
import com.github.weisj.darklaf.theme.info.PreferredThemeStyle

import javax.swing.plaf.ColorUIResource
import java.awt.*

class DarkLookAndFeel {

  static {
    // most linux distros have ugly font rendering, but these here can fix that:
    System.setProperty("awt.useSystemAAFontSettings", "on")
    System.setProperty("swing.aatext", "true")
    System.setProperty("sun.java2d.xrender", "true")

    LafManager.setThemeProvider(new DefaultThemeProvider(
            new IntelliJTheme(),
            new OneDarkTheme(),
            new HighContrastLightTheme(),
            new HighContrastDarkTheme()
    ))
  }

  static void setLookAndFeel() {
    LafManager.enableLogging(true)
    LafManager.registerDefaultsAdjustmentTask({ t, d ->
      if (Theme.isDark(t)) {
        Object p = d.get("backgroundContainer")
        if (p instanceof Color) {
          d.put("backgroundContainer", new ColorUIResource(((Color) p).darker()))
        }
      }
    })
    LafManager.installTheme(new PreferredThemeStyle(ContrastRule.STANDARD, ColorToneRule.LIGHT))
  }
}