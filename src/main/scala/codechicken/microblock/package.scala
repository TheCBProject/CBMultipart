package codechicken

import codechicken.microblock.utils.McImplicits
import org.apache.logging.log4j.LogManager

package object microblock extends McImplicits {
    def logger = LogManager.getLogger("CBMicroblock")
}
