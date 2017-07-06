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


var oobdcolour = new Blockly.FieldColour('#ffff00');

Blockly.Blocks['oobd_main'] = {
  init: function() {
    this.setHelpUrl('http://www.oobd.org/doku.php?id=doc:tools_quickscript#the_main_menu');
    this.setColour(60);
    this.appendDummyInput()
        .appendField("Implement your main program here:");
    this.appendStatementInput("inner");
    this.setInputsInline(true);
    /*this.setDeletable(false);*/
    this.setTooltip('');
  }
};


Blockly.Blocks['oobd_menu'] = {
  init: function() {
    this.setHelpUrl('http://www.oobd.org/doku.php?id=doc:tools_quickscript#the_menu');
    this.setColour(60);
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
    this.setHelpUrl('http://www.oobd.org/doku.php?id=doc:tools_quickscript#the_menu_item');
    this.setColour(60);
    this.appendDummyInput()
        .appendField("OOBD Menuitem");
    this.appendDummyInput()
        .appendField("Content")
        .appendField(new Blockly.FieldTextInput("Description of this Value"), "content");
    this.appendDummyInput()
        .appendField("Icons")
        .appendField(new Blockly.FieldDropdown([["none", "0x00"], ["Update", "0x02"], ["Timer", "0x04"], ["Upd. & Timer", "0x06"], ["Submenu", "0x01"], ["Back", "0x10"]]), "Flags");
    this.appendValueInput("idinfo")
        .setCheck("String")
        .appendField("ID");
    this.appendValueInput("mcaller")
        .setCheck("String")
        .appendField("Related Function Name");
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};

Blockly.Blocks['oobd_mcall'] = {
  init: function() {
    this.setHelpUrl('http://www.oobd.org/doku.php?id=doc:tools_quickscript#the_item_procedure');
    this.setColour(60);
    this.appendDummyInput()
        .appendField("OOBD MenuCall")
        .appendField(new Blockly.FieldTextInput("CallName"), "CallName");
    this.appendStatementInput("NAME");
    this.setInputsInline(true);
    this.setTooltip('');
  }
};



Blockly.Blocks['oobd_setdongle'] = {
  init: function() {
    this.setHelpUrl('http://www.oobd.org/doku.php?id=doc:tools_quickscript#set_the_dongle');
    this.setColour(60);
    this.appendDummyInput()
        .appendField("Set Dongle to");
    this.appendDummyInput()
        .appendField("Bus Mode")
        .appendField(new Blockly.FieldDropdown([["Off", "0"], ["Listen", "2"], ["Active", "3"]]), "busMode");
    this.appendDummyInput()
        .appendField("Channel")
        .appendField(new Blockly.FieldDropdown([["HS-CAN", "HS-CAN"], ["MS-CAN", "MS-CAN"], ["500b11" , "500b11"], ["250b11" , "250b11"], ["125b11" , "125b11"], ["500b29" , "500b29"], ["250b29" , "250b29"], ["125b29" , "125b29"], ["500b11p2" , "500b11p2"], ["250b11p2" , "250b11p2"], ["125b11p2" , "125b11p2"], ["500b29p2" , "500b29p2"], ["250b29p2" , "250b29p2"], ["125b29p2" , "125b29p2"]]), "bus");
    this.appendDummyInput()
        .appendField("Protocol")
        .appendField(new Blockly.FieldDropdown([["UDS", "1"], ["Real Time Data", "3"]]), "protocol");
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};

Blockly.Blocks['oobd_setmodule'] = {
  init: function() {
    this.setHelpUrl('http://www.oobd.org/doku.php?id=doc:tools_quickscript#define_the_module');
    this.setColour(60);
    this.appendDummyInput()
        .appendField("Set Module to ");
    this.appendDummyInput()
        .appendField("ID (e.g. 7E0)")
        .appendField(new Blockly.FieldTextInput("7E0"), "moduleID");
    this.appendDummyInput()
        .appendField("Timeout (ms)")
        .appendField(new Blockly.FieldTextInput("50"), "moduleTimeout");
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};


Blockly.Blocks['oobd_requestservice'] = {
  init: function() {
    this.setHelpUrl('http://www.oobd.org/doku.php?id=doc:tools_quickscript#request_a_service');
    this.setColour(60);
    this.appendDummyInput()
        .appendField("Request Service ");
    this.appendValueInput("serviceID")
        .setCheck("String")
        .appendField("ID (hex)");
    this.appendValueInput("NAME")
        .setCheck("String")
        .appendField("Parameters (hex)");
    this.appendStatementInput("inner")
        .appendField("if success");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};


Blockly.Blocks['oobd_evalresult'] = {
  init: function() {
    this.setHelpUrl('http://www.oobd.org/doku.php?id=doc:tools_quickscript#evaluate_a_result');
    this.setColour(60);
   this.appendDummyInput()
        .appendField("Measure ");
    this.appendValueInput("serviceID")
        .setCheck("String")
        .appendField("Service (hex)");
    this.appendValueInput("NAME")
        .setCheck("String")
        .appendField("Parameters (hex)");
    this.appendDummyInput()
        .appendField("Type")
        .appendField(new Blockly.FieldDropdown([["ASCII", "ascii"], ["Bit", "bit"], ["Unsigned", "unsigned"], ["Signed", "Signed"]]), "type");
    this.appendValueInput("startbit")
        .setCheck("Number")
        .appendField("Startbit");
    this.appendValueInput("length")
        .setCheck("Number")
        .appendField("Length (Bits)");
    this.appendValueInput("offset")
        .setCheck("Number")
        .appendField("Offset");
    this.appendValueInput("mult")
        .setCheck("Number")
        .appendField("Multiplier");
    this.appendValueInput("Unit")
        .setCheck("String")
        .appendField("Unit");
    this.appendDummyInput()
        .appendField("(\"low-Value\" | \"High-Value\" for Bits)");
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};

