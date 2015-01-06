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

Blockly.Lua['oobd_item'] = function(block) {
  var text_content = block.getFieldValue('content');
  var dropdown_flags = block.getFieldValue('flags');
  var statements_name = Blockly.Lua.statementToCode(block, 'NAME');
  var value_mcaller = Blockly.Lua.valueToCode(block, 'mcaller', Blockly.Lua.ORDER_ATOMIC);
  // TODO: Assemble Lua into code variable.
  var code = '...';
  return code;
};

Blockly.Lua['oobd_mcall'] = function(block) {
  var text_callname = block.getFieldValue('CallName');
  var statements_name = Blockly.Lua.statementToCode(block, 'NAME');
  // TODO: Assemble Lua into code variable.
  var code = '...';
  return code;
};

Blockly.Lua['oobd_service'] = function(block) {
  var value_service = Blockly.Lua.valueToCode(block, 'service', Blockly.Lua.ORDER_ATOMIC);
  var value_params = Blockly.Lua.valueToCode(block, 'params', Blockly.Lua.ORDER_ATOMIC);
  var statements_name = Blockly.Lua.statementToCode(block, 'NAME');
  // TODO: Assemble Lua into code variable.
  var code = '...';
  return code;
};