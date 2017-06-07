package nextflow.registry

import spock.lang.Specification

/**
 * @author Emilio Palumbo <emiliopalumbo@gmail.com>
 */
class RegistryTest extends Specification {
    def "Create new Registry"() {

        when:
        def reg = new Registry()
        def cwd = new File('.').getCanonicalPath()

        then:
        reg.registryPath == new File(cwd, 'registry')

        when:
        reg = new Registry("/path/to/file")

        then:
        reg.registryPath == new File("/path/to/file")

        when:
        def path = new File("/path/to/file")
        reg = new Registry(path)

        then:
        reg.registryPath == path

    }

    def "Create image from string"() {
        given:
        def reg = new Registry()

        expect:
        reg.makeImage("docker://nextflow/rnatoy") as String == "rnatoy.img"
        reg.makeImage("docker://nextflow/rnatoy").proto == "docker"
        reg.makeImage("shub://nextflow/rnatoy") as String == "rnatoy.img"
        reg.makeImage("shub://nextflow/rnatoy").proto == "shub"
        reg.makeImage("nextflow/rnatoy") as String == "rnatoy.img"
        reg.makeImage("nextflow/rnatoy").repo == "nextflow"
        reg.makeImage("nextflow/rnatoy:1.3") as String == "rnatoy-1.3.img"
        reg.makeImage("nextflow/rnatoy@c4fbc65") as String == "rnatoy@sha256-c4fbc65.img"
        reg.makeImage("/path/to/rnatoy@sha256-c4fbc65.img") as String == "/path/to/rnatoy@sha256-c4fbc65.img"
        reg.makeImage("/path/to/rnatoy@sha256-c4fbc65.img").repo == null
        reg.makeImage("nextflow/rnatoy").proto == null

    }

    def "Return image url"() {

        when:
        def reg = new Registry()

        then:
        reg.makeImage("docker://nextflow/rnatoy").toUrl() == "docker://nextflow/rnatoy"
        reg.makeImage("nextflow/rnatoy").toUrl() == "nextflow/rnatoy"
        //reg.makeImage("/path/to/rnatoy@sha256-c4fbc65.img").toUrl() == "/path/to/rnatoy@sha256-c4fbc65.img"

    }

    def "Check image status"() {

        given:
        def image = new Registry().makeImage('nextflow/rnatoy')

        expect:
        image.status == Image.Status.CREATED

        when:
        image.status = Image.Status.ERROR

        then:
        image.status == Image.Status.ERROR

    }

    def "Check if image is cached"() {

        when:
        def reg = new Registry()

        then:
        reg.hasImage('nextflow/rnatoy') == false

    }

    def "Pull image"() {

        when:
        def reg = new Registry()
        reg.pullCommand = "../scripts/singularity"

        then:
        reg.pullImage('library/busybox') == true

    }
}
