/*
 * *
 *  * Copyright 2012 Martin Gontovnikas (martin at gonto dot com dot ar) - twitter: @mgonto
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package ar.com.gonto.factorypal.objects

import reflect.macros.Context
import ar.com.gonto.factorypal.fields.FieldBuilder
import scala.language.experimental.macros
import scala.language.dynamics


/**
 * TODO: Add a comment
 * @author mgonto
 * Created Date: 12/10/12
 */
class ObjectBuilder[O] extends Dynamic {
  def selectDynamic[T](propName: String) = new FieldBuilder[O, Int]("name") //macro ObjectBuilder.createFieldBuilder[O]
}

object ObjectBuilder {

  def apply[O]() = new ObjectBuilder[O]

  def createFieldBuilder[O: c.WeakTypeTag](c: Context)(propName: c.Expr[String]) = {
    import c.universe._

    def doesntCompile(reason: String) = c.abort(c.enclosingPosition, reason)

    val propertyName = propName.tree match {
      case (Literal(Constant(propertyName: String))) => propertyName
      case _ => doesntCompile("The property isn't a string literal")
    }

    val objectType = c.weakTypeOf[O]

    val fieldMember = objectType.member(newTermName(propertyName)) orElse {
      doesntCompile(s"The property $propertyName isn't a field of $objectType")
    }

    val fieldMemberType = fieldMember.typeSignatureIn(objectType) match {
      case NullaryMethodType(tpe)   => tpe
      case _                      => doesntCompile(s"$propertyName isn't a field, it must be another thing")
    }

    c.Expr(
      Apply(
        Select(
          New(
            AppliedTypeTree(
              Ident(newTypeName("FieldBuilder")),
              List(Ident(newTypeName("objectType")), Ident(newTypeName("fieldMemberType")))
            )
          ),
          nme.CONSTRUCTOR
        ),
        List(
          Literal(
            Constant(
              "gonto"
            )
          )
        )
      )
    )


  }

}