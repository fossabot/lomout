package net.pototskiy.apps.magemediation

import com.beust.jcommander.Parameter

object Args {
    @Parameter(
        description = "excel file to import"
    )
    var files: List<String> = mutableListOf()
    @Parameter(
        names = ["--help"],
        description = "show help",
        help = true
    )
    var help: Boolean = false
    @Parameter(
        names = ["-c", "--net.pototskiy.apps.magemediation.config.configuration"],
        description = "specify import net.pototskiy.apps.magemediation.config.configuration file",
        arity = 1
    )
    var configFile: String = "config.xml"
    @Parameter(
        names = ["-l", "--log-level"],
        description = "log level: fatal, error, warn, info",
        arity = 1
    )
    var logLevel: String = "warn"
    @Parameter(
        names = ["--excel-product"],
        description = "excel product file path",
        arity = 1,
        required = false
    )
    var excelProductFile: String = ""
    @Parameter(
        names = ["--mage-product"],
        description = "magento product file path",
        arity = 1,
        required = false
    )
    var mageProductFile: String = ""
}