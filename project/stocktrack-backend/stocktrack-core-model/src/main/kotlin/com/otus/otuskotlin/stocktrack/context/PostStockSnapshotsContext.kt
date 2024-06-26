package com.otus.otuskotlin.stocktrack.context

import com.otus.otuskotlin.stocktrack.model.Command
import com.otus.otuskotlin.stocktrack.model.Debug
import com.otus.otuskotlin.stocktrack.model.ErrorDescription
import com.otus.otuskotlin.stocktrack.model.RequestId
import com.otus.otuskotlin.stocktrack.model.State
import com.otus.otuskotlin.stocktrack.snapshot.StockSnapshot
import java.time.Instant
import java.util.*

data class PostStockSnapshotsContext(
    override val command: Command,
    override val request: List<StockSnapshot> = emptyList(),
    override val response: Unit = Unit,
    override val state: State = State.NONE,
    override val errors: List<ErrorDescription> = emptyList(),
    override val debug: Debug = Debug.NONE,
    override val requestId: RequestId = RequestId(value = UUID.randomUUID().toString()),
    override val startedAt: Instant = Instant.MIN
) : Context<List<StockSnapshot>, Unit, PostStockSnapshotsContext> {
    override fun start(): PostStockSnapshotsContext {
        return copy(
            startedAt = Instant.now(),
            state = State.RUNNING
        )
    }

    override fun fail(error: ErrorDescription): PostStockSnapshotsContext {
        return copy(
            state = State.FAILED,
            errors = errors + error
        )
    }

    override fun fail(error: Collection<ErrorDescription>): PostStockSnapshotsContext {
        return copy(
            state = State.FAILED,
            errors = errors + error
        )
    }

    override fun finish(): PostStockSnapshotsContext {
        return copy(state = State.FINISHED)
    }
}