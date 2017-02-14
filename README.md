Spring Boot Application for downloading files from provided url
===============================================================

 It is a web REST application.<br />
<br />
Technology stack:<br />
Java 8<br />
Spring boot stater web (Tomcat embeded)<br />
Apache VFS<br />
Spring Boot Test<br />
JUnit<br />
WireMock<br />
Maven<br />
<br />
The application can be pacakged to a single jar file with maven package command 'mvn package'.<br />
It will create a jar file wich can pe run with the commanda java -jar .....jarfile<br />
<br />
There is an application.properties file for configuration of:<br />
port<br />
download folder<br />
<br />
For testing purposes the application starts with 2 files already in the storage. The storage is in memory on a <br />
<br />
ConcunrrentHashMap implementation.<br />
<br />
<br />
Given the fact that the application is proccessing the file download in the background asyncroniously two endpoints will be <br />
<br />
used. One (POST) to send the URI of the file to be downloaded and the other one (GET) to get the file status.<br />
<br />
It will expose two API REST endpoints:<br />
<br />
GET /download/{fileIiD}<br />
It's used to get information about a file by a given fileId. If the file Id is unknown a 404 HTTP status response will be <br />
<br />
return.<br />
If the file is known then a 200 HTTP Status is returned toghether with a DownloadEntity that will held the path of the file <br />
<br />
that the user was sending and a status message. As long as the file has status 'STARTED' that means that the file is still <br />
<br />
being proccessed. So the client can send another request at a later time to get the status of the file. This logic was <br />
<br />
preferred because the request wont hang or timeout in case of a larger files.<br />
<br />
POST /download Accepts json body in the request<br />
{<br />
"url":"http://someurl.com/path/to/file.html",<br />
"username":"some_user",<br />
"password": "some_password"<br />
}<br />
<br />
url - string - required, the url to download the file from<br />
username - string - optional, in case the protocol needs authentication mechanism<br />
password - string - optional, in case the protocol needs authentication mechanism<br />
<br />
200 Success response<br />
<br />
Tests:<br />
<br />
There are two types of tests, unit testing and integration testing. The integration testing is using spring rest tempate <br />
<br />
with WireMock for http requests.