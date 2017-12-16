package com.ljj.plugin;

import java.io.File;
import java.io.IOException;

import org.gradle.api.Project;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

/**
 * Created by ljj on 2017/6/24.
 */

public class ByteCodeUtil {
  private static ClassPool pool=ClassPool.getDefault();
  static Project project;
  static String packageName = "";

  public static
  void setProject(Project pro){
    project=pro;
  }

  public  static void inject(String path, String packageN) {

    try {
      //project.getLogger().error("inject");
      packageName=packageN;
      pool.appendClassPath(path);
      //project.getLogger().error("traverseFolder");
      traverseFolder(pool,new File(path));
    } catch (Exception e) {
     // project.getLogger().error(e.getMessage());
      e.printStackTrace();
    }

  }

  private static String injectStr = "System.out.println(\"I Love ljj\" ); ";

  public static void traverseFolder(ClassPool pool,File rootFile) throws NotFoundException, CannotCompileException, IOException {
    //        Log.d("traverseFolder:%s", rootFile);
    if (rootFile != null && rootFile.exists()) {
      File[] files = rootFile.listFiles();
      if (files == null || files.length == 0) {
        return;
      } else {
        for (File innerFile : files) {
          if (innerFile.isDirectory()) {
            traverseFolder(pool,innerFile);
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
