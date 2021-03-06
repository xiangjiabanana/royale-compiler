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
 * NoConflictNoWindow
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('NoConflictNoWindow');

goog.require('mypackage.TestClass');



/**
 * @constructor
 */
NoConflictNoWindow = function() {
  this.testClass = new mypackage.TestClass();
};


/**
 * Prevent renaming of class. Needed for reflection.
 */
goog.exportSymbol('NoConflictNoWindow', NoConflictNoWindow);


/**
 * @private
 * @type {mypackage.TestClass}
 */
NoConflictNoWindow.prototype.testClass = null;


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
NoConflictNoWindow.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'NoConflictNoWindow', qName: 'NoConflictNoWindow', kind: 'class' }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
NoConflictNoWindow.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    variables: function () {return {};},
    accessors: function () {return {};},
    methods: function () {
      return {
        'NoConflictNoWindow': { type: '', declaredBy: 'NoConflictNoWindow'}
      };
    }
  };
};
/**
 * @export
 * @const
 * @type {number}
 */
NoConflictNoWindow.prototype.ROYALE_REFLECTION_INFO.compileFlags = 9;
