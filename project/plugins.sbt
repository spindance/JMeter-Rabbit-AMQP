lazy val root = project.in( file(".") ).dependsOn( sbtGitTagsPlugin )

lazy val sbtGitTagsPlugin = uri("ssh://git@github.com/spindance/sbt-git-tags.git#release/1.1.0.0")
