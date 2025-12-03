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

package uk.gov.hmrc.vapingstampsapi.service

import uk.gov.hmrc.mongo.MongoComponent
import org.mongodb.scala._
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HealthService @Inject()(
                                mongoComponent: MongoComponent
                              )(using ec: ExecutionContext):

  /** Performs a ping command to validate Mongo connectivity. */
  def check(): Future[Boolean] =
    val command = org.mongodb.scala.bson.Document("ping" -> 1)

    mongoComponent.database.runCommand(command).toFuture
      .map(_ => true)
      .recover(_ => false)
