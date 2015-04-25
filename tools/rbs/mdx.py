import xml.etree.ElementTree as ET
# https://docs.python.org/2/library/xml.etree.elementtree.html
import pprint


class DID:
	def __init__(self, xmlroot,didID):
		#tree = xmlroot.find("./ECU_DATA/DATA_IDENTIFIERS/DID[ID='"+didID+"']")
		tree = xmlroot.find("./ECU_DATA/DATA_IDENTIFIERS/DID[@ID='"+didID+"']")
		try:
			tree
			print ("1")
			self.name=tree.find("./NAME").text
			print ("2")
			self.bytesize=int(tree.find("./BYTE_SIZE").text)
			print ("bytesize", self.bytesize)
			self.didType=tree.find("./DID_TYPE").text
			print ("4")
			self.subFields=tree.findall("./SUB_FIELD")
			print ("5")
			try:
				self.bytesize
				self.data=bytearray()
				print ("1")
				for i in range(self.bytesize):
					self.data.extend([0])
				print ("2")
				print (self.name, self.bytesize)
				pprint.pprint (tree)
			except Exception as n:
				print("DID "+didID+" has no data!")
		except Exception as n:
			print("DID "+didID+" is not in module..!")
		
	def getData(self):
		try:
			self.data
			self.subFields
			for subfield in self.subFields:
				leastBit= subfield.find('LEAST_SIG_BIT').text
				print ("Leastbit", leastBit)
			return self.data
		except Exception as n:
			return []

class MDX:
	def __init__(self, filename):
		tree = ET.parse(filename)
		self.root = tree.getroot()
		self.bus=self.root.find("./PROTOCOL/PHYSICAL_AND_LINK_LAYER/NAME").text
#		self.bus=self.root.find("./PROTOCOL/PHYSICAL_AND_LINK_LAYER_ID/NAME").text
#		self.bus=self.root.findall("./MDX")
		self.moduleID=self.root.find("./PROTOCOL/PHYSICAL_AND_LINK_LAYER/PHYSICAL_ADDRESS").text
		self.name=tree.find("./ADMINISTRATION/ECU_NAME").text
		self.shortName=tree.find("./ADMINISTRATION/SHORTNAME").text
		self.dids={}

	def getBusData(self):
		return (self.bus, self.moduleID)
	
	def answerDiD(self,service, didID): # get the request separated into service and did itself, both as strings
		if service=="22": # read data by service
			try:
				self.dids[didID]
			except Exception as n:
				self.dids[didID]=DID(self.root,didID)
			return self.dids[didID].getData()