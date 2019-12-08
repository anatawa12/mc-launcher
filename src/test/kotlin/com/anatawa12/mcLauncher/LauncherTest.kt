package com.anatawa12.mcLauncher

import com.anatawa12.mcLauncher.json.Library
import com.anatawa12.mcLauncher.json.VersionJson
import com.anatawa12.mcLauncher.launchInfo.Artifact
import com.anatawa12.mcLauncher.launchInfo.LaunchInfo
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.*

class LauncherTest {
    lateinit var appDataDir: File
    lateinit var profile: Profile

    @Before
    fun before() {
        fun File.pdirs(): File = apply { parentFile.mkdirs() }
        appDataDir = Files.createTempDirectory(System.getProperty("java.io.tempdir")).toFile()
        val versions = appDataDir.resolve("versions")
        versions.resolve("looping1/looping1.json").pdirs().writeText(
            """
{
  "assets": "looping1",
  "id": "looping1",
  "libraries": [],
  "inheritsFrom": "looping2",
  "mainClass": "MainClass",
  "minecraftArguments": "arguments",
  "minimumLauncherVersion": 13,
  "releaseTime": "2014-05-14T17:29:23+00:00",
  "time": "2014-05-14T17:29:23+00:00",
  "type": "release"
}
        """.trimIndent()
        )
        versions.resolve("looping2/looping2.json").pdirs().writeText(
            """
{
  "assets": "looping2",
  "id": "looping1",
  "libraries": [],
  "inheritsFrom": "looping1",
  "mainClass": "MainClass",
  "minecraftArguments": "arguments",
  "minimumLauncherVersion": 13,
  "releaseTime": "2014-05-14T17:29:23+00:00",
  "time": "2014-05-14T17:29:23+00:00",
  "type": "release"
}
        """.trimIndent()
        )
        versions.resolve("invalidJsonData1/invalidJsonData1.json").pdirs().writeText(
            """
{
  "libraries": [],
  "mainClass": "MainClass",
  "minecraftArguments": "arguments",
  "minimumLauncherVersion": 13,
  "releaseTime": "2014-05-14T17:29:23+00:00",
  "time": "2014-05-14T17:29:23+00:00",
  "type": "release"
}
        """.trimIndent()
        )
        versions.resolve("invalidJson1/invalidJson1.json").pdirs().writeText(
            """
{
invalid json data here!
}
        """.trimIndent()
        )

        versions.resolve("validJsonData1/validJsonData1.json").pdirs().writeText(
            """
{
  "id": "validJsonData1",
  "libraries": [],
  "mainClass": "MainClass",
  "minecraftArguments": "arguments",
  "minimumLauncherVersion": 13,
  "releaseTime": "2014-05-14T17:29:23+00:00",
  "time": "2014-05-14T17:29:23+00:00",
  "type": "release"
}
        """.trimIndent()
        )

        versions.resolve("inheritingJsonData1/inheritingJsonData1.json").pdirs().writeText(
            """
{
  "id": "inheritingJsonData1",
  "libraries": [],
  "inheritsFrom": "validJsonData1",
  "mainClass": "MainClass",
  "minecraftArguments": "arguments",
  "minimumLauncherVersion": 13,
  "releaseTime": "2014-05-14T17:29:23+00:00",
  "time": "2014-05-14T17:29:23+00:00",
  "type": "release"
}
        """.trimIndent()
        )

        profile = Profile(
            version = "null",
            platform = Platform.MacOS,
            appDataDirPath = appDataDir.path
        )
    }

    @After
    fun after() {
        appDataDir.walkBottomUp().forEach { it.delete() }
    }

    @Test(expected = KnownErrorException.InvalidVersionJsonData::class)
    fun loadLaunchInfoLooping() {
        Launcher(profile).loadLaunchInfo("looping2")
    }

    @Test(expected = KnownErrorException.VersionJsonNotFile::class)
    fun loadVersionJsonNotFound() {
        Launcher(profile).loadVersionJson("notfound")
    }

    @Test(expected = KnownErrorException.InvalidVersionJsonData::class)
    fun loadVersionJsonInvalidJsonData() {
        Launcher(profile).loadVersionJson("invalidJsonData1")
    }

    @Test(expected = KnownErrorException.InvalidVersionJson::class)
    fun loadVersionJsonInvalidJson() {
        Launcher(profile).loadVersionJson("invalidJson1")
    }

    @Test
    fun loadVersionJson() {
        val json = Launcher(profile).loadVersionJson("inheritingJsonData1")
        assertEquals(json.id, "inheritingJsonData1")
    }

    @Test
    fun getLoadArtifacts() {
        val loadArtifacts = Launcher(profile)
            .getLoadArtifacts(
                LaunchInfo.Builder("jsonName").apply {
                    addVersionJson(
                        VersionJson(
                            id = "versionJson1",
                            releaseTime = Date(),
                            time = Date(),
                            type = "release",
                            minecraftArguments = "minecraftArguments",
                            mainClass = "somePackage.MainClassName",
                            libraries = listOf(
                                Library(
                                    name = "com.mojang:netty:1.6"
                                ),
                                Library(
                                    name = "net.minecraftforge:forge:1.9.2-14.23.5.2795"
                                )
                            )
                        )
                    )
                    addVersionJson(
                        VersionJson(
                            id = "versionJson1",
                            releaseTime = Date(),
                            time = Date(),
                            type = "release",
                            minecraftArguments = "minecraftArguments",
                            mainClass = "somePackage.MainClassName",
                            libraries = listOf(
                                Library(
                                    name = "net.minecraftforge:forge:1.7.10-10.13.4.1614-1.7.10"
                                ),
                                Library(
                                    name = "net.minecraftforge:forge:1.9.2-14.23.5.2795"
                                )
                            )
                        )
                    )
                }.build()
            ).toList()
        assertEquals(
            "net/minecraftforge/forge/1.7.10-10.13.4.1614-1.7.10/forge-1.7.10-10.13.4.1614-1.7.10.jar",
            loadArtifacts[0].path
        )
        assertEquals(
            "com/mojang/netty/1.6/netty-1.6.jar",
            loadArtifacts[1].path
        )
        assertEquals(
            "net/minecraftforge/forge/1.9.2-14.23.5.2795/forge-1.9.2-14.23.5.2795.jar",
            loadArtifacts[2].path
        )
    }

    @Test
    fun createClassPath() {
        val main = spyk(Launcher(profile))
        val launchInfo =
            LaunchInfo.Builder("jsonName").apply {
                addVersionJson(
                    VersionJson(
                        id = "versionJson1",
                        releaseTime = Date(),
                        time = Date(),
                        type = "release",
                        minecraftArguments = "minecraftArguments",
                        mainClass = "somePackage.MainClassName",
                        libraries = listOf(
                            Library(
                                name = "com.mojang:netty:1.6"
                            ),
                            Library(
                                name = "net.minecraftforge:forge:1.9.2-14.23.5.2795"
                            )
                        )
                    )
                )
                addVersionJson(
                    VersionJson(
                        id = "versionJson1",
                        releaseTime = Date(),
                        time = Date(),
                        type = "release",
                        minecraftArguments = "minecraftArguments",
                        mainClass = "somePackage.MainClassName",
                        libraries = listOf(
                            Library(
                                name = "net.minecraftforge:forge:1.7.10-10.13.4.1614-1.7.10"
                            ),
                            Library(
                                name = "net.minecraftforge:forge:1.9.2-14.23.5.2795"
                            )
                        )
                    )
                )
            }.build()
        every {
            main.getLoadArtifacts(launchInfo)
        } returns sequenceOf(
            Artifact("net/minecraftforge/forge/1.7.10-10.13.4.1614-1.7.10/forge-1.7.10-10.13.4.1614-1.7.10.jar", ""),
            Artifact("com/mojang/netty/1.6/netty-1.6.jar", ""),
            Artifact("net/minecraftforge/forge/1.9.2-14.23.5.2795/forge-1.9.2-14.23.5.2795.jar", "")
        )

        val replaced = appDataDir.path.replace(File.pathSeparatorChar, ':').replace(File.separatorChar, '/')
        assertEquals(
            "$replaced/libraries/net/minecraftforge/forge/1.7.10-10.13.4.1614-1.7.10/forge-1.7.10-10.13.4.1614-1.7.10.jar:$replaced/libraries/com/mojang/netty/1.6/netty-1.6.jar:" +
                    "$replaced/libraries/net/minecraftforge/forge/1.9.2-14.23.5.2795/forge-1.9.2-14.23.5.2795.jar:$replaced/versions/jsonName/jsonName.jar",
            main.createClassPath(launchInfo).replace(File.pathSeparatorChar, ':').replace(File.separatorChar, '/')
        )

        verify(exactly = 1) { main.getLoadArtifacts(launchInfo) }
    }
}
