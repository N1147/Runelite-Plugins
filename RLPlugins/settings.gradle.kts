rootProject.name = "Numb"
//include("Utils")
include("NGuardians")
include("NGatherer")
include("AVorkath")
include("ATempoross")
//include("AHotkeys")
include("autologhop")
include("leftclickloot")
include("NQuickFighter")
//include("zulrah") // Requires resources. I'm too lazy
for (project in rootProject.children) {
    project.apply {
        projectDir = file(name)
        buildFileName = "$name.gradle.kts"

       require(projectDir.isDirectory) { "Project '${project.path} must have a $projectDir directory" }
        require(buildFile.isFile) { "Project '${project.path} must have a $buildFile build script" }
    }
}