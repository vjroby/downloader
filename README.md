[![Build Status](https://travis-ci.org/vjroby/downloader.svg?branch=master)](https://travis-ci.org/vjroby/downloader)
####Spring Boot Application for downloading files from provided url [Downloader]

 For short It is a web REST application.

Technology stack:

* Java 8 
* Spring boot stater web (Tomcat embeded)
* Apache VFS
* Spring Boot Test
* JUnit
* WireMock
* Apache Mina
* Maven

The application can be pacakged to a single jar file with maven package command `mvn package`.It will create a jar file wich can pe run with the command `java -jar path/to/file/downloader-0.0.1-SNAPSHOT.jar`

For configuration is used `application.properties` file and tests are using `test.properties`, where you can set the download folder and the buffer size. Together with some other Spring related settings.

The application will start on port 8080 and in the context `/api`. 

For testing purposes the application starts with 2 entries in the storage. The storage is in memory on a `ConcunrrentHashMap` implementation.

Given the fact that the application is downloading files asynchronously two endpoints will be used. One `[POST]` to send the `URL` of the file to be downloaded that will send the id of the file in the response. And the other one [GET] to retrieve the status of the file using the provided file id.

Two API REST endpoints will be exposed to the client:

`GET /download/{fileIiD}`
It's used to get information about a file by a given fileId. If the file Id is unknown a `404 HTTP` status response will be return.
If the file is known then a 200 HTTP Status is returned together with a 

Example DownloadEntity:

    {
    "path":"file://tmp/10-2017__18:48:13.log", 
    "fileName":"10-2017__18:48:13_1487181182231.log", 
    "status":"COMPLETED"
    }

DownloadEntity values

* path - the path of the file that the user was sending and a status message. 
* filename - the filename of the file
* status - the status of the file

As long as the file has status 'STARTED' that means that the file is still being processed. So the client can send another request at a later time to get the status of the file. This logic was preferred because this way request won't hang or timeout in case of a larger files.

POST /download 
Accepts JSON body in the request as fallows:

    {
    "url":"http://someurl.com/path/to/file.html",
    "username":"some_user",
    "password": "some_password"
    }

* url - string - required, the url to download the file from
* username - string - optional, in case the protocol needs authentication mechanism
* password - string - optional, in case the protocol needs authentication mechanism

200 Success response


Testing:

There are two types of tests, unit testing and integration testing. The integration testing is using spring rest template with WireMock for http mocking and Apache Mina for sftp server mocking.