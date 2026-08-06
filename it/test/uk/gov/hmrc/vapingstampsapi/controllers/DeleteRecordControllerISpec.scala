/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.vapingstampsapi.controllers

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{DELETE, contentAsJson, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.vapingstampsapi.controllers.actions.{AuthAction, StubAuthAction}
import uk.gov.hmrc.vapingstampsapi.models.errors.InternalServerErrorApiError
import uk.gov.hmrc.vapingstampsapi.utils.WiremockHelper

import scala.concurrent.Future

class DeleteRecordControllerISpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with WiremockHelper:

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(
      "microservice.services.eis.port" -> server.port()
    )
    .overrides(
      bind[AuthAction].to[StubAuthAction]
    )
    .build()

  "DELETE /delete-record/:stampsReferenceNumber" must {

    val defaultHeaders: Seq[(String, String)] = Seq(
      "Content-Type"  -> "application/json",
      "Accept"        -> "application/vnd.hmrc.1.0+json",
      "Authorization" -> "Bearer 123"
    )

    val validStampsReferenceNumber = "GBVC0000001DS"
    val invalidStampsReferenceNumber = "INVALID123"

    "return 204 No Content when EIS successfully deletes record" in {
      stubDelete("/delete-record", NO_CONTENT)

      val request = FakeRequest(DELETE, s"/delete-record/$validStampsReferenceNumber")
        .withHeaders(defaultHeaders*)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(NO_CONTENT)
    }

    "return 400 Bad Request when stampsReferenceNumber is invalid" in {
      val request = FakeRequest(DELETE, s"/delete-record/$invalidStampsReferenceNumber")
        .withHeaders(defaultHeaders*)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(BAD_REQUEST)
      response.map(contentAsJson).map { json =>
        (json \ "code").as[String] mustBe "BAD_REQUEST"
        (json \ "message").as[String] must include("Invalid stampsReferenceNumber")
      }
    }

    "return 400 Bad Request when stampsReferenceNumber has wrong prefix" in {
      val wrongPrefix = "AAVC0000001DS"
      val request = FakeRequest(DELETE, s"/delete-record/$wrongPrefix")
        .withHeaders(defaultHeaders*)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(BAD_REQUEST)
    }

    "return 500 Internal Server Error when EIS returns 400" in {
      stubDelete("/delete-record", BAD_REQUEST, """{"error": "Bad request"}""")

      val request = FakeRequest(DELETE, s"/delete-record/$validStampsReferenceNumber")
        .withHeaders(defaultHeaders*)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(INTERNAL_SERVER_ERROR)
      response.map(contentAsJson) mustBe Some(
        Json.toJson(InternalServerErrorApiError(message = "Error occurred deleting record"))
      )
    }

    "return 500 Internal Server Error when EIS returns 500" in {
      stubDelete("/delete-record", INTERNAL_SERVER_ERROR, """{"error": "Server error"}""")

      val request = FakeRequest(DELETE, s"/delete-record/$validStampsReferenceNumber")
        .withHeaders(defaultHeaders*)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(INTERNAL_SERVER_ERROR)
      response.map(contentAsJson) mustBe Some(
        Json.toJson(InternalServerErrorApiError(message = "Error occurred deleting record"))
      )
    }

    "accept XI prefix in stampsReferenceNumber" in {
      val xiReference = "XIVC0000001DS"
      stubDelete("/delete-record", NO_CONTENT)

      val request = FakeRequest(DELETE, s"/delete-record/$xiReference")
        .withHeaders(defaultHeaders*)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(NO_CONTENT)
    }
  }