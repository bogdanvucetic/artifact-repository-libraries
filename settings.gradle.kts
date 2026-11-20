rootProject.name = "component-repository"
include("libs:repository-common")
include("libs:component-publisher")
include("libs:resolver-api")
include("libs:dependency-resolver")
include("impls:resolver-local")
include("impls:publisher-local")
include("apps:cli-publisher")
include("apps:cli-resolver")