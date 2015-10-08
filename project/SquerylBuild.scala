import sbt._
import Keys._
//import ls.Plugin._

object SquerylBuild extends Build {

  lazy val squeryl = Project(
    id = "squeryl",
    base = file("."),
    settings = Project.defaultSettings /* ++ lsSettings */ ++ Seq(
      description := "A Scala ORM and DSL for talking with Databases using minimum verbosity and maximum type safety",
      organization := "org.squeryl",
      version := "0.9.6-RC4",
      javacOptions := Seq("-source", "1.6", "-target", "1.6"),
  	  version <<= version { v => //only release *if* -Drelease=true is passed to JVM
  	  	val release = Option(System.getProperty("release")) == Some("true")
  	  	if(release)
  	  		v 
  	  	else {	
  	  		val suffix = Option(System.getProperty("suffix"))
  	  		val i = (v.indexOf('-'), v.length) match {
  	  		  case (x, l) if x < 0 => l
  	  		  case (x, l) if v substring (x+1) matches """\d+""" => l //patch level, not RCx
  	  		  case (x, _) => x
  	  		}
  	  		v.substring(0,i) + "-" + (suffix getOrElse "SNAPSHOT")
  	  	}
  	  },
      parallelExecution := false,
      publishMavenStyle := true,
      scalaVersion := "2.11.1",
      crossScalaVersions := Seq("2.11.1", "2.10.3"),
      scalacOptions <++= scalaVersion map { sv =>
        Seq("-unchecked", "-deprecation") ++ (
          if(sv.startsWith("2.11"))
            Seq("-feature",
            "-language:implicitConversions",
            "-language:postfixOps",
            "-language:reflectiveCalls",
            "-language:existentials")
          else
            Nil
          )
      },
      licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
      homepage := Some(url("http://squeryl.org")),
      pomExtra := (<scm>
                     <url>git@github.com:squeryl/squeryl.git</url>
                     <connection>scm:git:git@github.com:squeryl/squeryl.git</connection>
                   </scm>
                   <developers>
                     <developer>
                       <id>max-l</id>
                       <name>Maxime Lévesque</name>
                       <url>https://github.com/max-l</url>
                     </developer>
                     <developer>
                       <id>davewhittaker</id>
                       <name>Dave Whittaker</name>
                       <url>https://github.com/davewhittaker</url>
                     </developer>
                   </developers>),
      publishTo <<= version { v => //add credentials to ~/.sbt/sonatype.sbt
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      publishArtifact in Test := false,
      pomIncludeRepository := { _ => false },
      //below is for lsync, run "ls-write-version", commit to github, then run "lsync" 
      /*
			  (LsKeys.tags in LsKeys.lsync) := Seq("sql", "orm", "query", "database", "db", "dsl"),
			  (LsKeys.docsUrl in LsKeys.lsync) := Some(new URL("http://squeryl.org/api/")),
			  (LsKeys.ghUser in LsKeys.lsync) := Some("squeryl"),
        */
      libraryDependencies ++= Seq(
        "cglib" % "cglib-nodep" % "2.2",
        "com.h2database" % "h2" % "1.2.127" % "provided",
        "mysql" % "mysql-connector-java" % "5.1.10" % "provided",
        "postgresql" % "postgresql" % "8.4-701.jdbc4" % "provided",
        "net.sourceforge.jtds" % "jtds" % "1.2.4" % "provided",
        "org.apache.derby" % "derby" % "10.7.1.1" % "provided",
        "org.xerial" % "sqlite-jdbc" % "3.8.7" % "test",
        "junit" % "junit" % "4.8.2" % "provided"
      ),
      libraryDependencies <++= scalaVersion { sv =>
        Seq("org.scala-lang" % "scalap" % sv,
          sv match {
            case sv if sv startsWith "2.11" =>
              "org.scalatest" %% "scalatest" % "2.1.3" % "test"
            case sv if sv startsWith "2.10" =>
              "org.scalatest" %% "scalatest" % "2.1.3" % "test"
            case _ =>
              "org.scalatest" % "scalatest_2.9.2" % "2.0.M6-SNAP3" % "test"
          }
        )
      }))
}
