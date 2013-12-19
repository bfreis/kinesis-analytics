logLevel := Level.Warn

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0-SNAPSHOT")

resolvers += "Play2war plugins release" at "http://repository-play-war.forge.cloudbees.com/release/"

addSbtPlugin("com.github.play2war" % "play2-war-plugin" % "1.2-beta2")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.1")

