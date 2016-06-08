# Hypermedia-Driven RESTful Web Service

[![Build Status](https://api.travis-ci.org/selakavon/spring-rest-oauth2-mongo.svg)](https://travis-ci.org/selakavon/spring-rest-oauth2-mongo)

This is a sample of a Hypermedia-Driven RESTful Web Service service based on Spring Boot platform using OAuth2 for protecting the endpoint and Mongo DB as a NoSQL storage.

### Technology stack POC based on Spring Boot
##### Spring projects used:
* [Spring Boot](http://projects.spring.io/spring-boot/)
* [Spring Data Mongo DB](http://projects.spring.io/spring-data-mongodb/)
* [Spring Data REST](http://projects.spring.io/spring-data-rest/)
* [Spring Security OAuth](http://projects.spring.io/spring-security-oauth/)
* [Spring HATEOAS](http://projects.spring.io/spring-hateoas/)
 
### Building

##### Using Maven

````sh
mvn clean install
````

### Dev enviroment configuraiton

##### Mongo

##### Admin user init script

````javascript
db.user.insert(
	{ "fullName": "Administrator",
	  "userName": "admin",
	  "password": "admin",
	  "roles": ["ADMIN","USER"]
	}
);
````

### Running 

##### Using Maven

````sh
mvn spring-boot:run -Drun.profiles=localmongo
`````

To enable ssl use -Drun.profiles=https.
````sh
mvn spring-boot:run -Drun.profiles=https,localmongo
````

To define custom mongo DB URI use spring.data.mongodb.uri argument.
````sh
java -jar target/jogging-1.0-SNAPSHOT.war --spring.data.mongodb.uri=
````

### Usage

:information_source: You can see the whole workflow at commented test methods userFLowTest() and adminFlowTest in
[sixkiller.sample.restapi.controller.E2EControllersTest](https://github.com/selakavon/spring-rest-oauth2-mongo/blob/master/src/test/java/sixkiller/sample/restapi/controller/E2EControllersTest.java)

:information_source: For testing purposes curl is used in insecure way regarding site's certificate.

##### Let's start with **ROOT** API resource.

The REST service stronly **Hypermedia-driven** and Content Type is **application/hal+json**.

````sh
curl -X GET https://localhost:8443/api -k
````

You will received hal+json body containing public resource(s).
````json
{
    "_links": {
        "users": {
            "href": "https://localhost:8443/api/users"
        }
    }
}
````
##### Now let's **Register** user.

````sh
curl -X POST https://localhost:8443/api/users -k -d '{"fullName":"Ales Novak","userName":"ales","password":"secret"}' -H 'Content-Type: application/json'
````

You will receive 201 status and Location headers.
````
HTTP/1.1 201 Created
Location: https://localhost:8443/api/users/ales
````

##### Now let's read user you've created

````sh
curl -X GET https://localhost:8443/api/users/ales -k -v
````

You will received **HTTP/1.1 401 Unauthorized** status and a following JSON body:

````json
{
    "error": "unauthorized",
    "error_description": "Full authentication is required to access this resource"
}
````

##### It's time to authenticate with new user's credentials

````sh
curl -X POST -vu webui:webuisecret https://localhost:8443/oauth/token -k -H "Accept: application/json" -d "password=secret&username=ales&grant_type=password&scope=read%20write&client_secret=webuisecret&client_id=webui"
````
You will received JSON containing access and refresh tokens.
````json
{
    "access_token": "7fb7353c-0b48-407c-9a18-65ed9754fea0",
    "token_type": "bearer",
    "refresh_token": "a4bb9adb-164d-4794-b181-fafd8458e4fa",
    "expires_in": 43199,
    "scope": "read write"
}
````

#### Get user's info authenticated
````sh
curl -X GET https://localhost:8443/api/users/ales -k -v -H "Authorization: Bearer 7fb7353c-0b48-407c-9a18-65ed9754fea0"
````

You will received basic user info and hypermedia links pointing to user related resources.

````json
{
    "fullName": "Ales Novak",
    "userName": "ales",
    "roles": ["USER"],
    "_links": {
        "self": {
            "href": "https://localhost:8443/api/users/ales"
        },
        "timeEntries": {
            "href": "https://localhost:8443/api/users/ales/time-entries"
        },
        "reportWeeks": {
            "href": "https://localhost:8443/api/users/ales/report-weeks"
        }
    }
}
````

##### For the rest of the workflow please take a look at commented test methods userFLowTest() and adminFlowTest in
[sixkiller.sample.restapi.controller.E2EControllersTest](https://github.com/selakavon/spring-rest-oauth2-mongo/blob/master/src/test/java/sixkiller/sample/restapi/controller/E2EControllersTest.java)



