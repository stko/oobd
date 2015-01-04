/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://blockly.googlecode.com/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Generating Lua for logic blocks.
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
'use strict';

goog.provide('Blockly.Lua.oobd');

goog.require('Blockly.Lua');


Blockly.Lua['oobd_menu'] = function(block) {
  var value_menutitle = Blockly.Lua.valueToCode(block, 'menuTitle', Blockly.Lua.ORDER_ATOMIC);
  var statements_inner = Blockly.Lua.statementToCode(block, 'inner');
  // TODO: Assemble Lua into code variable.
  var code = '...';
  return code;
};

