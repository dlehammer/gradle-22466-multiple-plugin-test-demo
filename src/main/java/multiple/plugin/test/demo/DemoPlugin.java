package multiple.plugin.test.demo;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.springframework.boot.gradle.dsl.SpringBootExtension;

public class DemoPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.afterEvaluate(project1 ->
                project.getPluginManager()
                        .withPlugin("org.springframework.boot", appliedPlugin ->
                                project.getExtensions()
                                        .getByType(SpringBootExtension.class)
                                        .buildInfo(buildInfo ->
                                                buildInfo.properties(properties ->
                                                        properties.getAdditional().put("demo-key", "some-demo-value")
                                                )
                                        )
                        )
        );
    }

}