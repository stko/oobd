import os
import sys
import email
import yaml
import struct

import binascii


class FlashECU:

	def __init__(self,flashFileName):
		fp = open(flashFileName)
		self.msg = email.message_from_file(fp)
		fp.close()
		for part in self.msg.walk():
			# multipart/* are just containers
			if part.get_content_type() != 'text/yaml':
				continue
			self.header=yaml.load(part.get_payload(decode=True))

	def blocks(self):
		return self.header["blocks"]
	
	def getBinary(self, block):
		cid=block["cid"]
		for part in self.msg.walk():
			ct=part.get_param('cid')
			if ct and ct==cid:
				return bytearray(part.get_payload(decode=True))



def download(module, filename, blocksize ):
	flashFile=FlashECU(filename)
	counter = 1
	for block in flashFile.blocks():
		print ("Block {0}: Start Adress {1:0X} Size {2} Bytes".format(counter,block["startAddress"],block["blockSize"]))
		counter+=1
		binary=flashFile.getBinary(block)
		telegram=bytearray.fromhex("34 00 44")+struct.pack(">i",block["startAddress"])+struct.pack(">i",block["blockSize"])
		answer=module.send(telegram)
		if not(answer and answer[0]==0x74):
			error=1
		bytesToSend =0L
		error = 0

		totalbytes = block["blockSize"]
		telegramcount = 1
		while ( ( totalbytes > 0 ) and ( error == 0 ) ):
			if totalbytes > blocksize:
				bytesToSend = blocksize
			else:
				bytesToSend = totalbytes
			dataptr=(telegramcount -1 ) * blocksize
			telegram=bytearray.fromhex("36")+struct.pack(">B",telegramcount)
			telegram.extend(binary[dataptr:dataptr+bytesToSend])

			answer=module.send(telegram)
			if not(answer and answer[0]==0x76):
				error=1
			totalbytes -=  bytesToSend
			if telegramcount < 255:
				telegramcount += 1 
			else:
				telegramcount = 0
			#{write ("Blocknr: " + inttostr(blockcount) + "telcount: " + inttostr(telegramcount) + " Totalbytes: " + inttostr(totalbytes)  );}
		telegram=bytearray.fromhex("37")
		answer=module.send(telegram)
		if not(answer and answer[0]==0x77):
			error=1
		break
	return flashFile
