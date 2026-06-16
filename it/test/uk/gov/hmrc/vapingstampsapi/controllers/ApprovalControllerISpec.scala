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
import play.api.test.Helpers.{POST, contentAsJson, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsJson}
import uk.gov.hmrc.vapingstampsapi.controllers.actions.{AuthAction, StubAuthAction}
import uk.gov.hmrc.vapingstampsapi.utils.WiremockHelper

import scala.concurrent.Future

class ApprovalControllerISpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with WiremockHelper:

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(
      "microservice.services.eis.port" -> server.port()
    )
    .overrides(
      bind[AuthAction].to[StubAuthAction]
    )
    .build()

  "POST /status" must {

    val defaultHeaders: Seq[(String, String)] = Seq(
      "Content-Type"  -> "application/json",
      "Accept"        -> "application/vnd.hmrc.1.0+json",
      "Authorization" -> "Bearer 123"
    )

    "return 200 with Summary data" in {
      val requestBody =
        """
          |{
          |  "vdsEmail": "example@email.com",
          |  "stampsReferenceNumber": "GBVA0000001DS"
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
          |""".stripMargin

      val expectedResponse =
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
          |""".stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(defaultHeaders*)
        .withJsonBody(Json.parse(requestBody))

      stubEndpointForPost(200, requestBody, responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(contentAsJson) mustBe Some(Json.parse(expectedResponse))
      response.map(status) mustBe Some(OK)
    }

    "return 200 with Summary data including all optional fields" in {
      val requestBody =
        """
          |{
          |  "vdsEmail": "example@email.com",
          |  "stampsReferenceNumber": "GBVA0000200DS"
          |}
          |""".stripMargin

      val responseBody =
        """
          |{
          |  "approvalStatus": "APPROVED",
          |  "businessName": "Acme Vaping Ltd",
          |  "addressLine1": "1 Business Park",
          |  "addressLine2": "London",
          |  "addressLine3": "Greater London",
          |  "addressLine4": "United Kingdom",
          |  "addressLine5": "Europe",
          |  "postCode": "SW1A 1AA",
          |  "contactName": "John Smith",
          |  "telephoneNumber": "02071234567",
          |  "stampsThreshold": 10000
          |}
          |""".stripMargin

      val expectedResponse =
        """
          |{
          |  "approvalStatus": "APPROVED",
          |  "businessName": "Acme Vaping Ltd",
          |  "addressLine1": "1 Business Park",
          |  "addressLine2": "London",
          |  "addressLine3": "Greater London",
          |  "addressLine4": "United Kingdom",
          |  "addressLine5": "Europe",
          |  "postCode": "SW1A 1AA",
          |  "contactName": "John Smith",
          |  "telephoneNumber": "02071234567",
          |  "stampsThreshold": 10000
          |}
          |""".stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(defaultHeaders*)
        .withBody(requestBody)

      stubEndpointForPost(200, requestBody, responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(OK)
      response.map(contentAsJson) mustBe Some(Json.parse(expectedResponse))
    }

    "return 200 with Summary data with only mandatory fields" in {
      val requestBody =
        """
          |{
          |  "vdsEmail": "example@email.com",
          |  "stampsReferenceNumber": "XIVA0000200DS"
          |}
          |""".stripMargin

      val responseBody =
        """
          |{
          |  "approvalStatus": "APPROVED",
          |  "businessName": "Acme Vaping Ltd",
          |  "addressLine1": "1 Business Park",
          |  "postCode": "SW1A 1AA",        
          |  "stampsThreshold": 10000
          |}
          |""".stripMargin

      val expectedResponse =
        """
          |{
          |  "approvalStatus": "APPROVED",
          |  "businessName": "Acme Vaping Ltd",
          |  "addressLine1": "1 Business Park",
          |  "postCode": "SW1A 1AA",
          |  "stampsThreshold": 10000
          |}
          |""".stripMargin

      val request = FakeRequest(POST, "/status/")
        .withHeaders(defaultHeaders*)
        .withBody(requestBody)

      stubEndpointForPost(200, requestBody, responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(OK)
      response.map(contentAsJson) mustBe Some(Json.parse(expectedResponse))
    }

    "return 200 with Not Approved" in {
      val requestBody =
        """
          |{
          |  "vdsEmail": "wrongExample@email.com",
          |  "stampsReferenceNumber": "XIVA0000200DS"
          |}
          |""".stripMargin

      val responseBody =
        """
          |{
          |  "approvalStatus": "NOT_APPROVED",
          |  "businessName": "Acme Vaping Ltd",
          |  "addressLine1": "1 Business Park",
          |  "postCode": "SW1A 1AA",
          |  "stampsThreshold": 10000
          |}
          |""".stripMargin

      val expectedResponse =
        """
          |{
          |  "approvalStatus": "NOT_APPROVED"
          |  }
          |""".stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(defaultHeaders*)
        .withBody(requestBody)

      stubEndpointForPost(200, requestBody, responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(OK)
      response.map(contentAsJson) mustBe Some(Json.parse(expectedResponse))

    }

    "return 400 error when invalid Reference Number is passed" in {
      val requestBody =
        """
          |{
          |  "vdsEmail": "example@email.com",
          |  "stampsReferenceNumber": "AAAA0000200"
          |}
          |""".stripMargin

      val responseBody =
        """
          |{
          |  "code": "BAD_REQUEST",
          |  "message": "The request is invalid",
          |  "errors": ["006"]
          |}
          |""".stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(defaultHeaders*)
        .withBody(requestBody)

      stubEndpointForPost(400, requestBody, responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(BAD_REQUEST)
      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
    }

    "return 404 when request is made to url that doesn't exist" in {
      val requestBody =
        """
          |{
          |  "vdsEmail": "example@email.com",
          |  "stampsReferenceNumber": "GBVA0000200DS"
          |}
          |""".stripMargin

      val responseBody =
        """
          |{
          |  "statusCode": 404,
          |  "message": "URI not found",
          |  "requested": "/url-not-in-service"
          |}
          |""".stripMargin

      val request = FakeRequest(POST, "/url-not-in-service")
        .withHeaders(defaultHeaders*)
        .withBody(requestBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(NOT_FOUND)
      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
    }

    "return 422 error when downstream Business error with 422 status is returned" in {
      val requestBody =
        """
          |{
          |  "vdsEmail": "example@email.com",
          |  "stampsReferenceNumber": "GBVA0000200DS"
          |}
          |""".stripMargin

      val responseBody =
        """
          |{
          |  "datetime": "2021-12-17T09:30:47Z",
          |  "errorCode": ["001"],
          |  "errorMessage": "Business validation failure."
          |}
          |""".stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(defaultHeaders*)
        .withBody(requestBody)

      stubEndpointForPost(422, requestBody, responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(UNPROCESSABLE_ENTITY)
      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
    }

    "return 500 error when downstream error is returned" in {
      val requestBody =
        """
          |{
          |  "vdsEmail": "example@email.com",
          |  "stampsReferenceNumber": "GBVA0000200DS"
          |}
          |""".stripMargin

      val responseBody =
        """
          |{
          |  "datetime": "2021-12-17T09:30:47Z",
          |  "message": "An unexpected error occurred while processing the request."
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
