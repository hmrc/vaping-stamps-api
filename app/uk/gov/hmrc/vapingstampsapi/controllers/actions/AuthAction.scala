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

import com.google.inject.Inject
import play.api.Logging
import play.api.http.Status.UNAUTHORIZED
import play.api.libs.json.Json
import play.api.mvc.*
import play.api.mvc.Results.Unauthorized
import uk.gov.hmrc.auth.core.AuthProvider.StandardApplication
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthConnector, AuthProviders, AuthorisationException, AuthorisedFunctions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait AuthAction extends AuthorisedFunctions with ActionBuilder[Request, AnyContent] with Logging

class AuthActionImpl @Inject() (
                                 override val authConnector: AuthConnector,
                                 controllerComponents: ControllerComponents
                               )(implicit val executionContext: ExecutionContext)
  extends AuthAction:

  val parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised(AuthProviders(StandardApplication)).retrieve(
      Retrievals.applicationName and
        Retrievals.applicationId
    ) { case applicationName ~ applicationId =>
      val applicationCredentials = for {
        applicationName <- applicationName.toRight("Unable to retrieve Application Name from Auth service.")
        applicationId   <- applicationId.toRight("Unable to retrieve Application ID from Auth service.")
      } yield (applicationName, applicationId)

      applicationCredentials match
        case Right(appName, appId) =>
          logger.info(
            s"[ApplicationAuth][authorised]: application credentials valid"
          )
          // TODO: compare authValues with app.conf encrypted values?
          block(request)
        case Left(error) =>
          logger.warn(s"[ApplicationAuth][authorised]: Application credentials invalid: $error")
          throw AuthorisationException.fromString(
            s"Application credentials invalid: $error"
          )
    } recover { case e: AuthorisationException =>
      logger.debug("Application failed authorisation: ", e)
      Unauthorized(Json.toJson(ErrorResponse(UNAUTHORIZED, e.reason)))
    }
