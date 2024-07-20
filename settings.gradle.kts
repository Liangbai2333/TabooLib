rootProject.name = "TabooLib"
include("common", "common-env", "common-util", "common-legacy-api", "common-reflex", "common-platform-api")
include(
//    "module:module-ai",
    "module:module-chat",
    "module:module-configuration",
    "module:module-lang",
    "module:module-metrics",
//    "module:module-navigation",
//    "module:module-bukkit-hook",
//    "module:module-bukkit-util",
//    "module:module-bukkit-util",
//    "module:module-bukkit-xseries",
    "module:module-database",
    "module:module-effect",
//    "module:module-kether",
    "module:module-nms",
//    "module:module-nms-util",
//    "module:module-nms-util-legacy",
//    "module:module-nms-util-stable",
//    "module:module-nms-util-unstable",
//    "module:module-nms-util-tag",
//    "module:module-nms-util-tag-12005",
//    "module:module-nms-util-tag-legacy",
//    "module:module-porticus",
//    "module:module-ui",
//    "module:module-ui-legacy",
//    "module:module-test"
)
include(
    "expansion:expansion-alkaid-redis",
    "expansion:expansion-lettuce-redis",
    "expansion:expansion-command-helper",
    // "expansion:expansion-geek-tool",
    // "expansion:expansion-ioc",
    "expansion:expansion-javascript",
    "expansion:expansion-jexl",
    "expansion:expansion-persistent-container",
    "expansion:expansion-persistent-container-object",
    "expansion:expansion-player-database",
    // "expansion:expansion-player-fake-op",
    "expansion:expansion-submit-chain"
)
include(
    "platform:platform-application",
    "platform:platform-bukkit",
    "platform:platform-bukkit-impl",
    "platform:platform-bungee",
    "platform:platform-bungee-impl",
    "platform:platform-velocity",
    "platform:platform-velocity-impl",
    "platform:platform-afybroker"
)
