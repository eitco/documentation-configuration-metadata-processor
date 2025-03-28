
[![License](https://img.shields.io/github/license/eitco/spring-config-generator.svg?style=for-the-badge)](https://opensource.org/license/mit)
[![Build status](https://img.shields.io/github/actions/workflow/status/eitco/documentation-configuration-metadata-processor/deploy.yaml?branch=main&style=for-the-badge&logo=github)](https://github.com/eitco/documentation-configuration-metadata-processor/actions/workflows/deploy.yaml)
[![Maven Central Version](https://img.shields.io/maven-central/v/de.eitco.cicd/documentation-configuration-metadata-processor?style=for-the-badge&logo=apachemaven)](https://central.sonatype.com/artifact/de.eitco.cicd/documentation-configuration-metadata-processor)

# documentation-configuration-metadata-processor

This [java annotation processor](https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/Processor.html) generates additional spring configuration metadata for the 
[configuration documentation generation plugin](https://github.com/eitco/spring-config-collector-maven-plugin).

It's main usage is to generate configuration metadata for classes used in collections in 
`@ConfigurationProperties` classes, which are ignored by the `spring-boot-configuration-processor`.

## Usage

Add a dependency to the processor to your POM:

````xml
<dependency>
    <groupId>de.eitco.cicd</groupId>
    <artifactId>documentation-configuration-metadata-processor</artifactId>
    <optional>true</optional>
    <version>processor-version</version>
</dependency>
````
Annotate classes to generate metadata for with:

````java
@AdditionalConfigurationMetadata(groups = {"TestGroup1","TestGroup2"})
````
The annotation requires one or more group names. The names are used to merge the generated configuration metadata with 
metadata generated by the `spring-boot-configuration-processor`. By setting more than ony group it is possible to reuse
metadata at several places in the generated documentation.

The processor supports the `@NestedConfigurationProperty` annotation from Spring Boot in classes annotated with 
`@AdditionalConfigurationMetadata`.
