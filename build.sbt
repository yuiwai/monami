organization in ThisBuild := "com.yuiwai"
version in ThisBuild := "0.1.0"
scalaVersion in ThisBuild := "2.12.4"
crossScalaVersions in ThisBuild := Seq("2.11.11", "2.12.4")

lazy val core = (project in file("core"))
  .settings(
    name := "monami-core"
  )

lazy val example = (project in file("example"))
  .settings(
    name := "monami-example"
  )
  .dependsOn(core)
