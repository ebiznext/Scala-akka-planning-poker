package utils

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import org.apache.commons.codec.binary.Base64

object AESEncryption {
  def encrypt(bytes: Array[Byte], secret: String): String = {
    val secretKey = new SecretKeySpec(secret.getBytes("UTF-8"), "AES")
    val encipher = Cipher.getInstance("AES" + "/ECB/PKCS5Padding")
    encipher.init(Cipher.ENCRYPT_MODE, secretKey)
    Base64.encodeBase64String(encipher.doFinal(bytes))
  }

  def decrypt(data64:String, secret: String): String = {
    val secretKey = new SecretKeySpec(secret.getBytes("UTF-8"), "AES")
    val encipher = Cipher.getInstance("AES" + "/ECB/PKCS5Padding")
    encipher.init(Cipher.DECRYPT_MODE, secretKey)
    new String(encipher.doFinal(Base64.decodeBase64(data64)))
  }
}
