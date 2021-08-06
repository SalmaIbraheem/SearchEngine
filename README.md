# Angelica

Main Page                  |  Results List
:-------------------------:|:-------------------------:
![Project Image](https://drive.google.com/uc?export=view&id=1agooS_mUJsXmGN2zQ8zKfNc7rIKEksfg) | ![Project Image](https://drive.google.com/uc?export=view&id=1TJI6T69-azsKHGDrroYCcYGYvicHNBL3)

> Search Engine Website

---

### Table of Contents
sections header to referance location f the topic  

- [Description](#description)
- [Ranking](#ranking)
- [How to Run](#how-to-run)
- [Dependencies](#dependencies)

---

## Description

Angelica is a search engine website that uses Web Crawler to browse the world wide websites	
It usues Text or voice search to get from the user the word that he wants to get websites based on, It response to his search with a list of websites ordered with the website ranking.
to accomplish that 4 main Modules:
* Web Crawler
* Indexer
* Query Processing / Phrase Searching 
* Ranker

#### Technologies
* JAVA Servelates 
* JSP
* HTML / CSS

---

## Ranking
* Term_frequency-Inverse_document(TF-IDF) to rank relevance of a search query
* The PageRank algorithm to rank the popularity of all pages
* In geographical based ranking if the extension of the page is the same as the location of the search query then this page’s rank is increased by a default percentage to give a boost to the rank of this page
* Personalized search, after the user click on a page the base url for this page is increased by certain percentage
		
----

## How To run
* Connect to mssql and Create a database called search_engine in mssql
* Run the file called CrawlerMain.java to start crawling,indexing and ranking websites
* In the case that you stop the program before crawler finishes crawling its limit of pages you should run the Ranker.java module on its own to rank pages crawled so far as the popularity ranking starts after the crawler finishes to get more accurate results 
			 
### To run the website 
* run tomcat
* run the main.jsp file on tomcat 
	
---
				
## Dependencies
- jsoup.jar 
- servler-api.jar
- json-simple.jar
- mssql-jdbc.jar
- opennlp-tools.jar

---
In the link attached is a .bak file of a database that the program crawled and indexed so it could be
used to preform queries on it
> https://drive.google.com/file/d/1iLhaS6YQ9PzUpCq32B2ShzDFTY4sHlN7/view?usp=sharing
