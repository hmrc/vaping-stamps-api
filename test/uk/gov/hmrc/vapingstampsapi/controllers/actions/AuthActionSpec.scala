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

package uk.gov.hmrc.vapingstampsapi.controllers.actions

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.*
import org.scalatestplus.play.*
import play.api.mvc.*
import play.api.mvc.Results.*
import play.api.test.*
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.AuthProvider.StandardApplication
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{applicationId, applicationName}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthConnector, AuthProviders, AuthorisationException}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends PlaySpec with Results {
  given headerCarrier: HeaderCarrier = HeaderCarrier()
  given ec: ExecutionContext = ExecutionContext.Implicits.global

  private val mockAuthConnector: AuthConnector = mock[AuthConnector]
  private val mockCc: ControllerComponents = stubControllerComponents()
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  val testContent = "test-application-name, application-id"

  val errorMsg = (field: String) =>
    s"{\"statusCode\":401,\"message\":\"Application credentials invalid: Unable to retrieve Application $field from Auth service.\"}"

  val testBlock: Request[_] => Future[Result] = { _ =>
    Future(Ok(testContent))
  }

  // Unit under test
  private val authAction = new AuthActionImpl(mockAuthConnector, mockCc)

  "AuthActionSpec#invokeBlock" should {
    "execute authorisation retrieval and return OK if authorised" in {

      when(
        mockAuthConnector.authorise(
          eqTo(
            AuthProviders(StandardApplication)
          ),
          eqTo(
            applicationName and applicationId
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(Some("test-application-name"), Some("application-id"))))

      val result: Future[Result] = authAction.invokeBlock(fakeRequest, testBlock)

      status(result) mustBe OK
      contentAsString(result) mustBe testContent

    }

    "execute authorisation retrieval and throw AuthorisationException if cannot get the application name" in {

      when(
        mockAuthConnector.authorise(
          eqTo(
            AuthProviders(StandardApplication)
          ),
          eqTo(
            applicationName and applicationId
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(None, Some("application-id"))))

      val result: Future[Result] = authAction.invokeBlock(fakeRequest, testBlock)

      status(result) mustBe UNAUTHORIZED
      contentAsString(
        result
      ) mustBe errorMsg("Name")
    }

    "execute authorisation retrieval and throw AuthorisationException if cannot get the application id" in {

      when(
        mockAuthConnector.authorise(
          eqTo(
            AuthProviders(StandardApplication)
          ),
          eqTo(
            applicationName and applicationId
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(Some("application-name"), None)))

      val result: Future[Result] = authAction.invokeBlock(fakeRequest, testBlock)

      status(result) mustBe UNAUTHORIZED
      contentAsString(
        result
      ) mustBe errorMsg("ID")
    }

    "execute authorisation retrieval and throw AuthorisationException because of some other reason" in {

      when(
        mockAuthConnector.authorise(
          eqTo(
            AuthProviders(StandardApplication)
          ),
          eqTo(
            applicationName and applicationId
          )
        )(any(), any())
      )
        .thenReturn(Future.failed(AuthorisationException.fromString("other")))

      val result: Future[Result] = authAction.invokeBlock(fakeRequest, testBlock)

      status(result) mustBe UNAUTHORIZED
      println(s"EXXX: ${contentAsString(result)}")
      contentAsString(
        result
      ) mustBe """{"statusCode":401,"message":"other"}"""
    }

  }
}
