/**
 * @license
 * Visual Blocks Editor
 *
 * Copyright 2012 Google Inc.
 * https://developers.google.com/blockly/
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
 * @fileoverview Logic blocks for Blockly.
 * @author q.neutron@gmail.com (Quynh Neutron)
 */
'use strict';

goog.provide('Blockly.Blocks.oobd');

goog.require('Blockly.Blocks');


Blockly.Blocks['oobd_menu'] = {
  init: function() {
    this.setHelpUrl('http://www.example.com/');
    this.setColour(330);
    this.appendDummyInput()
        .appendField("OOBD Menu");
    this.appendValueInput("menuTitle")
        .setCheck("String")
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendField("Title");
    this.appendStatementInput("inner");
    this.setInputsInline(true);
    this.setPreviousStatement(true, "null");
    this.setNextStatement(true, "null");
    this.setTooltip('');
  }
};

Blockly.Blocks['oobd_item'] = {
  init: function() {
    this.setHelpUrl('http://www.example.com/');
    this.setColour(15);
    this.appendDummyInput()
        .appendField("OOBD Menuitem");
    this.appendDummyInput()
        .appendField("Content")
        .appendField(new Blockly.FieldTextInput("Field Text"), "content");
    this.appendDummyInput()
        .appendField("Flags")
        .appendField(new Blockly.FieldDropdown([["none", "0x00"], ["Update", "0x01"], ["Timer", "0x02"], ["Upd. & Tim.", "0x03"]]), "flags");
    this.appendStatementInput("NAME")
        .appendField("id");
    this.appendValueInput("mcaller")
        .setCheck("mCall")
        .appendField("MenuCall");
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};

Blockly.Blocks['oobd_mcall'] = {
  init: function() {
    this.setHelpUrl('http://www.example.com/');
    this.setColour(15);
    this.appendDummyInput()
        .appendField("OOBD MenuCall")
        .appendField(new Blockly.FieldTextInput("CallName"), "CallName");
    this.appendStatementInput("NAME");
    this.setInputsInline(true);
    this.setTooltip('');
  }
};

Blockly.Blocks['oobd_service'] = {
  init: function() {
    this.setHelpUrl('http://www.example.com/');
    this.setColour(15);
    this.appendDummyInput()
        .appendField("UDS Service");
    this.appendValueInput("service")
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendField("Service-ID");
    this.appendValueInput("params")
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendField("Parameter");
    this.appendStatementInput("NAME");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};