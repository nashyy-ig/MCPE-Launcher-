package com.example.mcpelauncher

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * One tracked Minecraft: Bedrock Edition build the user has supplied a legitimate
 * APK for (e.g. exported from their own device, or downloaded from an official
 * source they trust). This app does not source, patch, or resign APKs itself.
 */
data class McpeVersion(
    val id: String,
    val label: String,
    val versionName: String,
    val packageName: String,
    val apkFileName: String,
)

class VersionStore(private val context: Context) {

    private val prefs = context.getSharedPreferences("versions", Context.MODE_PRIVATE)
    private val versionsDir = File(context.filesDir, "versions").apply { mkdirs() }

    fun apkDir(): File = versionsDir

    fun all(): List<McpeVersion> {
        val raw = prefs.getString("list", "[]") ?: "[]"
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            McpeVersion(
                id = o.getString("id"),
                label = o.getString("label"),
                versionName = o.getString("versionName"),
                packageName = o.getString("packageName"),
                apkFileName = o.getString("apkFileName"),
            )
        }
    }

    fun add(version: McpeVersion) {
        val list = all().toMutableList()
        list.removeAll { it.id == version.id }
        list.add(version)
        save(list)
    }

    fun remove(version: McpeVersion) {
        save(all().filterNot { it.id == version.id })
        File(versionsDir, version.apkFileName).delete()
    }

    fun apkFile(version: McpeVersion): File = File(versionsDir, version.apkFileName)

    private fun save(list: List<McpeVersion>) {
        val arr = JSONArray()
        list.forEach { v ->
            arr.put(JSONObject().apply {
                put("id", v.id)
                put("label", v.label)
                put("versionName", v.versionName)
                put("packageName", v.packageName)
                put("apkFileName", v.apkFileName)
            })
        }
        prefs.edit().putString("list", arr.toString()).apply()
    }
}
