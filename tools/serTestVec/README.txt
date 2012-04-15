Just two helping tools to fire a set of commands to the serial port

genTestVec.py just generates very basically a series of P x x x commands

serTestVec.py writes either a file or stdin to the given serial port

Both prgram can be "piped together" like this


python genTestVec.py | python serTestVec.py /tmp/DXM -


Please note that all input files should have \r as end of line, not \n nor \r\n





