# openapi-to-plantuml
Java library to generate a [PlantUML](https://plantuml.com) Class Diagram (supplemented with path information) from an OpenAPI 3.0 definition (YAML or JSON).

**Features**

Supports all features of OpenAPI 3.0 including

* Mixed types (*oneOf*, *anyOf*, *allOf*) 
* Multiple levels of indirection (`$ref` -> `$ref` for example) 
* Multiple response content types

**Status:** in development (first release to Maven Central mid-April 2021)

**Limitations**

* Only handles internal references ("#/*"), that is the yaml file should be self-contained
* Doesn't model *Callbacks* or *Links*

[openapi-example.yml](src/test/resources/openapi-example.yml): 

<img style="background-color:white" src="src/docs/openapi-example.svg"/>

## Getting started
Add this dependency to your pom.xml:

```xml
<dependency>
  <groupId>com.github.davidmoten</groupId>
  <artifactId>openapi-to-plantuml</artifactId>
  <version>VERSION_HERE</version>
</dependency>
```

## Usage

```java
import com.github.davidmoten.oas3.puml.Converter;

String puml = Converter.openApiToPuml(openapi);
```

## Examples

Unit test examples are [here](src/docs/examples.md).

[petstore-expanded.yml](src/test/resources/inputs/petstore-expanded.yml):

<img src="src/docs/tests/petstore-expanded.puml.svg"/>

[bookstore.yml](src/test/resources/demos/bookstore.yml):

<img src="src/docs/demos/bookstore.svg"/>

[ecommerce.yml](src/test/resources/demos/ecommerce.yml)

<img src="src/docs/demos/ecommerce.svg"/>

[news.yml](src/test/resources/demos/news.yml):

<img src="src/docs/demos/news.svg"/>

[strava.yml](src/test/resources/demos/strava.yml):

<img src="src/docs/demos/strava.svg"/>

[twitter.yml](src/test/resources/demos/twitter.yml):

<img src="src/docs/demos/twitter.svg"/>

[ebay.yml](src/test/resources/demos/ebay.yml):

<img src="src/docs/demos/ebay.svg"/>

