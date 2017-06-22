package com.ljj.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by ljj on 2017/6/12.
 */

public class DelLogPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
      project.afterEvaluate {
          AppExtension android=project.getExtensions().getByType(AppExtension.class);

          project.logger.error("plugin:"+project.projectDir);
      }
  }
}
