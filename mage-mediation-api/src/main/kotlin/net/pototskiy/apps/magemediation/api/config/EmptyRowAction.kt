package net.pototskiy.apps.magemediation.api.config

import javax.xml.bind.annotation.XmlEnum
import javax.xml.bind.annotation.XmlEnumValue
import javax.xml.bind.annotation.XmlType

@XmlType
@XmlEnum(String::class)
enum class EmptyRowAction {
    @field:XmlEnumValue("stop")
    STOP,
    @field:XmlEnumValue("ignore")
    IGNORE
}