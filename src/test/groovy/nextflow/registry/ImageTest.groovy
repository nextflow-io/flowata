package nextflow.registry

import spock.lang.Specification

/**
 * @author Emilio Palumbo <emiliopalumbo@gmail.com>
 */
class ImageTest extends Specification {

    def "ToString"() {

        expect:
        image.toString() == name

        where:
        image || name
        new Image('nextflow/rnatoy') || 'index.docker.io-nextflow-rnatoy-latest.img'
        new Image('ubuntu') || 'index.docker.io-library-ubuntu-latest.img'
        new Image('docker://nextflow/rnatoy') || 'index.docker.io-nextflow-rnatoy-latest.img'
        new Image('docker://my.registry.com:8080/nextflow/rnatoy') || 'my.registry.com-nextflow-rnatoy-latest.img'
        new Image('docker://my.registry.com/nextflow/rnatoy') || 'my.registry.com-nextflow-rnatoy-latest.img'
        new Image('nextflow/rnatoy:1.1') || 'index.docker.io-nextflow-rnatoy-1.1.img'
        new Image('nextflow/rnatoy@sha256:aef4b1') || 'index.docker.io-nextflow-rnatoy-sha256-aef4b1.img'
        new Image('shub://nextflow/rnatoy:master') || 'singularity-hub.org-nextflow-rnatoy-master.img'
        new Image('shub://nextflow/rnatoy') || 'singularity-hub.org-nextflow-rnatoy-master.img'

    }

}

