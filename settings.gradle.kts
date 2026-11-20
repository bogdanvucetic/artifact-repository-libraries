rootProject.name = "component-repository"

include("libs:repository-common")
include("libs:component-publisher")
include("libs:resolver-api")
include("libs:dependency-resolver")

include("impls:publisher-local")
include("impls:publisher-raf")
include("impls:resolver-local")
include("impls:resolver-raf")

include("apps:cli-publisher")
include("apps:cli-resolver")
