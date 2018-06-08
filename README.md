# Chatbot
![demo](https://github.com/wongkaho2626/Chatbot/blob/master/Chatbot%20GUI.png)

* Using a retrieval-based model to build a short text conversation machine. The chatbot should be able to engage in a conversation with users in natural human language.
* A wide range of conversation data should be crawled from social media, such as Hong Kong Discuss Forum.
* A considerable diversity regarding conversation scope by using Cantonese as the conversation language. The talking machine should be able to reply to anything user types.

## Prerequirements
* Java 1.8.0_131
* Jsoup 1.10.2
* JSON 20171018
* Mmseg4j 1.8.2
* Apache Maven 4.0.0
* Apache Log4j 1.2.17
* Apache Lucene 7.2.1


## Dataset Retrieval
After we get the top 10 scoring hits of the posts, we can start to do another indexing on comment dataset in order to get next scoring of the comments. First, we need to extract the comments base on the id from the top 10 scoring hits. After that, we do the indexing on those comments with using RAMDirectory. Because the size of the extracted comments is not too large, we can use RAMDirectory to reduce the indexing time compare with using FSDirectory. Each of the queries has different top 10 scoring hits of the post. Thus, we need to do the indexing of the extracted comment every time.
![demo](https://github.com/wongkaho2626/Chatbot/blob/master/Comments%20dataset%20retrieval.png)

## Multi-Round Conversation
Multi-round conversation means the chatbot can understand the previous conversation and make a response based on historical conversation record, which is a new function of this chatbot. Nowadays, many chatbots with retrieval-based model do not provide multi-round conversation as much effort is required to include the historical conversation record in the chatbot. Therefore, we are going to build three different methods to do the multi-round conversation, "Multi-Round Conversation: Q1 with keyword", "Multi-Round Conversation: Q1", and "Multi-Round Conversation: Q1 and R1". To simplify the testing on the multi-round conversation, we imply three round conversation. After three round conversation, the historical record will be deleted. And we hope to build a chatbot can make a response based on historical conversation record.

## Dataset
Please click [here](https://drive.google.com/file/d/1CG6i8lsKM7fyFA1c6B3Hzmly9MY8qAH2/view?usp=sharing) download the source code of chatbot and the dataset should be under:
```
Chatbot
└── resources
   ├── commentAfterChineseTextSegmentation1500.json
   ├── .
   ├── .
   ├── .
   ├── commentAfterChineseTextSegmentation2727.json
   ├── INDEX_DIRECTORY
   │   ├── _c.cfe
   │   ├── .
   │   ├── .
   │   ├── .
   │   └── write.lock
   └── postAfterChineseTextSegmentation.json
```

## License
This project is licensed under the terms of the [Apache license](http://www.apache.org/licenses/).
