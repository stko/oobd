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
  var statements_name = Blockly.Lua.statementToCode(block, 'inner');
  var code ='openPage('+value_menutitle+')\n'
 	+statements_name
	+'pageDone()\n'
        ;
  return code;
};

Blockly.Lua['oobd_item'] = function(block) {
  var text_content = block.getFieldValue('content');
  var dropdown_flags = block.getFieldValue('Flags');
  var value_idinfo = Blockly.Lua.valueToCode(block, 'idinfo', Blockly.Lua.ORDER_ATOMIC);
  var value_mcaller = Blockly.Lua.valueToCode(block, 'mcaller', Blockly.Lua.ORDER_ATOMIC);
  var code ='addElement("'+text_content+'" , '+value_mcaller+', "-", '+dropdown_flags +', '+value_idinfo +')\n'
        ;  
  return code;
};

Blockly.Lua['oobd_mcall'] = function(block) {
  var text_callname = block.getFieldValue('CallName');
  var statements_name = Blockly.Lua.statementToCode(block, 'NAME');
  var code = 'function '+ text_callname +'(oldvalue,id)\n'
	+'local result = oldvalue\n'
	+statements_name
	+'return result\n'
	+'end\n'
        ;
  return code;
};


Blockly.Lua['oobd_setdongle'] = function(block) {
  var dropdown_busmode = block.getFieldValue('busMode');
  var dropdown_bus = block.getFieldValue('bus');
  var dropdown_protocol = block.getFieldValue('protocol');
  var code ='setDongleMode("'+dropdown_busmode+'" , "'+dropdown_bus+'", "'+dropdown_protocol +'")\n'
        ;  
  return code;
};

Blockly.Lua['oobd_setmodule'] = function(block) {
  var text_moduleid = block.getFieldValue('moduleID');
  var text_moduletimeout = block.getFieldValue('moduleTimeout');
 var code ='setModuleParams("'+text_moduleid+'" , '+text_moduletimeout +')\n'
        ;  
   return code;
};

Blockly.Lua['oobd_requestservice'] = function(block) {
  var value_serviceid = Blockly.Lua.valueToCode(block, 'serviceID', Blockly.Lua.ORDER_ATOMIC);
  var value_name = Blockly.Lua.valueToCode(block, 'NAME', Blockly.Lua.ORDER_ATOMIC);
  var statements_inner = Blockly.Lua.statementToCode(block, 'inner');
  var code = 'echoWrite('+ value_serviceid +'..' + value_name  +'.."\\r")\n'
	+'local udsLen=receive()\n'
	+'if udsLen>0 then\n'
	+'if udsBuffer[1]==tonumber('+ value_serviceid + ',16) + 0x40  then\n'
	+statements_inner
	+'elseif udsBuffer[1]== 0x7F then\n'
	+'return string.format(getLocalePrintf("nrc",string.format("0x%x",udsBuffer[3]), "NRC: 0x%02X"),udsBuffer[3])\n'
	+'else\n'
	+'return "Error"\n'
	+'end\n'
	+'else\n'
	+'return "NO DATA"\n'
	+'end\n'
        ;
  return code;
};


Blockly.Lua['oobd_evalresult'] = function(block) {
  var dropdown_type = block.getFieldValue('type');
  var value_startbit = Blockly.Lua.valueToCode(block, 'startbit', Blockly.Lua.ORDER_ATOMIC);
  var value_length = Blockly.Lua.valueToCode(block, 'length', Blockly.Lua.ORDER_ATOMIC);
  var value_offset = Blockly.Lua.valueToCode(block, 'offset', Blockly.Lua.ORDER_ATOMIC);
  var value_mult = Blockly.Lua.valueToCode(block, 'mult', Blockly.Lua.ORDER_ATOMIC);
  var value_unit = Blockly.Lua.valueToCode(block, 'Unit', Blockly.Lua.ORDER_ATOMIC);
  var code ='result=evalResult("'+dropdown_type+'" , tonumber('+value_startbit+') , tonumber('+value_length+') , tonumber('+value_offset+') , tonumber('+value_mult+') , '+value_unit +')\n'
  // TODO: Change ORDER_NONE to the correct strength.
//  return [code, Blockly.Lua.ORDER_NONE];
  return code;
};


