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

import cats.data.Validated.{Invalid, Valid}
import com.google.inject.{ImplementedBy, Inject}
import play.api.libs.json.Json
import play.api.mvc.Results.BadRequest
import play.api.mvc.{ActionFilter, Request, Result}
import uk.gov.hmrc.vapingstampsapi.models.ApprovalRequest
import uk.gov.hmrc.vapingstampsapi.models.errors.{BadRequestApiError, BadRequestError}
import uk.gov.hmrc.vapingstampsapi.validators.RequestValidator

import scala.concurrent.{ExecutionContext, Future}

class ValidateRequestActionImpl @Inject() (val executionContext: ExecutionContext, validator: RequestValidator)
    extends ValidateRequestAction:

  override def filter[A](request: Request[A]): Future[Option[Result]] =
    validator.fromRequest(request) match {
      case Valid(a)   => Future.successful(None)
      case Invalid(e) =>
        val convertChain = e.toNonEmptyList.toList
        Future.successful(
          Some(
            BadRequest(
              Json.toJson(BadRequestApiError(errors = convertChain))
            )
          )
        )
    }

@ImplementedBy(classOf[ValidateRequestActionImpl])
trait ValidateRequestAction extends ActionFilter[Request]
