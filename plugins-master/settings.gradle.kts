rootProject.name = "Numb"
include("NUtils")

include("NQuickEat")
include("NQuickPray")
include("NGatherer")
include("NQuickPot")
include("NQuickFighter")
include("aconstruction")
include("apest")
include("apker")
include("abankstander")

include("IronBuilder")
//include("NTempoross")
//include("NGuardians")
//include("AVorkath")
//include("ARunedragons")


//include("NGauntlet")
//include("arooftops")


for (project in rootProject.children) {
    project.apply {
        projectDir = file(name)
        buildFileName = "$name.gradle.kts"

       require(projectDir.isDirectory) { "Project '${project.path} must have a $projectDir directory" }
        require(buildFile.isFile) { "Project '${project.path} must have a $buildFile build script" }
    }
}