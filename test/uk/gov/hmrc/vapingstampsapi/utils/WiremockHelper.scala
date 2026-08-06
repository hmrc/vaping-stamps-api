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

package uk.gov.hmrc.vapingstampsapi.utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

trait WiremockHelper extends BeforeAndAfterAll with BeforeAndAfterEach {
  this: Suite =>

  protected val server: WireMockServer =
    new WireMockServer(wireMockConfig().dynamicPort())

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  def stubEndpointForPost(status: Int, requestBody: String, responseBody: String): StubMapping =
    server.stubFor(
      post(urlEqualTo("/excise/decision/vapingstamps/profile/v1"))
        .withRequestBody(equalToJson(requestBody))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
        )
    )

  def stubDelete(url: String, status: Int, body: String = ""): StubMapping =
    server.stubFor(
      delete(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  def stubDeleteFault(url: String): StubMapping =
    server.stubFor(
      delete(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(499)
            .withHeader("Content-Type", "application/json", "Accept", "date", "x-correlation-id", "x-forwarded-host")
            .withBody("The request payload has timed out")
            .withFixedDelay(2000)
        )
    )

  def verifyDelete(url: String): Unit =
    server.verify(deleteRequestedFor(urlEqualTo(url)))

  def verifyDeleteWithoutRetry(url: String): Unit =
    server.verify(1, deleteRequestedFor(urlEqualTo(url)))

  def verifyDeleteWithRetry(url: String): Unit =
    server.verify(2, deleteRequestedFor(urlEqualTo(url)))

  def verifyDeleteWithAuthToken(url: String, token: String): Unit =
    server.verify(
      deleteRequestedFor(urlEqualTo(url))
        .withHeader("Authorization", equalTo(s"Bearer $token"))
    )
}
