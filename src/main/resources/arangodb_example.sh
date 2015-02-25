#!/usr/bin/bash

# An example of dss-data-import tool using Cloud Harmony API

DI=../../../target/pack/bin/dss-data-import 
DS=../../../../dss-data-save/target/pack/bin/dss-data-save
BASE_DI=json/api/cloudharmony.
BASE_DS=../../../../dss-data-save/src/test/resources/

function line {
  echo "\n$1\n"
}

line "Amazon Web Services (AWS)"
${DI} -c ${BASE_DI}aws.config -m ${BASE_DI}aws.map | ${DS} -d arangodb -p ${BASE_DS}arangodb.props
line "AWS CloudFront"
${DI} -c ${BASE_DI}aws.cloudfront.config -m ${BASE_DI}aws.cloudfront.map | ${DS} -d arangodb -p ${BASE_DS}arangodb.props
line "AWS DynamoDB"
${DI} -c ${BASE_DI}aws.dynamodb.config -m ${BASE_DI}aws.dynamodb.map | ${DS} -d arangodb -p ${BASE_DS}arangodb.props
line "AWS EBS"
${DI} -c ${BASE_DI}aws.ebs.config -m ${BASE_DI}aws.ebs.map | ${DS} -d arangodb -p ${BASE_DS}arangodb.props
line "AWS EC2"
${DI} -c ${BASE_DI}aws.ec2.config -m ${BASE_DI}aws.ec2.map | ${DS} -d arangodb -p ${BASE_DS}arangodb.props
line "AWS RDS"
${DI} -c ${BASE_DI}aws.rds.config -m ${BASE_DI}aws.rds.map | ${DS} -d arangodb -p ${BASE_DS}arangodb.props
line "AWS RedShift"
${DI} -c ${BASE_DI}aws.redshift.config -m ${BASE_DI}aws.redshift.map | ${DS} -d arangodb -p ${BASE_DS}arangodb.props
