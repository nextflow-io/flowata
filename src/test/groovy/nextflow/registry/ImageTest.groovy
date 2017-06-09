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
        Image.create('nextflow/rnatoy') || 'index.docker.io-nextflow-rnatoy-latest.img'
        Image.create('ubuntu') || 'index.docker.io-library-ubuntu-latest.img'
        Image.create('docker://nextflow/rnatoy') || 'index.docker.io-nextflow-rnatoy-latest.img'
        Image.create('docker://my.registry.com:8080/nextflow/rnatoy') || 'my.registry.com-nextflow-rnatoy-latest.img'
        Image.create('docker://my.registry.com/nextflow/rnatoy') || 'my.registry.com-nextflow-rnatoy-latest.img'
        Image.create('nextflow/rnatoy:1.1') || 'index.docker.io-nextflow-rnatoy-1.1.img'
        Image.create('nextflow/rnatoy@sha256:aef4b1') || 'index.docker.io-nextflow-rnatoy-sha256-aef4b1.img'
        Image.create('shub://nextflow/rnatoy:master') || 'singularity-hub.org-nextflow-rnatoy-master.img'
        Image.create('shub://nextflow/rnatoy') || 'singularity-hub.org-nextflow-rnatoy-master.img'

    }

}

