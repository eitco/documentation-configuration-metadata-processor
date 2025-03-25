package de.eitco.cicd.ascmp;

import org.springframework.boot.configurationprocessor.fieldvalues.FieldValuesParser;
import org.springframework.boot.configurationprocessor.fieldvalues.javac.JavaCompilerFieldValuesParser;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;
import org.springframework.boot.configurationprocessor.metadata.JsonMarshaller;
import org.springframework.boot.configurationprocessor.support.ConventionUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AdditionalMetadataProcessor extends AbstractProcessor {

    private static final String ADDITIONAL_METADATA_PATH = "META-INF/additional-spring-configuration-metadata.json";

    private final ConfigurationMetadata configurationMetadata = new ConfigurationMetadata();

    private FieldValuesParser fieldValuesParser;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {

        try {
            this.fieldValuesParser = new JavaCompilerFieldValuesParser(processingEnv);
        } catch (Throwable ex) {
            this.fieldValuesParser = FieldValuesParser.NONE;
        }

        super.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {

        return Set.of(AdditionalConfigurationMetadata.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        roundEnv.getElementsAnnotatedWith(AdditionalConfigurationMetadata.class).forEach(element -> {

            String group = element.getAnnotation(AdditionalConfigurationMetadata.class).group();
            String elementType = ((TypeElement) element).getQualifiedName().toString();

            Map<String, Object> fieldValues;

            try {
                fieldValues = fieldValuesParser.getFieldValues((TypeElement) element);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            configurationMetadata.add(ItemMetadata.newGroup(ConventionUtils.toDashedCase(group), elementType, elementType, null));

            List<VariableElement> variableElements = element.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .map(e -> (VariableElement) e)
                .toList();

            for (VariableElement variableElement : variableElements) {

                String fieldName = variableElement.getSimpleName().toString();
                String name = group + "." + fieldName;
                String docComment = getDocComment(variableElement);

                configurationMetadata.add(
                    ItemMetadata.newProperty(
                        null,
                        name,
                        variableElement.asType().toString(),
                        elementType,
                        null,
                        docComment,
                        fieldValues.get(fieldName),
                        null
                    )
                );
            }
        });

        if (roundEnv.processingOver()) {

            writeJson();
        }

        return false;
    }

    private void writeJson() {
        try {
            FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", ADDITIONAL_METADATA_PATH);

            try (OutputStream out = resource.openOutputStream()) {

                new JsonMarshaller().write(configurationMetadata, out);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getDocComment(VariableElement variableElement) {

        String docComment = processingEnv.getElementUtils().getDocComment(variableElement);

        docComment = docComment.replaceAll("\r", " ");
        docComment = docComment.replaceAll("\n", " ");

        return docComment.trim();
    }
}
