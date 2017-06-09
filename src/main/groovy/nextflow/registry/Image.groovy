package nextflow.registry

import java.nio.file.Path

/**
 * @author Emilio Palumbo <emiliopalumbo@gmail.com>
 */
class Image {
    enum Status {
        PENDING, CACHED, ERROR, CREATED
    }

    enum Protocol {
        DOCKER, SHUB
    }

    final SUFFIX = ".img"

    String proto, origin, repo, name, tag
    List statusInfo // statusInfo as limited size list e.g. last 10 attempts
    // lastAccessed, countUsed, timeToCreate
    Status status

    Image(String s) {
        def (proto, origin, repo, name, tag) = parseUrl(s)
        this.proto = proto
        this.origin = origin
        this.repo = repo
        this.name = name
        this.tag = tag
        this.status = Status.CREATED
    }

    def String toString() {
        return [origin, repo, name, tag].join('-') + this.SUFFIX
    }

    def String toUrl() {
        def proto = this.proto ? "${this.proto}://" : ""
        def repo = this.repo ?: ""
        return "${proto}${repo}/${this.name}"
    }

    private Tuple parseUrl(String url) {
        Protocol proto = Protocol.DOCKER
        String origin = 'index.docker.io',
                repo = 'library',
                name, tag = 'latest'
        def l = url.split('://').reverse() as ArrayList
        if (l.size() == 2) {
            proto = l.pop().toUpperCase() as Protocol
            if (proto == Protocol.SHUB) {
                origin = 'singularity-hub.org'
                tag = 'master'
            }
        }
        def a = l[0].tokenize('/')
        if (a[0].contains('.')) {
            origin = a[0].replaceAll(/:\d+/,'')
            repo = a[1]
        } else {
            if (a.size() > 1) {
                repo = a[0]
            }
        }
        def n = a[-1].tokenize(':@')
        name = n[0]
        if (n.size() > 1) {
            tag = n[1..-1].join('-')
        }
        return new Tuple(proto, origin, repo, name, tag)
    }
}
