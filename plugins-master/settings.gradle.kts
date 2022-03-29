rootProject.name = "Numb"
include("NQuickEat")
include("NQuickPray")
include("NGatherer")
include("NFarming")
include("NTempoross")
include("NQuickPot")
include("NQuickFighter")
//include("NInfernoHelper")
include("NUtils")
include("NGauntlet")
include("NGuardians")
//include("NumbSuite")

for (project in rootProject.children) {
    project.apply {
        projectDir = file(name)
        buildFileName = "$name.gradle.kts"

       require(projectDir.isDirectory) { "Project '${project.path} must have a $projectDir directory" }
        require(buildFile.isFile) { "Project '${project.path} must have a $buildFile build script" }
    }
}