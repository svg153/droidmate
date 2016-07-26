// Copyright (c) 2012-2016 Saarland University
// All rights reserved.
//
// Author: Konrad Jamrozik, jamrozik@st.cs.uni-saarland.de
//
// This file is part of the "DroidMate" project.
//
// www.droidmate.org
package org.droidmate.report

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.LinkedListMultimap
import java.lang.Math.max
import java.time.Duration
import java.time.LocalDateTime

fun <T, TItem> Iterable<T>.itemsAtTime(
  startTime: LocalDateTime,
  extractTime: (T) -> LocalDateTime,
  extractItems: (T) -> Iterable<TItem>
): Map<Long, Iterable<TItem>> {

  fun computeDuration(time: LocalDateTime): Long {
    return Duration.between(startTime, time).toMillis()
  }

  return this.associate { Pair(computeDuration(extractTime(it)), extractItems(it)) }
}

inline fun <T, K, V> Iterable<T>.associateMany(transform: (T) -> Pair<K, V>): Map<K, Iterable<V>> {
  // KJA curr work: move to utils and test. Test for 2 same values, to ensure they were not removed (i.e. we expect list of values, not set of values)
  val multimap = ArrayListMultimap.create<K, V>()
  this.forEach { val pair = transform(it); multimap.put(pair.first, pair.second) }
  return multimap.asMap()
}

fun <K, V> Iterable<Map<K, Iterable<V>>>.flatten(): Map<K, Iterable<V>> {
  // KJA curr work: move to utils and test. 
  // KJA problem here: loses incremental sorting by keys.
  val multimap = LinkedListMultimap.create<K, V>()
  this.forEach { map ->
    map.forEach { multimap.putAll(it.key, it.value) }
  }
  return multimap.asMap()
}

// KJA 2 
fun <T, TItem> Iterable<T>.itemsAtTimes(
  startTime: LocalDateTime,
  extractTime: (TItem) -> LocalDateTime,
  extractItems: (T) -> Iterable<TItem>
): Map<Long, Iterable<TItem>> {

  fun computeDuration(time: LocalDateTime): Long {
    return Duration.between(startTime, time).toMillis()
  }
  // KJA 1 current work. To implement. 
  val itemsAtTimesListedByOriginElement: List<Map<Long, Iterable<TItem>>> = this.map {
    val items: Iterable<TItem> = extractItems(it)
    val itemsAtTime: Map<Long, Iterable<TItem>> = items.associateMany { Pair(computeDuration(extractTime(it)), it) }
    itemsAtTime
  }
  return itemsAtTimesListedByOriginElement.flatten()
}

fun <TItem>  Map<Long, Iterable<TItem>>.accumulateUniqueStrings(
  extractUniqueString: (TItem) -> String
): Map<Long, Iterable<String>> {

  val uniqueStringsAcc: MutableSet<String> = hashSetOf()
  
  return this.mapValues {
    uniqueStringsAcc.addAll(it.value.map { extractUniqueString(it) })
    uniqueStringsAcc.toList()
  }
}

fun <T> Map<Long, T>.partition(partitionSize: Long): Map<Long, List<T>> {

  tailrec fun <T> _partition(
    acc: Collection<Pair<Long, List<T>>>,
    remainder: Collection<Pair<Long, T>>,
    partitionSize: Long,
    currentPartitionValue: Long): Collection<Pair<Long, List<T>>> {

    if (remainder.isEmpty()) return acc else {

      val currentPartition = remainder.partition { it.first <= currentPartitionValue }
      val current: List<Pair<Long, T>> = currentPartition.first
      val currentValues: List<T> = current.fold<Pair<Long, T>, MutableList<T>>(mutableListOf(), { out, pair -> out.add(pair.second); out })

      return _partition(acc.plus(Pair(currentPartitionValue, currentValues)), currentPartition.second, partitionSize, currentPartitionValue + partitionSize)
    }
  }

  return _partition(mutableListOf(Pair(0L, emptyList<T>())), this.toList(), partitionSize, partitionSize).toMap()
}

fun <T> Map<Long, T>.accumulateMaxes(
  extractMax: (T) -> Int
): Map<Long, Int>
{
  var currMaxVal: Int = 0

  return this.mapValues {
    currMaxVal = max(extractMax(it.value), currMaxVal)
    currMaxVal
  }
}

fun Map<Long, Int>.padPartitions(
  partitionSize: Long,
  lastPartition: Long
): Map<Long, Int> {

  require(lastPartition % partitionSize == 0L, { "lastPartition: $lastPartition partitionSize: $partitionSize" })
  require(this.all { it.key % partitionSize == 0L })

  return if (this.isEmpty())
    (0..lastPartition step partitionSize).associate { Pair(it, -1) }
  else {
    val maxKey = this.keys.max() ?: 0
    val paddedPartitions: Map<Long, Int> = ((maxKey + partitionSize)..lastPartition step partitionSize).associate { Pair(it, -1) }
    return this.plus(paddedPartitions)
  }
}
