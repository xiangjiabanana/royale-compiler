/**
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
/**
 * OtherClass
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('OtherClass');



/**
 * @constructor
 */
OtherClass = function() {
};


/**
 * Prevent renaming of class. Needed for reflection.
 */
goog.exportSymbol('OtherClass', OtherClass);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
OtherClass.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'OtherClass', qName: 'OtherClass', kind: 'class' }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
OtherClass.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    variables: function () {return {};},
    accessors: function () {return {};},
    methods: function () {
      return {
        'OtherClass': { type: '', declaredBy: 'OtherClass'}
      };
    }
  };
};
/**
 * @export
 * @const
 * @type {number}
 */
OtherClass.prototype.ROYALE_REFLECTION_INFO.compileFlags = 9;
