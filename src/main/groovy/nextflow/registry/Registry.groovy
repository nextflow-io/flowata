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
    private static final Tuple DEFAULT_PULL_COMMAND = new Tuple("singularity", "pull")

    Path registryPath
    Tuple pullCommand
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
        this.pullCommand = DEFAULT_PULL_COMMAND
    }

    def Image getImage(String s) {
        if (!imageStatus.containsKey(s)) {
            imageStatus[s] = new Image(s)
        }
        def img = imageStatus[s]
        if (Files.exists(getImagePath(img))) {
            img.status = Image.Status.CACHED
        }
        return imageStatus[s]
    }

    def Path getImagePath(Image image) {
        return Paths.get(registryPath.toString(), image.toString())
    }

    def boolean hasImage(String image) {
        def img = getImage(image)
        if (img.status == Image.Status.CACHED) {
            return true
        }
        return false
    }

    def pullImage(String s) {
        def image = getImage(s)
        this.pullImage(image)
    }

    def pullImage(Image image) {
        if (!Files.exists(registryPath) || !Files.isDirectory(registryPath)) {
            Files.createDirectory(registryPath)
        }
        def builder = new ProcessBuilder()
                .command(*pullCommand, image.toUrl())
                .redirectErrorStream(true)
                .directory(registryPath.toFile())

        def process
        try {
            process = builder.start()
        } catch (IOException e) {
            image.status = Image.Status.ERROR
            image.statusInfo = e.message
        }

        if (process) {
            //process.inputStream.eachLine { println it }
            process.waitFor()

            image.statusInfo = process.text

            if (process.exitValue()) {
                image.status = Image.Status.ERROR
            } else {
                image.status = Image.Status.CACHED
            }
        }

        imageStatus[image.toUrl()] = image

        return (image.status == Image.Status.CACHED)

    }
}
