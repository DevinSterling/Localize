plugins {
    `java-library`
}

abstract class TestConventionExtension @Inject constructor(private val project: Project) {
    abstract val testModuleNames: SetProperty<String>

    fun reflectiveTestImplementation(dependency: Provider<MinimalExternalModuleDependency>) {
        reflectiveTestImplementation(dependency, dependency.get().group)
    }

    fun reflectiveTestImplementation(dependency: Any, moduleName: String) {
        project.dependencies.testImplementation(dependency)
        testModuleNames.add(moduleName)
    }
}

val extension = extensions.create<TestConventionExtension>("testConvention")

/** Retrieves the module name dynamically */
val moduleName = provider {
    val moduleNamepattern = Regex("""module\s+([a-zA-Z0-9._]+)\s+\{""")
    val moduleInfo = file("src/main/java/module-info.java")
    moduleNamepattern.find(moduleInfo.readText())?.groupValues?.get(1)
}

val mainJavaDir = layout.projectDirectory.dir("src/main/java")

/** Retrieves all packages dynamically */
val packages = fileTree(mainJavaDir) {
    // Compared to `**/*.java`, `*/**/*.java` skips the `src/main/java` directory itself since it isn't needed
    include("*/**/*.java")
}.elements.map { files ->
    files.mapTo(mutableSetOf()) {
        val packageDir = it.asFile.parentFile.relativeTo(mainJavaDir.asFile)
        // Convert to package notation
        packageDir.path.replace(File.separatorChar, '.')
    }
}

tasks.compileTestJava {
    options.compilerArgumentProviders.add(CommandLineArgumentProvider {
        val allTargets = extension.testModuleNames.get().joinToString(",")
        if (allTargets.isEmpty()) return@CommandLineArgumentProvider emptyList()

        listOf(
            "--add-modules", allTargets,
            "--add-reads", "${moduleName.get()}=$allTargets"
        )
    })
}

tasks.test {
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        val allTargets = extension.testModuleNames.get().joinToString(",")
        if (allTargets.isEmpty()) return@CommandLineArgumentProvider emptyList()

        // Allow the project to access the target test modules
        val args = mutableListOf("--add-reads", "${moduleName.get()}=$allTargets")

        // Allow test modules deep reflection access on all packages
        for (targetPackage in packages.get()) {
            for (targetModule in extension.testModuleNames.get()) {
                args.add("--add-opens")
                args.add("${moduleName.get()}/$targetPackage=$targetModule")
            }
        }

        args
    })
}
