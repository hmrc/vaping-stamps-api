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
import uk.gov.hmrc.vapingstampsapi.models.errors.{ServiceUnavailableApiError, StampsReferenceNumberNotFound, UnprocessableEntityApiError}
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

    def incomingRequestBody(email: String, stampsReferenceNumber: String): String =
      s"""
         |{
         |  "vdsEmail": "$email",
         |  "stampsReferenceNumber": "$stampsReferenceNumber"
         |}
         |""".stripMargin

    def outGoingRequestBody(email: String, stampsReferenceNumber: String): String =
      s"""
         |{
         |  "vdsdetails": {
         |     "vdsEmail": "$email",
         |     "stampsReferenceNumber": "$stampsReferenceNumber"
         |  }
         |}
         |""".stripMargin

    "return 200 with Summary data" in {
      val responseBody =
        """
          |{
          |  "approvalStatus": "approved",
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
        .withJsonBody(Json.parse(incomingRequestBody("example@email.com", "GBVC0000001DS")))

      stubEndpointForPost(200, outGoingRequestBody("example@email.com", "GBVC0000001DS"), responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(contentAsJson) mustBe Some(Json.parse(expectedResponse))
      response.map(status) mustBe Some(OK)
    }

    "return 200 with Summary data including all optional fields" in {
      val responseBody =
        """
          |{
          |  "approvalStatus": "approved",
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
        .withJsonBody(Json.parse(incomingRequestBody("example@email.com", "GBVC0000200DS")))

      stubEndpointForPost(200, outGoingRequestBody("example@email.com", "GBVC0000200DS"), responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(OK)
      response.map(contentAsJson) mustBe Some(Json.parse(expectedResponse))
    }

    "return 200 with Summary data with only mandatory fields" in {
      val responseBody =
        """
          |{
          |  "approvalStatus": "approved",
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
        .withJsonBody(Json.parse(incomingRequestBody("example@email.com", "XIVC0000200DS")))

      stubEndpointForPost(200, outGoingRequestBody("example@email.com", "XIVC0000200DS"), responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(OK)
      response.map(contentAsJson) mustBe Some(Json.parse(expectedResponse))
    }

    "return 200 with Not Approved" in {
      val responseBody =
        """
          |{
          |  "approvalStatus": "not approved",
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
        .withJsonBody(Json.parse(incomingRequestBody("example@email.com", "XIVC0000200DS")))

      stubEndpointForPost(200, outGoingRequestBody("example@email.com", "XIVC0000200DS"), responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(OK)
      response.map(contentAsJson) mustBe Some(Json.parse(expectedResponse))

    }

    "return 400 error when invalid Reference Number, email, accept header and content type header is passed" in {
      val responseBody =
        """
          |{
          |  "code": "BAD_REQUEST",
          |  "message": "The request is invalid",
          |  "errors": ["002", "004", "006", "008"]
          |}
          |""".stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(
          Seq(
            "Content-Type"  -> "text/json",
            "Accept"        -> "application/vnd",
            "Authorization" -> "Bearer 123"
          )*
        )
        .withJsonBody(Json.parse(incomingRequestBody("", "")))

      stubEndpointForPost(400, outGoingRequestBody("", ""), responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(BAD_REQUEST)
      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
    }

    "return 400 error when Missing a Reference Number and an email" in {
      val responseBody =
        """
          |{
          |  "code": "BAD_REQUEST",
          |  "message": "The request is invalid",
          |  "errors": ["005", "007"]
          |}
          |""".stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(
          Seq(
            "Content-Type"  -> "application/json",
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer 123"
          )*
        )
        .withJsonBody(Json.parse("""
                                   |{
                                   |}
                                   |""".stripMargin))

      stubEndpointForPost(
        400,
        """
          |{
          |  "vdsdetails": {
          |  }
          |}
          |""".stripMargin,
        responseBody
      )

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(BAD_REQUEST)
      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
    }

    "return 400 error when an invalid for an excessively long vdsEmail is passed" in {
      val responseBody =
        """
          |{
          |  "code": "BAD_REQUEST",
          |  "message": "The request is invalid",
          |  "errors": ["009"]
          |}
          |""".stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(defaultHeaders*)
        .withJsonBody(
          Json.parse(
            incomingRequestBody(
              "example123456789123456789123456789123456789123456789123456789123456789@email123456789123456789123456789123456789123456789123456789123456789.com",
              "XIVC0000200DS"
            )
          )
        )

      stubEndpointForPost(400, outGoingRequestBody("example@email.com", "XIVC0000200DS"), responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(BAD_REQUEST)
      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
    }

    "return 404 when request is made to url that doesn't exist" in {
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
        .withJsonBody(Json.parse(incomingRequestBody("example@email.com", "GBVC0000200DS")))

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(NOT_FOUND)
      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
    }

    "return 415 error when content header is missing" in {
      val responseBody =
        """
          |{
          |  "statuscode": 415,
          |  "message": "Expecting text/json or application/json body"
          |}
          |""".stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(
          Seq(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer 123"
          )*
        )
        .withJsonBody(Json.parse(incomingRequestBody("example@email.com", "GBVC0000200DS")))

      stubEndpointForPost(415, outGoingRequestBody("example@email.com", "GBVC0000200DS"), responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(contentAsJson) mustBe Some(Json.parse(responseBody))
      response.map(status) mustBe Some(UNSUPPORTED_MEDIA_TYPE)
    }

    "return 422 error when downstream Business error with 422 status is returned" in {
      val responseBody =
        """
          |{
          |  "errorDetail": {
          |    "errorCode": "422",
          |    "errorMessage": "Unprocessable Entity",
          |    "source": "Backend",
          |    "sourceFaultDetail": {
          |      "detail": [
          |        "001"
          |      ]
          |    },
          |    "timestamp": "2026-04-14T10:54:12Z",
          |    "correlationId": "1ae81b45-41b4-4642-ae1c-db1126900001"
          |  }
          |}
          |""".stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(defaultHeaders*)
        .withJsonBody(Json.parse(incomingRequestBody("example@email.com", "GBVC0000200DS")))

      stubEndpointForPost(422, outGoingRequestBody("example@email.com", "GBVC0000200DS"), responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(UNPROCESSABLE_ENTITY)
      response.map(contentAsJson) mustBe Some(
        Json.toJson(UnprocessableEntityApiError(errors = Seq(StampsReferenceNumberNotFound)))
      )
    }

    "return 503 error when downstream error is returned" in {
      val responseBody =
        """
          |{
          |  "errorDetail": {
          |    "errorCode": "500",
          |    "errorMessage": "Internal Server Error",
          |    "source": "Backend",
          |    "timestamp": "2026-04-14T10:54:12Z",
          |    "correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e"
          |  }
          |}
          |""".stripMargin

      val request = FakeRequest(POST, "/status")
        .withHeaders(defaultHeaders*)
        .withJsonBody(Json.parse(incomingRequestBody("example@email.com", "GBVC0000200DS")))

      stubEndpointForPost(500, outGoingRequestBody("example@email.com", "GBVC0000200DS"), responseBody)

      val response: Option[Future[Result]] = route(app, request)

      response.map(status) mustBe Some(SERVICE_UNAVAILABLE)
      response.map(contentAsJson) mustBe Some(
        Json.toJson(ServiceUnavailableApiError(message = "Error has occurred in downstream service"))
      )
    }
  }
