#Decision Support System - Data Import Module

![Build Status](https://travis-ci.org/CA-Labs/dss-data-import.svg)

##Overview
In the context of a project funded by European Union called MODAClouds ([www.modaclouds.eu](http://modaclouds.eu)), part of the objective was to design a Decision Support System to analyze offerings of different cloud providers and compare them across different dimensions.

Data Import Module is part of the data gathering process of the MODAClouds DSS. The module is designed as a standalone CLI based application and can be implemented across different types of uses, not necessary connected to the Cloud Service providers scenario. 

##Installation

###From source
*Please keep in mind that in order to compile this program from source, you need to have your environmnet ready for Scala development. You can download scala from [http://www.scala-lang.org/download](http://www.scala-lang.org/download)*

1. Clone the repository with `git clone git@github.com:CA-Labs/dss-data-import.git`
1. Navigate to the cloned repository and execute `sbt` command in the repository root folder.
1. Execute `pack` task
1. Type `exit` to come back to your standard shell

###Standalone Release
*Please keep in mind that release assumes that you have Java JRE installed in your system. You can download Java from [http://www.oracle.com/technetwork/java/javase/downloads/index.html](http://www.oracle.com/technetwork/java/javase/downloads/index.html)*

1. Download the latest release from [https://github.com/CA-Labs/dss-data-import/releases/](https://github.com/CA-Labs/dss-data-import/releases/)
1. Once extracted you can execute the CLI `./bin/dss-data-import` with necessary arguments

##Concept
DSS Data Import module was designed to consume structured movable or static data. It can extract data from sources like: 

* xml
* json
* xlsx

and all the variations of the following including public or authenticated API points. 

Extraction is carried out on per source basis with 2 configuration files needed as a base. The output is printed to stout as a JSON. This ensures the possibility to further work on the data with other tools and ensures agnosticitiy of the tool. 

##Instructions

In order to successfully extract the data from the data source, two files are needed to be defined. 

1. Configuration file
1. Map file - which translates or standardises the output as to be used later on. 

###Configuration file definitions
example of the config file: 


```
source::http://api.openweathermap.org/data/2.5/weather?q=London&mode=xml  
resourceType::xmlAPI  
headers::accept=>*%  
```

`::` sign is used to indicate the definition of the configuration parameter and its value.

|Definition|Options|Comments|
|----------|-------|--------|
|source| |Valid URL or full PATH file location|
|resourceType|website<br>json<br>jsonAPI<br>xml<br>xmlAPI<br>xlsx|* website - will be treated as a subtype of xml<br>* xmlAPI - for WSDL types of the API|
|headers| |It should be specified as an object. It can accommodate multiple values. As per example:<br>`accept=>*%,api-key=>REALLYLongKey`|

###Map file definitions

Map files are key value address definition of the extract from the given data source. 

There are two main conventions used within the map file to specify the location of the data within the data. 

* JsonPath - used within sources [json, jsonAPI] - [specification](https://github.com/jayway/JsonPath)

Exmaple of JsonPath
```
metric1::$.foo.bar[0].foo
metric2::$.bar
metric3::$.baz.baz
```

* Xpath - used within sources [website,xml,xmlAPI,xlsx] - [specification](http://www.w3.org/TR/xpath20/)

Example of XPath
```
name::string(/current/city/@name)
country::string(/current/city/country)
```

###Running the tool
Once config file and map file are defined you can run the tool manually though command line or by a scheduled job process. 

There are two main configuration parameters necessary to run the extraction successfully: 
```
  -c <value> | --config <value>
        Absolute path to configuration file
  -m <value> | --mapping <value>
        Absolute path to map file
```

**Please note that the tool needs absolute paths for the files to be specified**

Example of a run:
```
./bin/dss-data-import \ 
-c /Users/yesyes/Documents/SuperSecretMegaProject/extract1.conf \ 
-m /Users/yesyes/Documents/SuperSecretMegaProject/extract1.map
```

##License
Copyright 2014-2015 CA Technologies - CA Labs EMEA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
