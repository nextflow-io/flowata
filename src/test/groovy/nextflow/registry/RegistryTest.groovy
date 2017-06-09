package nextflow.registry

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Emilio Palumbo <emiliopalumbo@gmail.com>
 */
class RegistryTest extends Specification {

    // Cleanyp registry folder
    def setupSpec() {
        def regDir = Paths.get('registry')
        Files.list(regDir).each {
            Files.delete(it)
        }
        Files.delete(regDir)
    }

    def "Create new Registry"() {

        when:
        def reg = new Registry()
        def cwd = Paths.get('.').toRealPath()

        then:
        reg.registryPath == Paths.get(cwd.toString(), 'registry')

        when:
        reg = new Registry("/path/to/file")

        then:
        reg.registryPath == Paths.get("/path/to/file")

        when:
        def path = Paths.get("/path/to/file")
        reg = new Registry(path)

        then:
        reg.registryPath == path

    }

    def "Get image path"() {
        given:
        def reg = new Registry()
        def regPath = Paths.get('.').toRealPath().resolve('registry')

        expect:
        reg.getImagePath("docker://nextflow/rnatoy") == regPath.resolve("index.docker.io-nextflow-rnatoy-latest.img")
        reg.getImagePath("shub://nextflow/rnatoy") == regPath.resolve("singularity-hub.org-nextflow-rnatoy-master.img")
        reg.getImagePath("nextflow/rnatoy") == regPath.resolve("index.docker.io-nextflow-rnatoy-latest.img")
        reg.getImagePath("nextflow/rnatoy:1.3") == regPath.resolve("index.docker.io-nextflow-rnatoy-1.3.img")
        reg.getImagePath("nextflow/rnatoy@sha256:c4fbc65") == regPath.resolve("index.docker.io-nextflow-rnatoy-sha256-c4fbc65.img")
        reg.getImagePath("/path/to/rnatoy@sha256-c4fbc65.img") == Paths.get("/path/to/rnatoy@sha256-c4fbc65.img")
    }

    def "Return image url"() {

        when:
        def reg = new Registry()

        then:
        reg.getImage("docker://nextflow/rnatoy").toUrl() == "docker://nextflow/rnatoy"
        reg.getImage("nextflow/rnatoy").toUrl() == "docker://nextflow/rnatoy"
        //reg.getImage("/path/to/rnatoy@sha256-c4fbc65.img").toUrl() == "/path/to/rnatoy@sha256-c4fbc65.img"

    }

    def "Pull image"() {

        when:
        def reg = new Registry()

        then:
        !reg.pullImage('nextflow/rnatoy')
        reg.getImage('nextflow/rnatoy').status == Image.Status.ERROR
        reg.getImage('nextflow/rnatoy').statusInfo == 'Cannot run program "singularity" (in directory "/Users/emilio/workspace/sregistry/registry"): error=13, Permission denied'

        when:
        reg.pullCommand = new Tuple("../scripts/singularity")

        then:
        !reg.pullImage('library/busybox')
        reg.getImage('library/busybox').status == Image.Status.ERROR
        reg.getImage('library/busybox').statusInfo == 'usage \'singularity pull <image>\'\n'

        when:
        reg.pullCommand = new Tuple("../scripts/singularity", "pull")

        then:
        reg.pullImage('library/busybox')
        reg.getImage('library/busybox').status == Image.Status.CACHED
        reg.getImage('library/busybox').statusInfo == '== Pull image docker://library/busybox\n== DONE\n'

    }

    def "Check image status"() {


        expect:
        image.status == status

        where:
        image                                      || status
        new Registry().getImage('nextflow/rnatoy') || Image.Status.CREATED
        new Registry().getImage('library/busybox') || Image.Status.CACHED

    }

    def "Check if image is cached"() {

        when:
        def reg = new Registry()

        then:
        reg.hasImage('library/busybox')
        !reg.hasImage('nextflow/rnatoy')

    }

}
