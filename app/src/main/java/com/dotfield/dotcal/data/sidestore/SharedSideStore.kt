package com.dotfield.dotcal.data.sidestore

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class SharedSideStore(
    private val file: File,
) {
    constructor(context: Context) : this(File(context.applicationContext.filesDir, FILE_NAME))

    private val mutex = Mutex()
    private var cache: MutableMap<String, MutableMap<String, String>>? = null

    suspend fun read(namespace: String, key: String): String? = withContext(Dispatchers.IO) {
        mutex.withLock { loadLocked()[namespace]?.get(key) }
    }

    suspend fun readNamespace(namespace: String): Map<String, String> = withContext(Dispatchers.IO) {
        mutex.withLock { loadLocked()[namespace]?.toMap().orEmpty() }
    }

    suspend fun write(namespace: String, key: String, jsonValue: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val data = loadLocked()
            data.getOrPut(namespace) { linkedMapOf() }[key] = jsonValue
            persistLocked(data)
        }
    }

    suspend fun remove(namespace: String, key: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val data = loadLocked()
            data[namespace]?.remove(key)
            if (data[namespace]?.isEmpty() == true) data.remove(namespace)
            persistLocked(data)
        }
    }

    private fun loadLocked(): MutableMap<String, MutableMap<String, String>> {
        cache?.let { return it }
        val loaded = if (file.exists()) parseStoreJson(file.readText()) else linkedMapOf()
        cache = loaded
        return loaded
    }

    private fun persistLocked(data: Map<String, Map<String, String>>) {
        file.parentFile?.let { if (!it.exists()) it.mkdirs() }
        file.writeText(encodeStoreJson(data))
        cache = data.mapValuesTo(linkedMapOf()) { (_, values) -> values.toMutableMap() }
    }

    companion object {
        private const val FILE_NAME = "dotcal_side_store.json"

        private fun encodeStoreJson(data: Map<String, Map<String, String>>): String {
            return data.entries.joinToString(prefix = "{", postfix = "}") { (namespace, values) ->
                val encodedValues = values.entries.joinToString(prefix = "{", postfix = "}") { (key, value) ->
                    "\"${escapeJson(key)}\":\"${escapeJson(value)}\""
                }
                "\"${escapeJson(namespace)}\":$encodedValues"
            }
        }

        private fun parseStoreJson(text: String): MutableMap<String, MutableMap<String, String>> {
            val parser = StoreJsonParser(text)
            return runCatching { parser.parseRoot() }.getOrDefault(linkedMapOf())
        }

        private fun escapeJson(value: String): String = buildString(value.length) {
            value.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
        }
    }
}

private class StoreJsonParser(private val text: String) {
    private var index = 0

    fun parseRoot(): MutableMap<String, MutableMap<String, String>> {
        val root = linkedMapOf<String, MutableMap<String, String>>()
        consume('{')
        skipWhitespace()
        while (!peek('}')) {
            val namespace = readString()
            consume(':')
            root[namespace] = readStringMap()
            skipWhitespace()
            if (peek(',')) consume(',') else break
        }
        consume('}')
        return root
    }

    private fun readStringMap(): MutableMap<String, String> {
        val values = linkedMapOf<String, String>()
        consume('{')
        skipWhitespace()
        while (!peek('}')) {
            val key = readString()
            consume(':')
            values[key] = readString()
            skipWhitespace()
            if (peek(',')) consume(',') else break
        }
        consume('}')
        return values
    }

    private fun readString(): String {
        skipWhitespace()
        consume('"')
        val result = StringBuilder()
        while (index < text.length) {
            val char = text[index++]
            when (char) {
                '"' -> return result.toString()
                '\\' -> result.append(readEscape())
                else -> result.append(char)
            }
        }
        error("Unterminated string")
    }

    private fun readEscape(): Char {
        if (index >= text.length) error("Unterminated escape")
        return when (val escaped = text[index++]) {
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            '"', '\\' -> escaped
            else -> escaped
        }
    }

    private fun consume(expected: Char) {
        skipWhitespace()
        if (index >= text.length || text[index] != expected) error("Expected $expected")
        index += 1
    }

    private fun peek(char: Char): Boolean {
        skipWhitespace()
        return index < text.length && text[index] == char
    }

    private fun skipWhitespace() {
        while (index < text.length && text[index].isWhitespace()) index += 1
    }
}
