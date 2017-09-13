package utils

import java.security.MessageDigest
import java.security.SecureRandom

import scala.annotation.tailrec

/*
 * Generates a Bearer Token with a length of
 * 32 characters (MD5) or 64 characters (SHA-256) according to the
 * specification RFC6750 (http://tools.ietf.org/html/rfc6750)
 *
 * Uniqueness obtained by hashing system time combined with a
 * application supplied 'tokenprefix' such as a sessionid or username
 *
 * public methods:
 *  generateMD5Token(tokenprefix: String): String
 *  generateSHAToken(tokenprefix: String): String
 *
 */
object BearerToken {
  val TOKEN_LENGTH = 45 // TOKEN_LENGTH is not the return size from a hash,
  // but the total characters used as random token prior to hash
  // 45 was selected because System.nanoTime().toString returns
  // 19 characters.  45 + 19 = 64.  Therefore we are guaranteed
  // at least 64 characters (bytes) to use in hash, to avoid MD5 collision < 64
  val TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_.-"
  val secureRandom = new SecureRandom()

  private def toHex(bytes: Array[Byte]): String = bytes.map( "%02x".format(_) ).mkString("")

  private def sha(s: String): String = {
    toHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8")))
  }
  private def md5(s: String): String = {
    toHex(MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8")))
  }

  // use tail recursion, functional style to build string.
  private def generate(tokenLength: Int) : String = {
    val charLen = TOKEN_CHARS.length()
    @tailrec
    def generateTokenAccumulator(accumulator: String, number: Int) : String = {
      if (number == 0) accumulator
      else generateTokenAccumulator(accumulator + TOKEN_CHARS(secureRandom.nextInt(charLen)).toString, number - 1)
    }
    generateTokenAccumulator("", tokenLength)
  }

  def generateMD5(tokenprefix: String): String = {
    md5(tokenprefix + System.nanoTime() + generate(TOKEN_LENGTH))
  }

  def generateSHA(tokenprefix: String): String = {
    sha(tokenprefix + System.nanoTime() + generate(TOKEN_LENGTH))
  }
}
