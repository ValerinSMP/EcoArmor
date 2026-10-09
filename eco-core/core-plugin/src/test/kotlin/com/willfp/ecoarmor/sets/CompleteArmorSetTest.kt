package com.willfp.ecoarmor.sets

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random

class CompleteArmorSetTest {
    private data class SetDefinition(val id: String, val required: Int)
    private fun oldFullSet(items: List<SetDefinition?>): SetDefinition? {
        val counts = items.filterNotNull().groupingBy { it }.eachCount()
        for ((set, count) in counts) if (count >= set.required) return set
        return null
    }
    private fun sharedCounts(items: List<SetDefinition?>) = items.filterNotNull().groupingBy { it }.eachCount()

    @Test fun `empty or missing armor selects no full set`() {
        assertNull(firstCompleteArmorSet(sharedCounts(emptyList())) { it.required })
        assertNull(firstCompleteArmorSet(sharedCounts(listOf(null, null))) { it.required })
    }

    @Test fun `partial below requirement and exact full requirement match`() {
        val set = SetDefinition("warderita", 4)
        assertNull(firstCompleteArmorSet(sharedCounts(List(3) { set })) { it.required })
        assertSame(set, firstCompleteArmorSet(sharedCounts(List(4) { set })) { it.required })
    }

    @Test fun `mixed qualifying sets select first seen not highest count`() {
        val first = SetDefinition("first", 1)
        val second = SetDefinition("second", 2)
        val counts = sharedCounts(listOf(first, second, second, second))
        assertSame(first, firstCompleteArmorSet(counts) { it.required })
        assertEquals(linkedMapOf(first to 1, second to 3), counts)
    }

    @Test fun `first nonqualifying set does not suppress later complete set`() {
        val first = SetDefinition("first", 4)
        val second = SetDefinition("second", 2)
        assertSame(second, firstCompleteArmorSet(sharedCounts(listOf(first, second, second, null))) { it.required })
    }

    @Test fun `selection does not consume or mutate partial counts`() {
        val set = SetDefinition("full", 2)
        val partial = SetDefinition("partial", 4)
        val counts = linkedMapOf(set to 2, partial to 1)
        val snapshot = counts.toMap()
        firstCompleteArmorSet(counts) { it.required }
        assertEquals(snapshot, counts)
        assertEquals(listOf(set, partial), counts.keys.toList())
    }

    @Test fun `unusual configured requirements preserve prior comparison semantics`() {
        for (required in listOf(-1, 0, 1, 4, 5)) {
            val set = SetDefinition("test", required)
            val items = listOf(set, set, null, null)
            assertEquals(oldFullSet(items), firstCompleteArmorSet(sharedCounts(items)) { it.required })
        }
    }

    @Test fun `random mixed equipment preserves full selection and partial counts`() {
        val random = Random(20261005)
        val pool = List(5) { SetDefinition("set$it", it) } + null
        repeat(500) {
            val equipment = List(4) { pool.random(random) }
            val fullCounts = sharedCounts(equipment)
            assertEquals(oldFullSet(equipment), firstCompleteArmorSet(fullCounts) { it.required })
            assertEquals(sharedCounts(equipment), fullCounts)
        }
    }
}
