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
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST, contentAsJson, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.vapingstampsapi.controllers.actions.AuthAction
import uk.gov.hmrc.vapingstampsapi.utils.WiremockHelper

import scala.concurrent.Future

class ApprovalControllerISpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with WiremockHelper {

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(
      "microservice.services.eis.port" -> server.port()
    )
    .overrides(
      bind[AuthAction].to[StubAuthAction]
    )
    .build()

  "GET /status/:vdsApprovalId/summary" must {

    val defaultHeaders: Seq[(String, String)] = Seq(
      "Accept"        -> "application/vnd.hmrc.1.0+json",
      "Authorization" -> "Bearer 123"
    )

    "return 200 with Summary data" in {
      val responseBody = """
                           |{
                           |  "approvalStatus": "APPROVED",
                           |  "businessName": "Acme Vaping Ltd",
                           |  "addressLine1": "1 Business Park",
                           |  "addressLine2": "London",
                           |  "postCode": "SW1A 1AA",
                           |  "contactName": "John Smith",
                           |  "telephoneNumber": "02071234567",
                           |  "stampsThreshold": 10000
                           |}
                           |
           """.stripMargin

      val request = FakeRequest(GET, "/status/AAAA0000200BB/summary")
        .withHeaders(defaultHeaders*)

      stubEndpointForGet(200, responseBody, "AAAA0000200BB")

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(OK)
      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
    }

    "return 400 error when invalid Id is passed" in {
      val responseBody =
        """
          |{
          |  "code": "Invalid approvalId",
          |  "message": "An Invalid request has been made"
          |}
          |
               """.stripMargin

      val request = FakeRequest(GET, "/status/AAAA0000200/summary")
        .withHeaders(defaultHeaders*)

      stubEndpointForGet(400, responseBody, "AAAA0000200")

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(BAD_REQUEST)
      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
    }

    "return 500 error when downstream error is returned" in {
      val responseBody =
        """
          |{
          |  "code": "Server Error",
          |  "message": "An unexpected Error has occurred downstream"
          |}
          |""".stripMargin

      val request = FakeRequest(GET, "/status/AAAA0000200BB/summary")
        .withHeaders(defaultHeaders*)

      stubEndpointForGet(500, responseBody, "AAAA0000200BB")

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(INTERNAL_SERVER_ERROR)
      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
    }
  }

  "POST /status" must {

    val defaultHeaders: Seq[(String, String)] = Seq(
      "Content-Type"  -> "application/json",
      "Authorization" -> "Bearer 123"
    )

    "return 200 with Summary data" in {
      val requestBody =
        """
          |{
          |  "contactEmail": "example@email.com",
          |  "vdsApprovalId": "AAAA0000200BB"
          |}
          |""".stripMargin

      val responseBody =
        """
          |{
          |  "approvalStatus": "APPROVED",
          |  "businessName": "Acme Vaping Ltd",
          |  "addressLine1": "1 Business Park",
          |  "addressLine2": "London",
          |  "postCode": "SW1A 1AA",
          |  "contactName": "John Smith",
          |  "telephoneNumber": "02071234567",
          |  "stampsThreshold": 10000
          |}
          |
                 """.stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(defaultHeaders*)
        .withBody(requestBody)

      stubEndpointForPost(200, requestBody, responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(OK)
      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
    }

    "return 400 error when invalid Id is passed" in {
      val requestBody =
        """
          |{
          |  "contactEmail": "example@email.com",
          |  "vdsApprovalId": "AAAA0000200"
          |}
          |""".stripMargin

      val responseBody =
        """
          |{
          |  "code": "Invalid approvalId",
          |  "message": "An Invalid request has been made"
          |}
          |
                   """.stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(defaultHeaders*)
        .withBody(requestBody)

      stubEndpointForPost(400, requestBody, responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(BAD_REQUEST)
      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
    }

    "return 500 error when downstream error is returned" in {
      val requestBody =
        """
          |{
          |  "contactEmail": "example@email.com",
          |  "vdsApprovalId": "AAAA0000200BB"
          |}
          |""".stripMargin

      val responseBody =
        """
          |{
          |  "code": "Server Error",
          |  "message": "An unexpected Error has occurred downstream"
          |}
          |""".stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(defaultHeaders*)
        .withBody(requestBody)

      stubEndpointForPost(500, requestBody, responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(INTERNAL_SERVER_ERROR)
      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
    }
  }
}
