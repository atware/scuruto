package controller.upload

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import _root_.controller.UploadController
import lib._
import model.Upload
import model.typebinder.UserId
import org.apache.commons.codec.binary.Base64
import org.joda.time.format.DateTimeFormat
import org.joda.time._
import skinny._

object S3UploadController extends UploadController {

  override def destination: UploadDestination = UploadDestination("s3")

  // --------------
  // sign
  val AWS_ACCESS_KEY = "AWS_ACCESS_KEY"
  val AWS_SECRET_KEY = "AWS_SECRET_KEY"
  def sign: String = {
    val userId = policiesParams.getAs[UserId]("user_id").get
    val filename = params("filename")
    val ext = filename.split('.').last
    val seed = userId.value + "_" + DateTime.now().toString + "_" + filename
    val baseDir = SkinnyConfig.stringConfigValue("upload.s3.baseDir").getOrElse("")
    val path = baseDir + new Sha1Digest(seed).digestString + "." + ext

    val bucket = SkinnyConfig.stringConfigValue("upload.s3.bucket").getOrElse(throw new IllegalArgumentException)
    val policyDocument = toJSONString(Map(
      "expiration" -> new DateTime(DateTimeZone.UTC).plusMinutes(1).toString(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z")),
      "conditions" -> Array(
        Map("bucket" -> bucket),
        Map("key" -> path),
        Map("Content-Type" -> policiesParams.getAs("content_type")),
        Array("content-length-range", policiesParams.getAs("size"), policiesParams.getAs("size"))
      )
    ), underscoreKeys = false)
    val policy = Base64.encodeBase64String(policyDocument.getBytes("UTF-8"))
    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(new SecretKeySpec(sys.env(AWS_SECRET_KEY).getBytes("UTF-8"), "HmacSHA1"))
    val signature = Base64.encodeBase64String(hmac.doFinal(policy.getBytes("UTF-8")))

    // add to uploads table
    Upload.createWithAttributes(
      'user_id -> userId.value,
      'original_filename -> filename,
      'filename -> path
    )

    // response
    toJSONString(Map(
      "url" -> s"https://$bucket.s3.amazonaws.com/",
      "form" -> Map(
        "AWSAccessKeyId" -> sys.env(AWS_ACCESS_KEY),
        "signature" -> signature,
        "policy" -> policy,
        "key" -> path,
        "Content-Type" -> policiesParams.getAs("content_type")
      )
    ), underscoreKeys = false)
  }

  // --------------
  override def upload: Any = throw new UnsupportedOperationException

  // --------------
  override def uploadedFileBaseUrl: UploadedBaseURL = UploadedBaseURL("s3")

}
