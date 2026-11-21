rootProject.name = "component-repository"

include("libs:repository-common")
include("libs:component-publisher")
include("libs:resolver-api")
include("libs:dependency-resolver")

include("impls:local-storage")
include("impls:raf-storage")
include("impls:drive-storage")

include("apps:cli-publisher")
include("apps:cli-resolver")
