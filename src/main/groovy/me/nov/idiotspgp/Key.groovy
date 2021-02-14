package me.nov.idiotspgp

import io.subutai.pgp.KeyPair
import io.subutai.pgp.PGPKeyUtil
import org.bouncycastle.openpgp.PGPPublicKey
import org.bouncycastle.openpgp.PGPPublicKeyRing
import org.bouncycastle.openpgp.PGPSecretKey
import org.bouncycastle.openpgp.PGPSecretKeyRing

import javax.swing.*

class Key {
  String name
  String description
  KeyPair keyPair
  boolean hasSecretKey = true

  String dateString() {
    return getPublicKey().getCreationTime().format('EE. MM-dd-yyyy HH:mm:ss')
  }

  boolean assertPrivate() {
    if (!hasSecretKey) {
      JOptionPane.showMessageDialog(IdiotsPGP.idiotsPGP, "The selected key has no private key information, which is required for this action.",
              "Error", JOptionPane.ERROR_MESSAGE)
      return false
    }
    return true
  }

  PGPPublicKey getPublicKey() {
    return PGPKeyUtil.readPublicKey(getPublicKeyRing())
  }

  PGPSecretKey getSecretKey() {
    return PGPKeyUtil.readSecretKey(getSecretKeyRing())
  }

  PGPPublicKeyRing getPublicKeyRing() {
    return PGPKeyUtil.readPublicKeyRing(keyPair.pubKeyring)
  }

  PGPSecretKeyRing getSecretKeyRing() {
    return PGPKeyUtil.readSecretKeyRing(keyPair.secKeyring)
  }

  List<String> getUserIds() {
    return getPublicKey().getUserIDs().toList()
  }
}
