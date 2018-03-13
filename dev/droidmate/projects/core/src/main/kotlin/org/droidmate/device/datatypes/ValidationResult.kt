// DroidMate, an automated execution generator for Android apps.
// Copyright (C) 2012-2018. Saarland University
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// Current Maintainers:
// Nataniel Borges Jr. <nataniel dot borges at cispa dot saarland>
// Jenny Hotzkow <jenny dot hotzkow at cispa dot saarland>
//
// Former Maintainers:
// Konrad Jamrozik <jamrozik at st dot cs dot uni-saarland dot de>
//
// web: www.droidmate.org
package org.droidmate.device.datatypes

public enum class ValidationResult constructor(val valid: Boolean, val description: String) {

    OK(true, "The window xml hierarchy dump is well-formed and does not represent an 'app has stopped' dialog box."),
    app_has_stopped_dialog_box_with_OK_button_enabled(true, "The window xml hierarchy dump represents 'app has stopped' dialog box and the 'OK' button is enabled."),
    app_has_stopped_dialog_box_with_OK_button_disabled(false, "The window xml hierarchy dump represents 'app has stopped' dialog box, but the 'OK' button is disabled."),
    request_runtime_permission_dialog_box_with_Allow_button_enabled(true, "The window xml hierarchy dump represents 'request runtime permission' dialog box and the 'Allow' button is enabled."),
    request_runtime_permission_dialog_box_with_Allow_button_disabled(false, "The window xml hierarchy dump represents 'request runtime permission' dialog box, but the 'Allow' button is disabled."),
    missing_root_xml_node_prefix(false, "The window xml hierarchy dump doesn't contain the root node prefix, i.e.: " + UiautomatorWindowDump.rootXmlNodePrefix),
    is_empty(false, "The window xml hierarchy dump is empty (its string length is zero)"),
    is_null(false, "The window xml hierarchy dump is null"),
    error(false, "This item should never be reached")
}
