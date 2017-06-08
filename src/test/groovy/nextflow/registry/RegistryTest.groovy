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
        ['registry/busybox.img', 'registry'].each {
            Files.deleteIfExists(Paths.get(it))
        }
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

    def "Create image from string"() {
        given:
        def reg = new Registry()

        expect:
        reg.getImage("docker://nextflow/rnatoy") as String == "rnatoy.img"
        reg.getImage("docker://nextflow/rnatoy").proto == "docker"
        reg.getImage("shub://nextflow/rnatoy") as String == "rnatoy.img"
        reg.getImage("shub://nextflow/rnatoy").proto == "shub"
        reg.getImage("nextflow/rnatoy") as String == "rnatoy.img"
        reg.getImage("nextflow/rnatoy").repo == "nextflow"
        reg.getImage("nextflow/rnatoy:1.3") as String == "rnatoy-1.3.img"
        reg.getImage("nextflow/rnatoy@c4fbc65") as String == "rnatoy@sha256-c4fbc65.img"
        reg.getImage("/path/to/rnatoy@sha256-c4fbc65.img") as String == "/path/to/rnatoy@sha256-c4fbc65.img"
        reg.getImage("/path/to/rnatoy@sha256-c4fbc65.img").repo == null
        reg.getImage("nextflow/rnatoy").proto == null

    }

    def "Return image url"() {

        when:
        def reg = new Registry()

        then:
        reg.getImage("docker://nextflow/rnatoy").toUrl() == "docker://nextflow/rnatoy"
        reg.getImage("nextflow/rnatoy").toUrl() == "nextflow/rnatoy"
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
        reg.getImage('library/busybox').statusInfo == '== Pull image library/busybox\n== DONE\n'

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
