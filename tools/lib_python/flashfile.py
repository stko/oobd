#!/usr/bin/env python

"""save the binary blocks as a MIME message."""   



# Doku : https://docs.python.org/2/library/email-examples.html


import os
import sys
import pprint

# For guessing MIME type based on file name extension
import mimetypes

from optparse import OptionParser

from email import encoders
from email.message import Message
from email.mime.audio import MIMEAudio
from email.mime.base import MIMEBase
from email.mime.image import MIMEImage
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from email.utils import make_msgid

# Create the container (outer) email message.
msg = MIMEMultipart()

msg.preamble = 'Our family reunion'
binary_cid = make_msgid('OOBD')
fp = open("vbf_lib.py", 'rb')
img = MIMEBase('application','octet-stream',cid=binary_cid)
img.set_payload(fp.read())
fp.close()
# Encode the payload using Base64
encoders.encode_base64(img)
msg.attach(img)

print msg.as_string()
pp = pprint.PrettyPrinter(indent=4)
for part in msg.walk():
    ct=part.get_param('cid')
    if ct:
        pp.pprint(ct)
        con=part.get_payload(decode=True)
        print (con)