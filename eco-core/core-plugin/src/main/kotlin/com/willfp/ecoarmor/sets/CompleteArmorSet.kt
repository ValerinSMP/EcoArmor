package com.willfp.ecoarmor.sets

/** Preserve first-seen set selection while allowing full and partial sets to share counts. */
internal inline fun <T> firstCompleteArmorSet(counts: Map<T, Int>, required: (T) -> Int): T? {
    for ((set, count) in counts) {
        if (count >= required(set)) return set
    }
    return null
}
