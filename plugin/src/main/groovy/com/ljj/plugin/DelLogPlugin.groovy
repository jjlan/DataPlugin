package com.ljj.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Created by ljj on 2017/6/12.
 */

public class DelLogPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
      project.afterEvaluate {
          System.out.println("ljj->plugin");
          project.logger.error("plugin:"+project.projectDir);
      }
  }
}
