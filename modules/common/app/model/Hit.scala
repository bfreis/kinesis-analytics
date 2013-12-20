package model

import java.nio.ByteBuffer
import com.google.gson.{LongSerializationPolicy, GsonBuilder}
import com.google.common.base.Charsets

case class Hit(referer: String,
               ua: String,
               ip: String,
               sid: String,
               timestamp: Long = System.currentTimeMillis()) {
  def marshall: ByteBuffer = {
    val json = Hit.gson.toJson(this)
    ByteBuffer.wrap(json.getBytes(Charsets.UTF_8))
  }
}

object Hit {
  private val gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create()
  def unmarshall(bb: ByteBuffer): Hit = {
    gson.fromJson(new String(bb.array()), classOf[Hit])
  }
}
