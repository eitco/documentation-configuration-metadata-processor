
[![License](https://img.shields.io/github/license/eitco/spring-config-generator.svg?style=for-the-badge)](https://opensource.org/license/mit)

[//]: # ([![Build status]&#40;https://img.shields.io/github/actions/workflow/status/eitco/spring-config-generator/deploy.yaml?branch=main&style=for-the-badge&logo=github&#41;]&#40;https://github.com/eitco/spring-config-generator/actions/workflows/deploy.yaml&#41;)
[//]: # ([![Maven Central Version]&#40;https://img.shields.io/maven-central/v/de.eitco.cicd/spring-config-generator?style=for-the-badge&logo=apachemaven&#41;]&#40;https://central.sonatype.com/artifact/de.eitco.cicd/spring-config-generator&#41;)

# additional-spring-configuration-metadata-processor

This [java annotation processor](https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/Processor.html) generates additional [spring configuration metadata](https://docs.spring.io/spring-boot/specification/configuration-metadata/annotation-processor.html#appendix.configuration-metadata.annotation-processor.adding-additional-metadata)
for collections in classes annotated with `@ConfigurationProperties`. This metadata is not required for IDE support. 
It's main usage is providing data for the [configuration documentation generation plugin](https://github.com/eitco/spring-config-collector-maven-plugin).