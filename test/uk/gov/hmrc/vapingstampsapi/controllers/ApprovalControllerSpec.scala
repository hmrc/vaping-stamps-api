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

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import play.api.test.*
import play.api.test.Helpers.*

class ApprovalControllerSpec extends AnyWordSpec with Matchers {

  private val controller =
    new ApprovalController(Helpers.stubControllerComponents())

  "ApprovalController.retrieveSummary" should {

    "return 200 OK for a valid approval ID" in {
      val approvalId = "AAAA0000200BB"

      val request = FakeRequest(GET, s"/status/$approvalId/summary")
      val result = controller.retrieveSummary(approvalId).apply(request)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsString(result) must include(approvalId)
    }

    "return OK even if approval ID format varies" in {
      val approvalId = "AAAA0000204BB"

      val request = FakeRequest(GET, s"/status/$approvalId/summary")
      val result = controller.retrieveSummary(approvalId).apply(request)

      status(result) mustBe OK
    }

    "return full approval summary payload for a valid approval ID" in {
      val approvalId = "AAAA0000200BB"

      val request = FakeRequest(GET, s"/status/$approvalId/summary")
      val result = controller.retrieveSummary(approvalId).apply(request)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")

      val json = contentAsJson(result)

      (json \ "approvalStatus").as[String] mustBe "APPROVED"
      (json \ "businessName").as[String] mustBe "Acme Vaping Ltd"
      (json \ "registeredBusinessAddress").as[String] mustBe "1 Business Park, London, SW1A 1AA"
      (json \ "correspondenceAddress").as[String] mustBe "PO Box 123, London, SW1A 2BB"
      (json \ "contactName").as[String] mustBe "John Smith"
      (json \ "contactTelephone").as[String] mustBe "02071234567"
      (json \ "contactEmail").as[String] mustBe "john.smith@acmevaping.co.uk"
      (json \ "approvalNumber").as[String] mustBe approvalId
      (json \ "stampThreshold").as[Long] mustBe 10000L
    }
  }
}
