/*
 * Copyright 2025 HM Revenue & Customs
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


import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.vapingstampsapi.service.HealthService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class HealthController @Inject()(
                                  cc: ControllerComponents,
                                  healthService: HealthService
                                )(using ec: ExecutionContext)
  extends BackendController(cc):

  val health: Action[AnyContent] =
    Action.async { implicit request =>
      healthService.check().map {
        case true => Ok("""{"status":"UP"}""")
        case false => ServiceUnavailable("""{"status":"DOWN","error":"Mongo unreachable"}""")
      }
    }
