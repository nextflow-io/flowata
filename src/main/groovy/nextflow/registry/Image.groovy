package nextflow.registry

/**
 * @author Emilio Palumbo <emiliopalumbo@gmail.com>
 */
class Image {
    enum Status {
        PENDING, CACHED, ERROR, CREATED
    }

    final SUFFIX = ".img"

    String proto, repo, name
    Status status

    Image(String s) {
        def (proto, repo, name) = parseUrl(s)
        this.proto = proto
        this.repo = repo
        this.name = name
        this.status = Status.CREATED
    }

    def String toString() {
        return this.name + this.SUFFIX
    }

    def String toUrl() {
        def proto = this.proto ? "${this.proto}://" : ""
        def repo = this.repo ?: ""
        return "${proto}${repo}/${this.name}"
    }

    private Tuple parseUrl(String url) {
        def proto, repo, name
        if ( url.startsWith('/') ) {
            name = url.replaceAll(/.img/,"")
        } else {
            (repo, name) = url.split('/', 2)
            if (repo.contains(':')) {
                proto = repo.replaceAll(':', '')
                (repo, name) = name.substring(1).split('/', 2)
            }
            name = name.replaceAll(/:/, '-').replaceAll(/@/, '@sha256-')
        }
        return new Tuple(proto, repo, name)
    }
}
