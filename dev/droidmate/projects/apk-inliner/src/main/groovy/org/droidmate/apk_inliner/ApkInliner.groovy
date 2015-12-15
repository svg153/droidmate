// Copyright (c) 2013-2015 Saarland University
// All right reserved.
//
// Author: Konrad Jamrozik, jamrozik@st.cs.uni-saarland.de
//
// This file is part of the "DroidMate" project.
//
// www.droidmate.org

package org.droidmate.apk_inliner

import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.droidmate.common.Dex
import org.droidmate.common.ISysCmdExecutor
import org.droidmate.common.Jar

import java.nio.file.Path
import java.nio.file.StandardCopyOption

import static java.nio.file.Files.*

@TypeChecked
@Slf4j
class ApkInliner implements IApkInliner
{
  private final ISysCmdExecutor   sysCmdExecutor
  private final IJarsignerWrapper jarsignerWrapper

  private final Jar    inlinerJar
  private final Dex    appGuardLoader
  private final String monitorClassName
  private final String pathToMonitorApkOnAndroidDevice

  ApkInliner(
    ISysCmdExecutor sysCmdExecutor,
    IJarsignerWrapper jarsignerWrapper,
    Jar inlinerJar,
    Dex appGuardLoader,
    String monitorClassName,
    String pathToMonitorApkOnAndroidDevice)
  {
    this.sysCmdExecutor = sysCmdExecutor
    this.jarsignerWrapper = jarsignerWrapper
    this.inlinerJar = inlinerJar
    this.appGuardLoader = appGuardLoader
    this.monitorClassName = monitorClassName
    this.pathToMonitorApkOnAndroidDevice = pathToMonitorApkOnAndroidDevice
  }


  @Override
  void inline(Path inputPath, Path outputDir)
  {
    assert inputPath != null
    assert outputDir != null
    if (!isDirectory(inputPath))
      assert new ApkPath(inputPath)
    assert isDirectory(outputDir)

    if (isDirectory(inputPath))
    {
      if (list(inputPath).count() == 0)
      {
        log.warn("No target apks for inlining found. Searched directory: ${inputPath.toRealPath().toString()}.\nAborting inlining.");
        return
      }

      (list(inputPath).collect() as Collection<Path>)
        .findAll { Path p -> p.fileName.toString() != ".gitignore" }
        .each {Path apkPath -> inlineApkIntoDir(apkPath, outputDir)}

      assert list(inputPath)
        .findAll { Path p -> p.extension == "apk" }
        .size() <=
        list(outputDir)
          .findAll { Path p -> p.extension == "apk" }
          .size()
    }
    else
      inlineApkIntoDir(inputPath, outputDir)
  }

/**
 * <p>
 * Inlines apk at path {@code apkPath} and puts its inlined version in {@code outputDir}.
 *
 * </p><p>
 * For example, if {@code apkPath} is:
 *
 *   /abc/def/calc.apk
 *
 * and {@code outputDir} is:
 *
 *   /abc/def/out/
 *
 * then the output inlined apk will have path
 *
 *   /abc/def/out/calc-inlined.apk
 *
 * </p>
 *
 * @param apkPath
 * @param outputDir
 * @return
 */
  private ApkPath inlineApkIntoDir(Path apkPath, Path outputDir)
  {
    ApkPath apk = new ApkPath(apkPath)

    ApkPath unsignedInlinedApk = executeInlineApk(apk)
    assert unsignedInlinedApk.name.endsWith("-inlined.apk")

    ApkPath signedInlinedApk = jarsignerWrapper.signWithDebugKey(unsignedInlinedApk)

    Path signedInlinedApkPathAfterMove = move(signedInlinedApk.path, outputDir.resolve(signedInlinedApk.name),
      StandardCopyOption.REPLACE_EXISTING)
    signedInlinedApk = new ApkPath(signedInlinedApkPathAfterMove)

    return signedInlinedApk
  }

  private ApkPath executeInlineApk(/* in */ ApkPath targetApk)
  {
    Path inlinedApkPath = targetApk.resolveSibling(targetApk.fileName.toString().replace(".apk", "-inlined.apk"))
    assert notExists(inlinedApkPath)

    sysCmdExecutor.execute(
      "Inlining ${targetApk.toRealPath().toString()}",
      "java -jar",
      inlinerJar.toRealPath().toString(),
      targetApk.toRealPath().toString(),
      appGuardLoader.toRealPath().toString(),
      pathToMonitorApkOnAndroidDevice,
      monitorClassName)

    assert exists(inlinedApkPath)
    return new ApkPath(inlinedApkPath)
  }
}
