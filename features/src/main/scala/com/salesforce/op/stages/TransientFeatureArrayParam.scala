/*
 * Copyright (c) 2017, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.op.stages

import com.salesforce.op.features._
import org.apache.spark.ml.param._
import org.apache.spark.ml.util.Identifiable
import org.json4s.DefaultFormats
import org.json4s.JsonAST.{JArray, JValue}
import org.json4s.jackson.JsonMethods.{compact, parse, render}

import scala.util.{Failure, Success}


/**
 * Transient array param for storing input features for stages
 */
private[stages] class TransientFeatureArrayParam
(
  parent: String,
  name: String,
  doc: String,
  isValid: Array[TransientFeature] => Boolean
) extends Param[Array[TransientFeature]](parent, name, doc, isValid) {

  @transient implicit val formats = DefaultFormats

  def this(parent: String, name: String, doc: String) =
    this(parent, name, doc, (_: Array[TransientFeature]) => true)

  def this(parent: Identifiable, name: String, doc: String, isValid: Array[TransientFeature] => Boolean) =
    this(parent.uid, name, doc, isValid)

  def this(parent: Identifiable, name: String, doc: String) = this(parent.uid, name, doc)

  /** Creates a param pair with the given value (for Java). */
  override def w(value: Array[TransientFeature]): ParamPair[Array[TransientFeature]] = super.w(value)

  override def jsonEncode(value: Array[TransientFeature]): String = {
    compact(render(JArray(value.map(_.toJson).toList)))
  }

  override def jsonDecode(json: String): Array[TransientFeature] = {
    parse(json).extract[Array[JValue]].map(obj => {
      TransientFeature(obj) match {
        case Failure(e) => throw new RuntimeException("Failed to parse TransientFeature", e)
        case Success(v) => v
      }
    })
  }
}
