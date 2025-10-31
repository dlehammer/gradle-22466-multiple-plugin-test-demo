package multiple.plugin.test.demo

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class DemoPluginSpec extends Specification {

    File testDirectory = new File("build/tmp/test/${DemoPluginSpec.simpleName}/my-project")

    void setup() {
        if (testDirectory.exists()) {
            assert testDirectory.deleteDir()
        }

        assert testDirectory.mkdirs()
    }

    void 'test demo key presence'() {
        setup:
            injectBuildGradle()
            injectGradleProperties()
            injectMainClass()
            injectSettingsGradle()

        and:
            GradleRunner gradleRunner = GradleRunner.create()
                    .forwardOutput()
                    .withArguments('bootJar',
                            "-Dmaven.repo.local=${Paths.get('build/tmp/test/mvn-repo-override').toAbsolutePath().toString()}")
//                    .withPluginClasspath()
                    .withProjectDir(testDirectory)

        when:
            BuildResult result = gradleRunner.build()

        then:
            result.output.contains('BUILD SUCCESSFUL')

            getBuildInfoProperty('build.demo-key') == 'some-demo-value'
    }

    private void injectBuildGradle() {
        new File(testDirectory, 'build.gradle') << '''
plugins {
    id 'java'
    id 'multiple-plugin-test-demo' version "${demoPluginVersion}"
    id 'org.springframework.boot' version "${springBootVersion}"
}

dependencies {
    implementation "org.springframework.boot:spring-boot-starter-web:$springBootVersion"
}

repositories {
    mavenLocal()
    mavenCentral()
}
'''
    }

    private void injectGradleProperties() {
        File gradleProperties = new File(testDirectory, 'gradle.properties')
        Path.of('gradle.properties').withReader { gradleProperties.append(it) }
    }

    private void injectMainClass() {
        File packageDirectory = new File(testDirectory, 'src/main/java/demo')
        assert packageDirectory.mkdirs()

        new File(packageDirectory, 'TestApplication.java').text = '''package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @GetMapping("/hello")
    String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

}
'''
    }

    private void injectSettingsGradle() {
        new File(testDirectory, 'settings.gradle').text = '''
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}

rootProject.name = 'my-project'
'''
    }

    private String getBuildInfoProperty(String key) {
        return testDirectory.toPath().resolve('build/resources/main/META-INF/build-info.properties').withReader {
            Properties properties = new Properties()
            properties.load(it)

            return properties.get(key)
        }
    }

}