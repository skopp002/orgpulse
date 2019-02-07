import lucene, json
from org.apache.lucene.document import Document, Field, FieldType, StringField, TextField
from org.apache.lucene.index import IndexWriterConfig, IndexWriter
from org.apache.lucene.analysis.standard import StandardAnalyzer
from org.apache.lucene.store import RAMDirectory
import glob

#awk '{ sub("\r$", ""); print }' tweets.json > tweets_tweaked.json
#specify the directory in which the tweets_tweaked.json exists

tweets=[]
json_direc = input("Enter the relative path of the JSON files: ")
for filename in glob.iglob(json_direc + '**/*.json', recursive=True):
    try:
      print("Filename is", filename)
      with open(filename) as f:
        for line in f:
           tweet=json.loads(line)
           tweets.append(tweet)
    except:
        continue

for tweet in tweets:
    ids = [tweet['id_str'] for tweet in tweets if 'id_str' in tweet]
    text = [tweet['text'] for tweet in tweets if 'text' in tweet]
    lang = [tweet['lang'] for tweet in tweets if 'lang' in tweet]
    geo = [tweet['geo'] for tweet in tweets if 'geo' in tweet]
    place = [tweet['place'] for tweet in tweets if 'place' in tweet]
    print(ids, text, lang, geo, place)

tweet_dict = {"ids":ids,"text":text,"lang":lang,"geo":geo,"place":place}
lucene.initVM(vmargs=['-Djava.awt.headless=true'])
index = Document()
for i in list(tweet_dict):
    index.add(Field(i, tweet_dict[i], StringField.TYPE_STORED))
index_config = IndexWriterConfig(StandardAnalyzer())
index_direc = RAMDirectory()
#currently the index is being saved on RAM, saving on disk is possible as well. 
index_writer = IndexWriter(index_direc,index_config)
index_writer.addDocument(index)
index_writer.commit()
index_writer.close()
