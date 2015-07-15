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
 * controllers.MyController
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('controllers.MyController');



/**
 * @constructor
 * @implements {org.apache.flex.core.IDocument}
 * @param {org.apache.flex.core.Application=} app
 */
controllers.MyController = function(app) {
  app = typeof app !== 'undefined' ? app : null;
  if (app) {
    this.app = org.apache.flex.utils.Language.as(app, FlexJSTest_again);
    app.addEventListener("viewChanged", goog.bind(this.viewChangeHandler, this));
  }
};


/**
 * @private
 * @type {string}
 */
controllers.MyController.prototype.queryBegin = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22";


/**
 * @private
 * @type {string}
 */
controllers.MyController.prototype.queryEnd = "%22)%0A%09%09&env=http%3A%2F%2Fdatatables.org%2Falltables.env&format=json";


/**
 * @private
 * @type {FlexJSTest_again}
 */
controllers.MyController.prototype.app;


/**
 * @private
 * @param {org.apache.flex.events.Event} event
 */
controllers.MyController.prototype.viewChangeHandler = function(event) {
  this.app.initialView.addEventListener("buttonClicked", goog.bind(this.buttonClickHandler, this));
  this.app.initialView.addEventListener("listChanged", goog.bind(this.listChangedHandler, this));
  this.app.initialView.addEventListener("cityListChanged", goog.bind(this.cityListChangeHandler, this));
  this.app.initialView.addEventListener("transferClicked", goog.bind(this.transferClickHandler, this));
  this.app.initialView.addEventListener("comboBoxChanged", goog.bind(this.comboBoxChangeHandler, this));
};


/**
 * @private
 * @param {org.apache.flex.events.Event} event
 */
controllers.MyController.prototype.buttonClickHandler = function(event) {
  var /** @type {string} */ sym = org.apache.flex.utils.Language.as(this.app.initialView, MyInitialView, true).symbol;
  this.app.service.url = this.queryBegin + sym + this.queryEnd;
  this.app.service.send();
  this.app.service.addEventListener("complete", goog.bind(this.completeHandler, this));
};


/**
 * @private
 * @param {org.apache.flex.events.Event} event
 */
controllers.MyController.prototype.completeHandler = function(event) {
  org.apache.flex.utils.Language.as(this.app.model, models.MyModel, true).labelText = org.apache.flex.utils.Language.as(this.app.collection.getItemAt(0), String);
};


/**
 * @private
 * @param {org.apache.flex.events.Event} event
 */
controllers.MyController.prototype.listChangedHandler = function(event) {
  org.apache.flex.utils.Language.as(this.app.model, models.MyModel, true).labelText = org.apache.flex.utils.Language.as(this.app.initialView, MyInitialView, true).symbol;
};


/**
 * @private
 * @param {org.apache.flex.events.Event} event
 */
controllers.MyController.prototype.cityListChangeHandler = function(event) {
  org.apache.flex.utils.Language.as(this.app.model, models.MyModel, true).labelText = org.apache.flex.utils.Language.as(this.app.initialView, MyInitialView, true).city;
};


/**
 * @private
 * @param {org.apache.flex.events.Event} event
 */
controllers.MyController.prototype.transferClickHandler = function(event) {
  org.apache.flex.utils.Language.as(this.app.model, models.MyModel, true).labelText = org.apache.flex.utils.Language.as(this.app.initialView, MyInitialView, true).inputText;
};


/**
 * @private
 * @param {org.apache.flex.events.Event} event
 */
controllers.MyController.prototype.comboBoxChangeHandler = function(event) {
  org.apache.flex.utils.Language.as(this.app.model, models.MyModel, true).labelText = org.apache.flex.utils.Language.as(this.app.initialView, MyInitialView, true).comboBoxValue;
};


/**
 * @export
 * @param {Object} document
 * @param {string=} id
 */
controllers.MyController.prototype.setDocument = function(document, id) {
  id = typeof id !== 'undefined' ? id : null;
  this.app = org.apache.flex.utils.Language.as(document, FlexJSTest_again);
  this.app.addEventListener("viewChanged", goog.bind(this.viewChangeHandler, this));
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
controllers.MyController.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'MyController', qName: 'controllers.MyController'}], interfaces: [org.apache.flex.core.IDocument] };

