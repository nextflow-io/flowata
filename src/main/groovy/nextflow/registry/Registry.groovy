package nextflow.registry

import java.util.concurrent.ConcurrentHashMap

/**
 * @author Emilio Palumbo <emiliopalumbo@gmail.com>
 */
class Registry {

    private static final File DEFAULT_DIR = new File(new File(".").getCanonicalPath(), "registry")

    File registryPath
    String pullCommand
    ConcurrentHashMap<String, Image> imageStatus

    Registry() {
        this(DEFAULT_DIR)
    }

    Registry(String path) {
        this(new File(path))
    }

    Registry(File path) {
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
        if (!this.registryPath.exists() ||  !this.registryPath.directory) {
            this.registryPath.mkdirs()
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
