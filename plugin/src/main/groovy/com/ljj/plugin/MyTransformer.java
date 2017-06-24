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

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

/**
 * Created by ljj on 2017/6/22.
 */

public class MyTransformer extends Transform {

  Project project;

  public MyTransformer(Project project) {
    this.project = project;
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
    project.getLogger().error("context->path：" + context.getPath());
    project.getLogger().error("context->temporaryDir：" + context.getTemporaryDir().getAbsolutePath());
    for (TransformInput input : inputs) {
      Collection<DirectoryInput> dirInputs = input.getDirectoryInputs();
      Collection<JarInput> jarInputs = input.getJarInputs();
      for (DirectoryInput directoryInput : dirInputs) {
        project.getLogger().error("context->directoryInput：" + directoryInput.getFile().getAbsolutePath());

        File dest = outputProvider
            .getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(),
                Format.DIRECTORY);
        project.getLogger().error("context->directoryOutput：" + dest);
        //inject(directoryInput.getFile().getAbsolutePath(),packageName);
        FileUtils.copyDirectory(directoryInput.getFile(), dest);
      }
      for (JarInput jarInput : jarInputs) {
        project.getLogger().error("context->JarInput：" + jarInput.getFile().getAbsolutePath());
        File dest = outputProvider
            .getContentLocation(jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
        project.getLogger().error("context->JarOutput：" + dest);
        FileUtils.copyFile(jarInput.getFile(), dest);
      }
    }
  }

  ClassPool pool = ClassPool.getDefault();
  String packageName = "com/ljj/gradleplugin";

  private void inject(String path, String packageName) {

    try {
      project.getLogger().error("inject");
      pool.appendClassPath(path);
      project.getLogger().error("traverseFolder");
      traverseFolder(new File(path));
    } catch (Exception e) {
      project.getLogger().error(e.getMessage());
      e.printStackTrace();
    }

  }

  private static String injectStr = "System.out.println(\"I Love ljj\" ); ";

  public void traverseFolder(File rootFile) throws NotFoundException, CannotCompileException, IOException {
    //        Log.d("traverseFolder:%s", rootFile);
    if (rootFile != null && rootFile.exists()) {
      File[] files = rootFile.listFiles();
      if (files == null || files.length == 0) {
        return;
      } else {
        for (File innerFile : files) {
          if (innerFile.isDirectory()) {
            traverseFolder(innerFile);
          } else {
            String filePath = innerFile.getAbsolutePath();

            if (filePath.endsWith(".class") && !filePath.contains("R$") && !filePath.contains("R.class") &&
                !filePath.contains("BuildConfig.class")) {
              // 判断当前目录是否是在我们的应用包里面

              int index = filePath.indexOf(packageName);
              project.getLogger().error("xxxxxxxxxxxxxxxxxxxxxxx" + innerFile.getAbsolutePath());
              boolean isMyPackage = index != -1;
              if (isMyPackage) {
                int end = filePath.length() - 6; // .class = 6
                String className = filePath.substring(index, end).replace('\\', '.').replace('/', '.');
                //开始修改class文件
                CtClass c = pool.getCtClass(className);
                if (c.isFrozen()) {
                  c.defrost();
                }
                CtConstructor[] cts = c.getDeclaredConstructors();
                if (cts == null || cts.length == 0) {
                  //手动创建一个构造函数
                  CtConstructor constructor = new CtConstructor(new CtClass[0], c);
                  constructor.insertBeforeBody(injectStr);
                  c.addConstructor(constructor);
                } else {
                  cts[0].insertBeforeBody(injectStr);
                }
                c.writeFile(rootFile.getAbsolutePath());
                c.detach();
              }

            }
          }
        }
      }
    } else {
      //Log.d("文件不存在");
    }
  }
}