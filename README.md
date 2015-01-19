#Decision Support System - Data Import Module

![Build Status](https://travis-ci.org/CA-Labs/dss-data-import.svg)

##Overview
In the context of a project funded by European Union called MODAClouds ([www.modaclouds.eu](http://modaclouds.eu)), part of the objective was to design a Decision Support System to analyze offerings of different cloud providers and compare them accross different dimentions.

Data Import Module is part of the data gathering process of the MODAClouds DSS. The module is designed as a standalone CLI based application and can be implemented accross different types of uses, not necessarly connected to the Cloud Service providers scenario. 

##Installation

###From source
*Please keep in mind that in order to compile this program from source, you need to have your environmnet ready for Scala development. You can download scala from [http://www.scala-lang.org/download](http://www.scala-lang.org/download)*

1. Clone the repository with `git clone git@github.com:CA-Labs/dss-data-import.git`
1. Naviage to the cloned repository and execute `sbt` command in the repository root folder.
1. Execute `pack` task
1. Type `exit` to come back to your standard shell

###Standalone Release
*Please keep in mind that release assumes that you have Java JRE installed in your system. You can download Java from [http://www.oracle.com/technetwork/java/javase/downloads/index.html](http://www.oracle.com/technetwork/java/javase/downloads/index.html)*

1. Download the latest release from [https://github.com/CA-Labs/dss-data-import/releases/](https://github.com/CA-Labs/dss-data-import/releases/)
1. Once extracted you can execute the CLI `./bin/dss-data-import`

##Concept
DSS Data Import module was designed to consume structurized movable or static data. It can extract data from sources like: 

* xml
* json
* xlsx

and all the variations of the following including public or authenticated API points. 

Extraction is carried out on per source basis with 2 configuration files needed as a base. The output is printed to stout as a JSON. This ensures the possiblity to further work on the data with other tools and ensures agnosticitiy of the tool. 

##Instructions
