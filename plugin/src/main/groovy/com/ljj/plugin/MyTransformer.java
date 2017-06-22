package com.ljj.plugin;


import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import com.android.build.api.transform.Context;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;

/**
 * Created by ljj on 2017/6/22.
 */

public class MyTransformer extends Transform {

 Project project;
  public MyTransformer(Project project){
    this.project=project;
  }

  @Override
  public String getName() {
    return "dataplugin";
  }

  @Override
  public Set<QualifiedContent.ContentType> getInputTypes() {
    return TransformManager.CONTENT_CLASS;
  }

  @Override
  public Set<QualifiedContent.Scope> getScopes() {
    return TransformManager.SCOPE_FULL_PROJECT;
  }

  @Override
  public boolean isIncremental() {
    return false;
  }

  @Override
  public void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs,
      TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException,
      InterruptedException {
     project.getLogger().error("context->path："+context.getPath());
     project.getLogger().error("context->temporaryDir："+context.getTemporaryDir().getAbsolutePath());
      for(TransformInput input:inputs){
        Collection<DirectoryInput> dirInputs=input.getDirectoryInputs();
        Collection<JarInput> jarInputs=input.getJarInputs();
        for(DirectoryInput directoryInput:dirInputs){
          project.getLogger().error("context->directoryInput："+directoryInput.getFile().getAbsolutePath());
          File dest=outputProvider.getContentLocation(directoryInput.getName(),directoryInput.getContentTypes(),directoryInput.getScopes(),
              Format.DIRECTORY);
          project.getLogger().error("context->directoryOutput："+dest);
          FileUtils.copyDirectory(directoryInput.getFile(),dest);
        }
        for(JarInput jarInput:jarInputs){
          project.getLogger().error("context->JarInput："+jarInput.getFile().getAbsolutePath());
          File dest=outputProvider.getContentLocation(jarInput.getName(),jarInput.getContentTypes(),jarInput.getScopes(),Format.JAR);
          project.getLogger().error("context->JarOutput："+dest);
          FileUtils.copyFile(jarInput.getFile(),dest);
        }
      }



  }
}
