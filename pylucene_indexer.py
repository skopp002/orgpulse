import lucene, json
from org.apache.lucene.document import Document, Field, FieldType, StringField, TextField
from org.apache.lucene.index import IndexWriterConfig, IndexWriter
from org.apache.lucene.analysis.standard import StandardAnalyzer
# this was just what I was using to access the .json file from my desktop, a direct input of the directory may not
# really be the best idea. The open() and json.load functions output a dictionary value, test_dict a direct copy paste
# of the output of these functions from a small test .json file I downloaded to use for testing
# json_direc = input("Enter the Directory of the JSON file: ")
# with open(json_direc) as file:
# data = json.load(file)
test_dict = {'error_message': 'You must use an API key to authenticate each request to Google Maps Platform APIs. For additional information, please refer to http://g.co/dev/maps-no-account', 'results': [], 'status': 'REQUEST_DENIED'}
lucene.initVM(vmargs=['-Djava.awt.headless=true'])
index = Document()
index_fields = ["error_message", "status"]
# index_fields can be set to pick out the parts of the tweets we care about, this area would have code to further parse
# or narrow down results in a more specific way if we wanted/needed.
for i in index_fields:
    index.add(Field("error_message", test_dict[i], StringField.TYPE_STORED))
index_config = IndexWriterConfig(StandardAnalyzer())
# index_direc = SimpleFSDirectory(Paths.get(sys.argv[1]))
# not sure what the formula for index_direc is doing, can this directory be set up with a prompt or
# just set to be the same value as json_direc? Once that is done the below code should work to commit changes to
# the index? once index_direc is set the rest of the code below should do the finishing steps for the index
# index_writer = IndexWriter(index_direc,index_config)
# index_writer.addDocument(index)
# index.writer.commit()
# index.writer.close()
