package nextflow.registry

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Emilio Palumbo <emiliopalumbo@gmail.com>
 */
class Registry {

    private static final Path DEFAULT_DIR = Paths.get(Paths.get(".").toRealPath().toString(), "registry")

    Path registryPath
    String pullCommand
    ConcurrentHashMap<String, Image> imageStatus

    Registry() {
        this(DEFAULT_DIR)
    }

    Registry(String path) {
        this(Paths.get(path))
    }

    Registry(Path path) {
        this.registryPath = path
        this.imageStatus = new ConcurrentHashMap<String,Image>()
        this.pullCommand = "singularity"
    }

    def Image makeImage(String s) {
        return new Image(s)
    }

    def boolean hasImage(String image) {
        return imageStatus.containsKey(image) && new File(registryPath, makeImage(image).toString()).exists()
    }

    def pullImage(String s) {
        def image = this.makeImage(s)
        this.pullImage(image)
    }

    def pullImage(Image image) {
        if (!Files.exists(this.registryPath) || !Files.isDirectory(this.registryPath)) {
            Files.createDirectory(this.registryPath)
        }
        def builder = new ProcessBuilder()
        .command(this.pullCommand, 'pull', image.toUrl())
        .redirectErrorStream(true)
        .directory(this.registryPath)

        def process = builder.start()
        process.inputStream.eachLine {println it}
        process.waitFor()

        if (process.exitValue()) {
            throw new RuntimeException(process.text)
        }

        return true
    }
}
