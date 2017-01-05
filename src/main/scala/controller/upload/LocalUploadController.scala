package controller.upload

import java.io.File

import controller.UploadController
import lib._
import model.Upload
import model.typebinder.UserId
import org.joda.time._
import skinny.SkinnyConfig

import scala.util.Try

object LocalUploadController extends UploadController {

  // --------------
  // sign
  override def sign: String = {
    val userId = policiesParams.getAs[UserId]("user_id").get
    val filename = params("filename")
    val ext = filename.split('.').last
    val seed = userId.value + "_" + DateTime.now().toString + "_" + filename
    val key = new Sha1Digest(seed).digestString + "." + ext

    val policyDocument = new PolicyDocument(
      LocalDateTime.now().plusMinutes(1),
      Map("key" -> key, "Content-Type" -> policiesParams.getAs[String]("content_type").get)
    )
    skinnySession.setAttribute(SessionAttribute.UploadPolicy.key, policyDocument)

    // add to uploads table
    Upload.createWithAttributes(
      'user_id -> userId.value,
      'original_filename -> filename,
      'filename -> key
    )

    // response
    toJSONString(Map(
      "url" -> "/upload/file",
      "form" -> Map(
        "key" -> key,
        "Content-Type" -> policiesParams.getAs("content_type")
      )
    ), underscoreKeys = false)
  }

  case class PolicyDocument(
    expiration: LocalDateTime,
    conditions: Map[String, Any]
  )

  // --------------
  // upload
  override def upload = {
    fileParams.get("file") match {
      case Some(file) => {
        skinnySession.getAs[LocalUploadController.PolicyDocument](SessionAttribute.UploadPolicy.key) match {
          case Some(policyDocument) => {
            if (LocalDateTime.now().isBefore(policyDocument.expiration)) {
              val key = policyDocument.conditions.get("key")
              val contentType = policyDocument.conditions.get("Content-Type")
              if (params.get("key") == key && params.get("Content-Type") == contentType) {
                SkinnyConfig.stringConfigValue("upload.local.baseDir") match {
                  case Some(baseDir) => {
                    Try {
                      file.write(new File(baseDir, key.get.toString))
                    } getOrElse logger.warn(s"failed to upload file. dir=$baseDir, file=${key.get.toString}")
                    skinnySession.removeAttribute(SessionAttribute.UploadPolicy.key)
                  }
                  case _ => throw new IllegalArgumentException
                }
              } else {
                haltWithBody(400)
              }
            } else {
              haltWithBody(403)
            }
          }
          case _ => haltWithBody(403)
        }
      }
      case _ => throw new IllegalArgumentException("file not found")
    }
  }

  // --------------
  override def uploadedFileBaseUrl: UploadedBaseURL = UploadedBaseURL("local")

}
