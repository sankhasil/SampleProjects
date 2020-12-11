import pymongo
import asyncio
# import threading
from bson.json_util import dumps
client = pymongo.MongoClient("mongodb://admin:admin@localhost:27018")
collection = client['test-db']['test-collection']
loop = asyncio.get_event_loop()


# async def mongo_watch_for_all(col):
#     change_stream_for_all = col.watch() 
#     for change in change_stream_for_all:
#         print("All changes ",dumps(change))
#         print('')

async def mongo_watch_with_filter(col1):
    change_stream_for_specific = col1.watch([{
    '$match' : {  'operationType': { '$in': ['insert','update','replace'] }, 
                  'fullDocument.specificKey': 'Erste'}
    }])
    
    # for change in change_stream_for_specific:
    #     print("Filtered ",dumps(change))
    #     print('')

    return await change_stream_for_specific.next


# all_thread = threading.Thread(target=mongo_watch_for_all,args=(collection, ))
# filter_thread = threading.Thread(target=mongo_watch_with_filter,args=(collection, ))
print("Watching ....")
# asyncio.ensure_future(mongo_watch_for_all(collection))
asyncio.ensure_future(mongo_watch_with_filter(collection))
loop.run_forever()
# filter_thread.start()
# all_thread.start()
# all_thread.join()
# filter_thread.join()




