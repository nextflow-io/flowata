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
    private static final Tuple DEFAULT_PULL_COMMAND = new Tuple("singularity", "pull", "--name")

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
            def image = Image.create(s)
            if (image) {
                imageStatus[s] = image
            }
        }
        def img = imageStatus[s]
        if (img && Files.exists(getImagePath(img))) {
            img.status = Image.Status.CACHED
        }
        return imageStatus[s]
    }

    def Path getImagePath(Image image) {
        if (image) {
            if (image.name.startsWith('/')) {
                return Paths.get(image.toString())
            }
            return registryPath.resolve(image.toString())
        }
        return null
    }

    def Path getImagePath(String s) {
        getImagePath(getImage(s) as Image)
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
            Files.createDirectories(registryPath)
        }
        def builder = new ProcessBuilder()
                .command(*pullCommand, image.toString(), image.toUrl())
                .redirectErrorStream(true)

                .directory(registryPath.toFile())

        def process
        try {
            process = builder.start()
        } catch (IOException e) {
            image.status = Image.Status.ERROR
            image.statusInfo = e.message
            // log message
        }

        if (process) {
            //process.inputStream.eachLine { println it }
            process.waitFor()

            image.statusInfo = process.text

            if (process.exitValue()) {
                image.status = Image.Status.ERROR
                // log message
            } else {
                image.status = Image.Status.CACHED
            }
        }

        imageStatus[image.toUrl()] = image

        return (image.status == Image.Status.CACHED)

    }
}
