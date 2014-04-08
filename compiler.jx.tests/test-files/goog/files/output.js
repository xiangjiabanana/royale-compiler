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
goog.provide('org.apache.flex.A');

goog.require('flash.events.IEventDispatcher');
goog.require('spark.components.Button');

/**
 * @constructor
 * @extends {spark.components.Button}
 * @implements {flash.events.IEventDispatcher}
 */
org.apache.flex.A = function() {
	var self = this;
	goog.base(this);
	self.trace(typeof("a"));
}
goog.inherits(org.apache.flex.A, spark.components.Button);

/**
 * @const
 * @type {string}
 */
org.apache.flex.A.MY_CLASS_CONST = "myClassConst";

/**
 * @private
 * @type {ArgumentError}
 */
org.apache.flex.A.prototype._a = new ArgumentError();

/**
 * @const
 * @type {string}
 */
org.apache.flex.A.prototype.MY_INSTANCE_CONST = "myInstanceConst";