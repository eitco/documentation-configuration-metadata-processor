package de.eitco.cicd.ascmp;

import org.springframework.boot.configurationprocessor.fieldvalues.FieldValuesParser;
import org.springframework.boot.configurationprocessor.fieldvalues.javac.JavaCompilerFieldValuesParser;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;
import org.springframework.boot.configurationprocessor.metadata.JsonMarshaller;
import org.springframework.boot.configurationprocessor.support.ConventionUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AdditionalMetadataProcessor extends AbstractProcessor {

    private static final String ADDITIONAL_METADATA_PATH = "META-INF/documentation-spring-configuration-metadata.json";

    private final ConfigurationMetadata configurationMetadata = new ConfigurationMetadata();
    private final Set<ElementWithGroup> processedElements = new HashSet<>();

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

            String[] groups = element.getAnnotation(AdditionalConfigurationMetadata.class).groups();

            for (String group : groups) {

                String elementType = ((TypeElement) element).getQualifiedName().toString();

                configurationMetadata.addIfMissing(ItemMetadata.newGroup(ConventionUtils.toDashedCase(group), elementType, elementType, null));

                try {
                    processElement(element, group, elementType);
                } catch (Exception e) {
                    processingEnv.getMessager().printError("[AdditionalMetadataProcessor]: " + e.getMessage(), element);
                }
            }
        });

        if (roundEnv.processingOver()) {

            try {
                writeJson();
            } catch (Exception e) {
                processingEnv.getMessager().printError("[AdditionalMetadataProcessor]: " + e.getMessage());
            }
        }

        return false;
    }

    private void processElement(Element element, String group, String elementType) {

        ElementWithGroup elementWithGroup = new ElementWithGroup(group, element);

        if (processedElements.contains(elementWithGroup)) {
            return;
        }

        processedElements.add(elementWithGroup);

        Map<String, Object> fieldValues;

        try {
            fieldValues = fieldValuesParser.getFieldValues((TypeElement) element);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<VariableElement> variableElements = element.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.FIELD)
            .map(e -> (VariableElement) e)
            .toList();

        for (VariableElement variableElement : variableElements) {

            String fieldName = variableElement.getSimpleName().toString();

            NestedConfigurationProperty nestedConfigurationProperty = variableElement.getAnnotation(NestedConfigurationProperty.class);

            if (nestedConfigurationProperty != null) {
                Element nestedElement = processingEnv.getTypeUtils().asElement(variableElement.asType());
                processElement(nestedElement, group + "." + fieldName, elementType);
            } else {
                addProperty(group, elementType, variableElement, fieldValues, fieldName);
            }
        }
    }

    private void addProperty(
        String group,
        String elementType,
        VariableElement variableElement,
        Map<String, Object> fieldValues,
        String fieldName
    ) {

        String name = group + "." + fieldName;
        String docComment = getDocComment(variableElement);

        configurationMetadata.addIfMissing(
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

        if (docComment == null) {
            return null;
        }

        docComment = docComment.replaceAll("\r", " ");
        docComment = docComment.replaceAll("\n", " ");

        return docComment.trim();
    }
}
