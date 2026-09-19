package com.dotfield.dotcal.data.readiness

import com.dotfield.dotcal.data.sidestore.SharedSideStore
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class EventReadinessItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
)

class EventReadinessStore(
    private val sideStore: SharedSideStore,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {
    private val mutex = Mutex()

    suspend fun read(eventId: String): List<EventReadinessItem> {
        return sideStore.read(Namespace, eventId)
            ?.let(::parseEventReadinessItems)
            .orEmpty()
    }

    suspend fun add(eventId: String, title: String): List<EventReadinessItem> = mutate(eventId) { items ->
        items + EventReadinessItem(
            id = idFactory(),
            title = normalizedTitle(title),
            isCompleted = false,
        )
    }

    suspend fun rename(eventId: String, itemId: String, title: String): List<EventReadinessItem> {
        val normalized = normalizedTitle(title)
        return mutate(eventId) { items ->
            items.map { item -> if (item.id == itemId) item.copy(title = normalized) else item }
        }
    }

    suspend fun setCompleted(
        eventId: String,
        itemId: String,
        completed: Boolean,
    ): List<EventReadinessItem> = mutate(eventId) { items ->
        items.map { item -> if (item.id == itemId) item.copy(isCompleted = completed) else item }
    }

    suspend fun remove(eventId: String, itemId: String): List<EventReadinessItem> = mutate(eventId) { items ->
        items.filterNot { it.id == itemId }
    }

    suspend fun clear(eventId: String) {
        mutex.withLock { sideStore.remove(Namespace, eventId) }
    }

    suspend fun move(fromEventId: String, toEventId: String) {
        if (fromEventId == toEventId) return
        mutex.withLock {
            val encoded = sideStore.read(Namespace, fromEventId) ?: return@withLock
            sideStore.write(Namespace, toEventId, encoded)
            sideStore.remove(Namespace, fromEventId)
        }
    }

    private suspend fun mutate(
        eventId: String,
        transform: (List<EventReadinessItem>) -> List<EventReadinessItem>,
    ): List<EventReadinessItem> = mutex.withLock {
        val updated = transform(read(eventId))
        if (updated.isEmpty()) {
            sideStore.remove(Namespace, eventId)
        } else {
            sideStore.write(Namespace, eventId, updated.encodeEventReadinessItems())
        }
        updated
    }

    private fun normalizedTitle(title: String): String {
        return title.trim().also { normalized ->
            require(normalized.isNotEmpty()) { "CHECKLIST ITEM REQUIRED" }
            require(normalized.length <= MaxTitleLength) { "CHECKLIST ITEM TOO LONG" }
        }
    }

    companion object {
        const val Namespace = "event_readiness"
        const val MaxTitleLength = 120
    }
}

fun List<EventReadinessItem>.encodeEventReadinessItems(): String {
    return joinToString("\n") { item ->
        listOf(item.id, item.title, item.isCompleted.toString())
            .joinToString("\t") { value -> value.urlEncode() }
    }
}

fun parseEventReadinessItems(value: String): List<EventReadinessItem> {
    return value.lineSequence()
        .filter { it.isNotBlank() }
        .mapNotNull { line ->
            runCatching {
                val parts = line.split('\t').map { part -> part.urlDecode() }
                if (parts.size != 3) return@runCatching null
                val completed = parts[2].toBooleanStrictOrNull() ?: return@runCatching null
                EventReadinessItem(
                    id = parts[0].takeIf { it.isNotBlank() } ?: return@runCatching null,
                    title = parts[1].takeIf { it.isNotBlank() } ?: return@runCatching null,
                    isCompleted = completed,
                )
            }.getOrNull()
        }
        .toList()
}

private fun String.urlEncode(): String = URLEncoder.encode(this, StandardCharsets.UTF_8.name())

private fun String.urlDecode(): String = URLDecoder.decode(this, StandardCharsets.UTF_8.name())
