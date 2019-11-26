import sys
import json
from pyes import ES

###
# Injects data into a new index
###

INDEX_NAME = 'color'
conn = ES()


def log(msg, flush=True):
	sys.stdout.write('---' + msg + ' --- \r')
	if flush:
		sys.stdout.flush()


def index(data_type):
	count = 0
	with open('events.json') as b:
		for line in b:
			try:
				doc = json.loads(line)
				conn.index(doc, INDEX_NAME, data_type, bulk=500)
				count += 1
				log(data_type + ' ' + str(count) + ' documents indexed')
			except:
				log('ERROR: ' + line, flush=False)
	log(data_type + ' ' + str(count) + ' documents indexed', False)
	return conn.force_bulk()


index('event')
