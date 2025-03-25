import java.nio.file.Files

File baseDirectory = new File("$basedir");

File generatedSourceFile = new File(baseDirectory, "target/classes/META-INF/additional-spring-configuration-metadata.json")
assert generatedSourceFile.exists()
assert generatedSourceFile.isFile()

String content = Files.readString(generatedSourceFile.toPath())

assert content.contains("de.eitco.cicd.ascmp.test.TestSettings")

