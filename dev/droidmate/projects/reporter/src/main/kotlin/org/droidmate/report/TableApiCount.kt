// Copyright (c) 2012-2016 Saarland University
// All rights reserved.
//
// Author: Konrad Jamrozik, jamrozik@st.cs.uni-saarland.de
//
// This file is part of the "DroidMate" project.
//
// www.droidmate.org
package org.droidmate.report

import com.google.common.collect.Table
import org.droidmate.exploration.data_aggregators.IApkExplorationOutput2

class TableApiCount() {
  
  companion object {

    val headerTime = "Time_seconds"
    val headerApisSeen = "Apis_seen"
    val headerApiEventsSeen = "Api+Event_pairs_seen"

    // KJA 4 DRY with TableViewCount
    val stepSizeInMs = 1000L

    fun build(data: IApkExplorationOutput2): Table<Int, String, Int> {

      // KJA 4 DRY with TableViewCount
      val timeRange: List<Long> = 0L.rangeTo(data.explorationTimeInMs).step(stepSizeInMs).toList()
      val uniqueApisCountByTime = data.uniqueApisCountByTime
      // KJA 3 to implement
      val uniqueApiEventPairsCountByTime = timeRange.associate { Pair(it, 0) }
      
      return buildTable(
        headers = listOf(headerTime, headerApisSeen, headerApiEventsSeen),
        rowCount = timeRange.size,
        computeRow = { rowIndex ->
          val timePassed = timeRange[rowIndex]
          listOf(
            (timePassed / stepSizeInMs).toInt(),
            uniqueApisCountByTime[timePassed]!!,
            uniqueApiEventPairsCountByTime[timePassed]!!)
        }
      )
    }

    private val IApkExplorationOutput2.uniqueApisCountByTime: Map<Long, Int> get() {
      val partitionSize = 1000L
      // KJA 4 DRY with TableViewCount
      // KJA 2 to implement itemsAtTimes 
      return this.actRess.itemsAtTimes(
        extractItems = { it.result.deviceLogs.apiLogsOrEmpty },
        startTime = this.explorationStartTime,
        extractTime = { it.time }
      )
        .mapKeys {
          // KNOWN BUG got here time with relation to exploration start of -25, but it should be always > 0.
          // The currently applied workaround is to add 100 milliseconds.
          it.key + 100L
        }
        // KJA bug here: because the input collection is not ordered by keys, the accumulation is screwwed.
        .accumulateUniqueStrings(
          extractUniqueString = { it.uniqueString }
        )
        .mapValues { it.value.count() }
        .partition(partitionSize)
        .accumulateMaxes(extractMax = { it.max() ?: 0 })
        .padPartitions(partitionSize, lastPartition = this.explorationTimeInMs.zeroLeastSignificantDigits(3))
    }

  }
}